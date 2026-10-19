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
    @Test fun sharedAppearanceFixtureRendersAtSameSizeAsStandardWidget() {
        val context=RuntimeEnvironment.getApplication()
        val program=Compiler.compile(context.assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() })
        val inst=WidgetInstance(program);val now=System.currentTimeMillis()
        inst.set("now",Num(now/1000.0));inst.set("ichimoku",TRUE)
        for((name,raw) in com.buildapptest.bitcoinpulse.WidgetVisualFixture.data(now)) inst.set(name,Obj(mapOf("ok" to TRUE,"out" to Json.parseOrText(raw))))
        program.triggers.forEach { it.action.run(inst.root,emptyList()) }
        val control=Robolectric.buildActivity(ComponentActivity::class.java);val activity=control.get();activity.enableEdgeToEdge();control.setup().visible()
        activity.setContent { Box(Modifier.fillMaxSize()) { WidgetContent(inst,Modifier.width(360.dp).height(840.dp),cells=6 to 13) } }
        val density=context.resources.displayMetrics.density;val width=(360*density).toInt();val height=(840*density).toInt();val decor=activity.window.decorView
        val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        repeat(3) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(350))
            decor.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));decor.layout(0,0,width,height);decor.draw(Canvas(bitmap))
        }
        assertTrue("Runtime errors: ${inst.problems}",inst.problems.isEmpty())
        val folder=File("build/reports/widget-parity").apply { mkdirs() }
        File(folder,"omalauncher-360.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        fun owner(v:View):SemanticsOwner? {
            if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
            if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
            return null
        }
        val nodes=owner(decor)!!.getAllSemanticsNodes(false)
        val anchors=org.json.JSONObject()
        for((name,text) in listOf("price" to "€ 72.521,90","cloud" to "Cloud Aan","first" to "4 uur ▾","second" to "1 dag ▾","block" to "Blok #971.890","fee" to "3 sat/vB","mempool" to "Mempool 18.420 tx · 12,8 vMB","refresh" to "↻")) {
            val node=nodes.first { it.config.getOrNull(SemanticsProperties.Text)?.any { t -> t.text==text }==true }
            anchors.put(name,node.boundsInRoot.center.y/density)
        }
        File(folder,"omalauncher-anchors.json").writeText(anchors.toString(2))
        control.pause().stop().destroy()
    }
    @Test fun missingAndFailedFeedsKeepCurrencyAndPeriodCachesSeparate() {
        val source=RuntimeEnvironment.getApplication().assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() }
        val program=Compiler.compile(source);val inst=WidgetInstance(program)
        inst.set("now",Num(1000.0));inst.set("ichimoku",TRUE)
        val response=Json.parseOrText(File("../../.tools/omalauncher-reference/data/eur4h.json").readText())
        inst.set("eur4h",Obj(mapOf("ok" to TRUE,"out" to response)))
        program.triggers.forEach { it.action.run(inst.root,emptyList()) }
        fun sizes()=(inst.get("charts") as Lst).items.map { (it as Lst).items.size }
        assertEquals(listOf(36,0),sizes())
        val cached=inst.get("cacheeur4h");val stamp=inst.get("timeeur4h")
        inst.set("now",Num(2000.0));inst.set("eur4h",Obj(mapOf("ok" to FALSE)))
        program.triggers.forEach { it.action.run(inst.root,emptyList()) }
        assertEquals(cached,inst.get("cacheeur4h"));assertEquals(stamp,inst.get("timeeur4h"))
        inst.set("interval2",Num(10080.0));assertEquals(listOf(36,0),sizes())
        inst.set("currency",Str("USD"));assertEquals(listOf(0,0),sizes())
        assertTrue(inst.problems.isEmpty())
    }
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
                    WidgetContent(inst, Modifier.width(width.dp).height(840.dp), cells=6 to 13)
                }
            }
            val decor = activity.window.decorView
            val density = context.resources.displayMetrics.density
            val frame=Bitmap.createBitmap((width*density).toInt(),(870*density).toInt(),Bitmap.Config.ARGB_8888)
            fun settle() {
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250))
                decor.measure(View.MeasureSpec.makeMeasureSpec((width*density).toInt(),View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec((870*density).toInt(),View.MeasureSpec.EXACTLY))
                decor.layout(0,0,(width*density).toInt(),(870*density).toInt())
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
                decor.draw(Canvas(frame))
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
                    assertTrue("$width dp: text outside widget '$text': $b",b.left>=0 && b.right<=(width*density)+1 && b.bottom<=840*density+1)
                }
                assertTrue("Runtime errors: ${inst.problems}", inst.problems.isEmpty())
            }
            verifyText()
            fun capture(suffix: String) {
                val bitmap=Bitmap.createBitmap((width*density).toInt(),(840*density).toInt(),Bitmap.Config.ARGB_8888)
                decor.draw(Canvas(bitmap))
                val output=File("build/reports/omalauncher-native").apply { mkdirs() }
                File(output,"omalauncher-$width-$suffix.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                val caption=textNodes().first { nodeText(it).startsWith("Laatste candle") }.boundsInRoot
                val chartStart=(caption.top-116*density).toInt().coerceAtLeast(0)
                val chartEnd=caption.top.toInt().coerceAtMost(bitmap.height)
                var red=0;var green=0
                var boundaries=0
                for(y in chartStart until chartEnd) for(x in 0 until bitmap.width) when(bitmap.getPixel(x,y)) {0xffff647c.toInt()->red++;0xff35e89a.toInt()->green++}
                if(suffix=="cloud") {
                    for(y in chartStart until chartEnd) for(x in 0 until bitmap.width) if(bitmap.getPixel(x,y) in listOf(0xffa5d6a7.toInt(),0xffef9a9a.toInt()))boundaries++
                    assertTrue("Cloud boundaries must be drawn",boundaries>10)
                }
                assertTrue("Red and green must survive a green launcher theme: red=$red green=$green", red>10 && green>10)
                assertEquals("Opaque widget background",0xff182230.toInt(),bitmap.getPixel(bitmap.width/2,4))
            }
            capture("eur")
            fun tap(text: String) {
                val n=textNodes().first { nodeText(it)==text };val b=n.boundsInRoot;val time=SystemClock.uptimeMillis()
                for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
                    val e=MotionEvent.obtain(time,SystemClock.uptimeMillis(),action,b.center.x,b.center.y,0);decor.dispatchTouchEvent(e);e.recycle()
                }
                settle();settle()
            }
            tap("Cloud Uit");assertEquals(TRUE,inst.get("ichimoku"));settle();verifyText();capture("cloud")
            fun verifyCloud(currency: String, sourceName: String, slot: Int=0) {
                val rows=com.buildapptest.bitcoinpulse.data.CandleParser.parse(File("../../.tools/omalauncher-reference/data/$sourceName.json").readText(),currency)
                val count=(inst.get("zoomCounts") as Lst).items[slot].num()!!.toInt()
                val expected=com.buildapptest.bitcoinpulse.data.Ichimoku.calculate(rows).filter { it.index>=rows.size-count }
                val actual=((inst.get("clouds") as Lst).items[slot] as Lst).items
                assertEquals(expected.size,actual.size)
                expected.zip(actual).forEach { (p,value) ->
                    val fields=(value as Obj).fields
                    assertEquals((p.index-(rows.size-count)).toDouble(),fields["index"]!!.num()!!,0.0)
                    assertEquals(p.a,fields["a"]!!.num()!!,0.000001)
                    assertEquals(p.b,fields["b"]!!.num()!!,0.000001)
                }
            }
            verifyCloud("EUR","eur4h")
            tap("−");assertEquals(Num(60.0),inst.get("zoom4h"));verifyCloud("EUR","eur4h")
            tap("−");assertEquals(Num(90.0),inst.get("zoom4h"));verifyCloud("EUR","eur4h");verifyText();capture("zoom-90")
            tap("−");assertEquals(Num(90.0),inst.get("zoom4h"))
            tap("+");tap("+");tap("+");assertEquals(Num(24.0),inst.get("zoom4h"));verifyCloud("EUR","eur4h");verifyText();capture("zoom-24")
            tap("+");assertEquals(Num(24.0),inst.get("zoom4h"))
            assertEquals(Num(36.0),inst.get("zoomDay"));assertEquals(Num(36.0),inst.get("zoomWeek"))
            tap("−");assertEquals(Num(36.0),inst.get("zoom4h"))
            for(code in listOf("EUR","USD")) for((interval,name) in listOf(240.0 to "4h",1440.0 to "Day",10080.0 to "Week")) {
                inst.set("currency",Str(code));inst.set("interval",Num(interval))
                program.triggers.last().action.run(inst.root,emptyList())
                verifyCloud(code,code.lowercase()+name)
            }
            inst.set("currency",Str("EUR"));inst.set("interval",Num(240.0));program.triggers.last().action.run(inst.root,emptyList());settle()
            tap("Cloud Aan");assertEquals(FALSE,inst.get("ichimoku"));settle()
            assertTrue((inst.get("clouds") as Lst).items.all { (it as Lst).items.isEmpty() })
            tap("Cloud Uit")
            tap("EUR ▾"); assertEquals(Str("USD"),inst.get("currency"))
            program.triggers.last().action.run(inst.root, emptyList());settle();verifyText();capture("usd")
            tap("4 uur ▾");verifyText();assertEquals(Num(0.0),inst.get("picker"));capture("picker")
            tap("1 week");assertEquals(Num(10080.0),inst.get("interval"));verifyCloud("USD","usdWeek")
            tap("1 week ▾");assertEquals(Num(0.0),inst.get("picker"));tap("4 uur");assertEquals("First slot returns to four hours",Num(240.0),inst.get("interval"))
            tap("1 dag ▾");assertEquals("Second picker opens",Num(1.0),inst.get("picker"));capture("second-picker");tap("1 week")
            assertEquals(Num(10080.0),inst.get("interval2"));verifyCloud("USD","usdWeek",1)
            for((first,second) in listOf(240.0 to 1440.0,240.0 to 10080.0,1440.0 to 10080.0)) {
                inst.set("interval",Num(first));inst.set("interval2",Num(second));settle();verifyText()
                val names=mapOf(240.0 to "usd4h",1440.0 to "usdDay",10080.0 to "usdWeek")
                verifyCloud("USD",names.getValue(first));verifyCloud("USD",names.getValue(second),1)
            }
            capture("two-charts")
            inst.set("graphCount",Num(1.0));assertEquals(Num(1.0),inst.get("graphCount"));settle();verifyText()
            assertEquals(1,textNodes().count { nodeText(it).endsWith(" ▾") && !nodeText(it).startsWith("USD") })
            inst.set("graphCount",Num(2.0));assertEquals(Num(2.0),inst.get("graphCount"));settle();verifyText()
            val saved=(inst.get("histories") as Lst).items[1]
            inst.set("usdWeek",Obj(mapOf("ok" to FALSE)))
            program.triggers.forEach { it.action.run(inst.root,emptyList()) };settle()
            assertEquals("Failed weekly feed keeps only its own cache",saved,(inst.get("histories") as Lst).items[1])
            assertTrue(inst.problems.isEmpty())
            tap("↻");assertTrue(inst.actions.contains("refresh"))
            println("PASS upstream renderer at $width × 840 dp: text fits, footer visible, one/two charts, per-slot period choices, cloud/zoom parity, cache isolation, currency and refresh")
            control.pause().stop().destroy()
        }
    }
}
