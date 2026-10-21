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
class OwlMiniTest {
    @Test fun tinyLayoutsFitAndRealTapsChangeOnlyTheirOwnInstance() {
        val context=RuntimeEnvironment.getApplication()
        val source=context.assets.open("bitcoin-pulse-mini.owl").bufferedReader().use { it.readText() }
        val now=System.currentTimeMillis()
        fun instance(code: String): WidgetInstance {
            val inst=WidgetInstance(Compiler.compile(Omalauncher.configuredMiniSource(source,code)))
            inst.set("now",Num(now/1000.0))
            for((name,raw) in com.buildapptest.bitcoinpulse.WidgetVisualFixture.data(now).filterKeys { it in listOf("ticker","blocks","fees") })
                inst.set(name,Obj(mapOf("ok" to TRUE,"out" to Json.parseOrText(raw))))
            inst.program.triggers.forEach { it.action.run(inst.root,emptyList()) }
            return inst
        }
        val other=instance("USD")
        assertEquals(Str("$ 78,3k"),other.get("miniPriceText"))
        for((w,h) in listOf(40 to 40,56 to 56,64 to 64,80 to 72,80 to 88,120 to 120)) {
            val inst=instance("EUR");inst.setTile(1,1,w.toDouble(),h.toDouble())
            val control=Robolectric.buildActivity(ComponentActivity::class.java)
            val activity=control.get();activity.enableEdgeToEdge();control.setup().visible()
            activity.setContent { Box(Modifier.fillMaxSize()) { WidgetContent(inst,Modifier.width(w.dp).height(h.dp),cells=1 to 1) } }
            val d=context.resources.displayMetrics.density;val width=(w*d).toInt();val height=(h*d).toInt();val decor=activity.window.decorView
            val frame=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            fun settle() { repeat(3) {
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250))
                decor.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));decor.layout(0,0,width,height);decor.draw(Canvas(frame))
            } }
            fun owner(v:View):SemanticsOwner? {
                if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
                if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
                return null
            }
            fun nodes()=owner(decor)!!.getAllSemanticsNodes(false).filter { it.config.getOrNull(SemanticsProperties.Text)!=null }
            fun labels()=nodes().flatMap { it.config[SemanticsProperties.Text].map { t->t.text } }
            settle()
            assertTrue(labels().containsAll(listOf("971.890","3 sat/vB")))
            assertEquals(h>=56,labels().contains("Blok"))
            assertEquals(w>=76 && h>=80,labels().contains("€ 72,5k"))
            for(n in nodes()) {
                val layouts=mutableListOf<TextLayoutResult>();n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                assertTrue("Overflow at $w x $h: ${n.config[SemanticsProperties.Text]}",layouts.all { !it.hasVisualOverflow })
                assertTrue(n.boundsInRoot.top>=0 && n.boundsInRoot.bottom<=height+1 && n.boundsInRoot.right<=width+1)
            }
            val folder=File("build/reports/miniwidget").apply { mkdirs() }
            File(folder,"owl-EUR-$w-$h.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG,100,it) }
            val time=SystemClock.uptimeMillis()
            for(a in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) { val e=MotionEvent.obtain(time,time,a,width/2f,height/2f,0);decor.dispatchTouchEvent(e);e.recycle() }
            androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications();settle()
            assertEquals(Str("USD"),inst.get("currency"));assertEquals(Str("USD"),other.get("currency"))
            assertEquals(Str("$ 78,3k"),inst.get("miniPriceText"))
            inst.set("currency",Str("EUR"));assertEquals(Str("USD"),other.get("currency"))
            val cached=inst.get("block")
            inst.set("blocks",Obj(mapOf("ok" to FALSE)));inst.program.triggers.forEach { it.action.run(inst.root,emptyList()) }
            assertEquals(cached,inst.get("block"));assertEquals(TRUE,inst.get("oldBlock"))
            assertTrue("Runtime errors: ${inst.problems}",inst.problems.isEmpty());assertTrue(inst.actions.isEmpty())
            control.pause().stop().destroy()
        }
        val empty=WidgetInstance(Compiler.compile(source));empty.setTile(1,1,80.0,88.0)
        assertEquals(Str("—"),empty.get("heightText"));assertEquals(Str("— sat/vB"),empty.get("feeText"));assertEquals(Str("€ —"),empty.get("miniPriceText"))
        assertTrue(empty.problems.isEmpty())
    }
}
