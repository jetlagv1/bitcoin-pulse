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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.buildapptest.bitcoinpulse.widget.Omalauncher
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
@Config(sdk=[35],application=Application::class,qualifiers="w430dp-h900dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OwlAdaptiveTest {
    @Test fun everyImportSizeGrowsAndShrinksWithWorkingControlsAndRetainedSettings() {
        val context=RuntimeEnvironment.getApplication()
        for(asset in listOf("bitcoin-pulse-mini.owl","bitcoin-pulse-compact.owl","bitcoin-pulse.owl")) {
            val program=Compiler.compile(context.assets.open(asset).bufferedReader().use { it.readText() })
            assertEquals(1 to 1,program.minSize)
            val inst=WidgetInstance(program);val now=System.currentTimeMillis()
            inst.set("now",Num(now/1000.0));inst.set("currency",Str("USD"));inst.set("interval",Num(240.0));inst.set("interval2",Num(10080.0));inst.set("zoom4h",Num(270.0));inst.set("ichimoku",TRUE)
            for((name,raw) in com.buildapptest.bitcoinpulse.WidgetVisualFixture.data(now)) inst.set(name,Obj(mapOf("ok" to TRUE,"out" to Json.parseOrText(raw))))
            program.triggers.forEach { it.action.run(inst.root,emptyList()) }
            val controller=Robolectric.buildActivity(ComponentActivity::class.java)
            val activity=controller.get();activity.enableEdgeToEdge();controller.setup().visible()
            var size by mutableStateOf(80 to 88)
            activity.setContent { Box(Modifier.fillMaxSize()) { WidgetContent(inst,Modifier.width(size.first.dp).height(size.second.dp),cells=1 to 1) } }
            val decor=activity.window.decorView;val density=context.resources.displayMetrics.density
            fun owner(v:View):SemanticsOwner? {
                if(v.javaClass.name.contains("AndroidComposeView"))return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
                if(v is ViewGroup)for(i in 0 until v.childCount)owner(v.getChildAt(i))?.let { return it }
                return null
            }
            fun nodes()=owner(decor)!!.getAllSemanticsNodes(false).filter { it.config.getOrNull(SemanticsProperties.Text)!=null }
            fun text(n:androidx.compose.ui.semantics.SemanticsNode)=n.config[SemanticsProperties.Text].joinToString { it.text }
            for((w,h) in listOf(80 to 88,120 to 120,160 to 160,200 to 240,220 to 300,250 to 400,320 to 480,320 to 840,200 to 240,80 to 88)) {
                size=w to h;inst.setTile(1,1,w.toDouble(),h.toDouble());androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
                val width=(w*density).toInt();val height=(h*density).toInt()
                val frame=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
                fun settle() { repeat(3) { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));decor.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));decor.layout(0,0,width,height);decor.draw(Canvas(frame)) } }
                fun tap(label:String) {
                    val node=nodes().first { text(it)==label };val b=node.boundsInRoot;val time=SystemClock.uptimeMillis()
                    for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) { val e=MotionEvent.obtain(time,time,action,b.center.x,b.center.y,0);decor.dispatchTouchEvent(e);e.recycle() }
                    androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications();settle()
                }
                settle()
                val mode=when { w>=250 && h>=320->"network";w>=160 && h>=160->"compact";else->"mini" }
                assertEquals(Str(mode),inst.get("viewMode"))
                val source=program.globals.filterIsInstance<Global.Source>().first { it.name=="usd4h" }
                assertEquals(mode=="network",source.arg!!(inst.root)!=Null)
                val labels=nodes().map { text(it) }
                if(mode=="mini")assertTrue(labels.contains("971.890"))
                if(mode=="compact") {
                    assertTrue(labels.contains("Blok #971.890"));assertTrue(labels.contains("3 sat/vB"))
                    assertEquals(h>=240,labels.any { it.contains("Foundry USA") })
                    assertEquals(h>=300,labels.any { it.startsWith("Mempool") })
                    tap("BTC/USD ▾");assertEquals(Str("EUR"),inst.get("currency"));tap("BTC/EUR ▾")
                    tap("↻");assertTrue(inst.actions.contains("refresh"))
                }
                if(mode=="network") {
                    assertTrue(labels.containsAll(listOf("4 uur ▾","1 week ▾","Cloud Aan")))
                    tap("USD ▾");tap("EUR ▾")
                    tap("4 uur ▾");tap("1 dag");tap("1 dag ▾");tap("4 uur")
                    tap("Cloud Aan");tap("Cloud Uit")
                } else for(node in nodes()) {
                    val layouts=mutableListOf<TextLayoutResult>();node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    assertTrue("$asset $w x $h overflow: ${text(node)}",layouts.all { !it.hasVisualOverflow })
                    assertTrue("$asset $w x $h bounds: ${text(node)} ${node.boundsInRoot}",node.boundsInRoot.bottom<=height+1 && node.boundsInRoot.right<=width+1)
                }
                assertEquals(Str("USD"),inst.get("currency"));assertEquals(Num(240.0),inst.get("interval"));assertEquals(Num(10080.0),inst.get("interval2"));assertEquals(Num(270.0),inst.get("zoom4h"));assertEquals(TRUE,inst.get("ichimoku"))
                assertFalse(inst.actions.contains("androidApp"));assertTrue(inst.problems.toString(),inst.problems.isEmpty())
                if(asset.contains("-mini")) {
                    val folder=File("build/reports/adaptive-widget").apply { mkdirs() }
                    File(folder,"owl-$w-$h.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG,100,it) }
                }
            }
            controller.pause().stop().destroy()
        }
    }
}
