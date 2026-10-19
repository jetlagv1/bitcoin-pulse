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
class OwlResizeTest {
    @Test fun denseWidgetsFitFourByFiveAndScaleToFourBySixWithWorkingControls() {
        val context=RuntimeEnvironment.getApplication()
        val program=Compiler.compile(context.assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() })
        for(count in 1..2) for(heightDp in listOf(400,480,560)) {
            val inst=WidgetInstance(program);val now=System.currentTimeMillis()
            inst.setTile(4,if(heightDp==400)5 else 6,320.0,heightDp.toDouble())
            inst.set("now",Num(now/1000.0));inst.set("ichimoku",TRUE);inst.set("graphCount",Num(count.toDouble()))
            for((name,raw) in com.buildapptest.bitcoinpulse.WidgetVisualFixture.data(now)) inst.set(name,Obj(mapOf("ok" to TRUE,"out" to Json.parseOrText(raw))))
            program.triggers.forEach { it.action.run(inst.root,emptyList()) }
            val control=Robolectric.buildActivity(ComponentActivity::class.java);val activity=control.get();activity.enableEdgeToEdge();control.setup().visible()
            activity.setContent { Box(Modifier.fillMaxSize()) { WidgetContent(inst,Modifier.width(320.dp).height(heightDp.dp),cells=4 to if(heightDp==400)5 else 6) } }
            val d=context.resources.displayMetrics.density;val w=(320*d).toInt();val h=(heightDp*d).toInt();val decor=activity.window.decorView
            val frame=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
            fun settle() { repeat(4) { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));decor.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));decor.layout(0,0,w,h);shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100));decor.draw(Canvas(frame)) } }
            fun owner(v:View):SemanticsOwner? {
                if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
                if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
                return null
            }
            fun nodes()=owner(decor)!!.getAllSemanticsNodes(false).filter { it.config.getOrNull(SemanticsProperties.Text)!=null }
            fun text(n:androidx.compose.ui.semantics.SemanticsNode)=n.config.getOrNull(SemanticsProperties.Text)!!.joinToString { it.text }
            fun tap(label:String) {
                val b=(nodes().firstOrNull { text(it)==label } ?: error("Missing '$label': ${nodes().map { text(it) }}")).boundsInRoot;val time=SystemClock.uptimeMillis()
                for(a in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) { val e=MotionEvent.obtain(time,time,a,b.center.x,b.center.y,0);decor.dispatchTouchEvent(e);e.recycle() }
                androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
                settle()
            }
            settle()
            assertEquals("tile=${inst.tile("tileHeight")}, dense=${inst.get("dense")}",TRUE,inst.get("dense"))
            assertTrue(inst.problems.toString(),inst.problems.isEmpty())
            assertEquals(count,nodes().count { text(it) in listOf("4 uur ▾","1 dag ▾","1 week ▾") })
            assertFalse(nodes().any { text(it) in listOf("1 grafiek","2 grafieken") })
            for(n in nodes()) assertTrue("$count charts at $heightDp dp clipped: ${text(n)} ${n.boundsInRoot}",n.boundsInRoot.bottom<=h+1)
            val folder=File("build/reports/widget-resize").apply { mkdirs() }
            File(folder,"omalauncher-320-$heightDp-$count.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG,100,it) }
            tap("−");assertEquals(Num(60.0),inst.get("zoom4h"))
            tap("+");assertEquals(Num(36.0),inst.get("zoom4h"))
            tap("Cloud Aan");assertEquals(FALSE,inst.get("ichimoku"))
            tap("4 uur ▾");assertEquals("Picker must open",Num(0.0),inst.get("picker"));tap("1 week");assertEquals(Num(10080.0),inst.get("interval"))
            if(count==2) { tap("1 dag ▾");tap("4 uur");assertEquals(Num(240.0),inst.get("interval2")) }
            tap("↻");assertTrue(inst.actions.contains("refresh"))
            assertTrue(inst.problems.toString(),inst.problems.isEmpty())
            control.pause().stop().destroy()
        }
    }
}
