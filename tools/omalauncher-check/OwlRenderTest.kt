package com.buildapptest.bitcoinpulse.owlcheck

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import dev.omalauncher.widgets.lang.*
import dev.omalauncher.widgets.render.WidgetContent
import dev.omalauncher.widgets.runtime.WidgetInstance
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35], application=Application::class, qualifiers="w430dp-h900dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OwlRenderTest {
    @Test fun productionOwlRendersAtPhoneWidthsUsingUpstreamRenderer() {
        val context = RuntimeEnvironment.getApplication()
        val source = context.assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() }
        val program = Compiler.compile(source)
        assertFalse(program.needsLinux)
        assertTrue(program.warnings.isEmpty())
        for (width in listOf(320, 360, 390)) {
            val inst = WidgetInstance(program)
            val now = System.currentTimeMillis() / 1000.0
            inst.set("now", Num(now))
            for (name in listOf("ticker", "blocks", "fees", "pool", "eur4h", "usd4h", "eurDay", "usdDay", "eurWeek", "usdWeek")) {
                val payload = Json.parseOrText(File("../../.tools/omalauncher-reference/data/$name.json").readText())
                inst.set(name, Obj(mapOf("ok" to TRUE, "out" to payload)))
            }
            program.triggers.forEach { it.action.run(inst.root, emptyList()) }
            // Use a recent receipt time but retain the real recorded block age.
            val control = Robolectric.buildActivity(ComponentActivity::class.java)
            val activity = control.get()
            activity.enableEdgeToEdge()
            control.setup().visible()
            activity.setContent {
                Box(Modifier.fillMaxSize().background(Color.Magenta)) {
                    WidgetContent(inst, Modifier.width(width.dp).height(560.dp), cells=6 to 9)
                }
            }
            val decor = activity.window.decorView
            val density = context.resources.displayMetrics.density
            fun settle() {
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250))
                decor.measure(View.MeasureSpec.makeMeasureSpec((width*density).toInt(),View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec((650*density).toInt(),View.MeasureSpec.EXACTLY))
                decor.layout(0,0,(width*density).toInt(),(650*density).toInt())
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
            }
            settle(); settle()
            fun owner(v: View): SemanticsOwner? {
                if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
                if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
                return null
            }
            val semantics = owner(decor) ?: error("No Compose semantics owner")
            fun textNodes() = semantics.getAllSemanticsNodes(false).filter { it.config.getOrNull(SemanticsProperties.Text) != null }
            fun nodeText(n: androidx.compose.ui.semantics.SemanticsNode) = n.config.getOrNull(SemanticsProperties.Text)!!.joinToString { it.text }
            fun verifyText() {
                val nodes = textNodes()
                assertTrue(nodes.any { nodeText(it).startsWith("Blok #") })
                assertTrue(nodes.any { nodeText(it) == "Volgend blok" })
                for (node in nodes) {
                    val text = nodeText(node)
                    val layouts = mutableListOf<TextLayoutResult>()
                    node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    for (layout in layouts) assertFalse("$width dp: clipped text '$text'",layout.hasVisualOverflow)
                    val b=node.boundsInRoot
                    assertTrue("$width dp: text outside widget '$text': $b",b.left>=0 && b.right<=(width*density)+1 && b.bottom<=560*density+1)
                }
                assertTrue("Runtime errors: ${inst.problems}", inst.problems.isEmpty())
            }
            verifyText()
            fun capture(suffix: String) {
                val bitmap=Bitmap.createBitmap((width*density).toInt(),(560*density).toInt(),Bitmap.Config.ARGB_8888)
                decor.draw(Canvas(bitmap))
                val output=File("build/reports/omalauncher-native").apply { mkdirs() }
                File(output,"omalauncher-$width-$suffix.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                val caption=textNodes().first { nodeText(it).startsWith("Laatste candle") }.boundsInRoot
                val chartStart=(caption.top-116*density).toInt().coerceAtLeast(0)
                val chartEnd=caption.top.toInt().coerceAtMost(bitmap.height)
                var red=0;var green=0
                for(y in chartStart until chartEnd) for(x in 0 until bitmap.width) when(bitmap.getPixel(x,y)) {0xffff647c.toInt()->red++;0xff35e89a.toInt()->green++}
                assertTrue("Red and green must survive a green launcher theme: red=$red green=$green", red>10 && green>10)
                assertEquals("Opaque widget background",0xff182230.toInt(),bitmap.getPixel(bitmap.width/2,4))
            }
            capture("eur")
            fun tap(text: String) {
                val n=textNodes().first { nodeText(it)==text };val b=n.boundsInRoot;val time=SystemClock.uptimeMillis()
                for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
                    val e=MotionEvent.obtain(time,SystemClock.uptimeMillis(),action,b.center.x,b.center.y,0);decor.dispatchTouchEvent(e);e.recycle()
                }
                settle()
            }
            tap("EUR ▾"); assertEquals(Str("USD"),inst.get("currency"))
            program.triggers.last().action.run(inst.root, emptyList());settle();verifyText();capture("usd")
            for ((label, interval) in listOf("1 dag" to 1440.0, "1 week" to 10080.0)) {
                tap(label);assertEquals(Num(interval),inst.get("interval"))
                program.triggers.last().action.run(inst.root, emptyList());settle();verifyText()
            }
            tap("↻");assertTrue(inst.actions.contains("refresh"))
            println("PASS upstream renderer at $width × 560 dp: text fits, footer visible, red/green candles, currency, all periods and refresh")
            control.pause().stop().destroy()
        }
    }
}
