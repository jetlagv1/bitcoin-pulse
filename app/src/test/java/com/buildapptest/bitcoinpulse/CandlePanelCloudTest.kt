package com.buildapptest.bitcoinpulse

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import com.buildapptest.bitcoinpulse.data.PERIODS
import com.buildapptest.bitcoinpulse.ui.CandlePanel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=PulseApplication::class,qualifiers="w390dp-h900dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CandlePanelCloudTest {
    @Test fun oneAccessibleSwitchWorksForSingleAndAllCombinationCharts() {
        val app=RuntimeEnvironment.getApplication() as PulseApplication
        val now=System.currentTimeMillis()
        val prefs=app.getSharedPreferences("pulse-data-v1",0).edit().clear()
        for(period in PERIODS) {
            val rows=(0..239).joinToString(",") { i -> "[${1700000000L+i*period.minutes*60L},${1000+i},${1010+i},${990+i},${1001+i},${1000+i},1,1]" }
            prefs.putString("candles-EUR-${period.id}","""{"error":[],"result":{"XXBTZEUR":[$rows]}}""").putLong("candles-EUR-${period.id}-time",now)
        }
        prefs.commit()
        val repository=app.repository
        val controller=Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val activity=controller.get()
        activity.setContent {
            val s by repository.state.collectAsState()
            MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xffff8a1f),onPrimary=Color(0xff17120d),onSurface=Color(0xfff7faff),secondaryContainer=Color(0xff243246))) {
                Surface(color=Color(0xff182230),contentColor=Color(0xfff7faff)) {
                    Column(Modifier.verticalScroll(rememberScrollState())) { CandlePanel(s,repository,now,detailed=false) }
                }
            }
        }
        fun settle() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(350))
        settle()
        fun owner(v: View): SemanticsOwner? {
            if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
            if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
            return null
        }
        val semantics=owner(activity.window.decorView)!!
        fun nodes()=semantics.getAllSemanticsNodes(false)
        fun switch()=nodes().single { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains("Ichimoku cloud voor alle grafieken")==true && it.config.getOrNull(SemanticsActions.OnClick)!=null }
        assertFalse(repository.state.value.ichimoku)
        assertTrue(switch().config[SemanticsActions.OnClick].action!!.invoke())
        settle();assertTrue(repository.state.value.ichimoku)
        repository.setChartMode("combi");settle()
        val images=nodes().filter { it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { text -> text.contains("-candles in EUR") }==true }
        assertEquals(3,images.size)
        assertTrue(repository.state.value.ichimoku)
        // Send real multi-pointer Android events through the scrollable Compose panel.
        val bounds=images.first().boundsInRoot
        val decor=activity.window.decorView
        val cx=bounds.center.x;val cy=bounds.center.y
        val start=SystemClock.uptimeMillis()
        fun touch(action: Int, spread: Float, count: Int=2) {
            val props=Array(count) { i -> MotionEvent.PointerProperties().apply { id=i;toolType=MotionEvent.TOOL_TYPE_FINGER } }
            val coords=Array(count) { i -> MotionEvent.PointerCoords().apply { x=cx+(if(i==0)-spread else spread);y=cy;pressure=1f;size=1f } }
            val event=MotionEvent.obtain(start,SystemClock.uptimeMillis(),action,count,props,coords,0,0,1f,1f,0,0,android.view.InputDevice.SOURCE_TOUCHSCREEN,0)
            decor.dispatchTouchEvent(event);event.recycle();settle()
        }
        touch(MotionEvent.ACTION_DOWN,40f,1)
        touch(MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),40f)
        touch(MotionEvent.ACTION_MOVE,70f)
        touch(MotionEvent.ACTION_MOVE,100f)
        touch(MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),100f)
        touch(MotionEvent.ACTION_UP,100f,1)
        assertEquals("Spreading fingers zooms in",24,repository.state.value.chartZoom["4h"])
        assertEquals("Other Combi graph is independent",36,repository.state.value.chartZoom["1d"])
        touch(MotionEvent.ACTION_DOWN,100f,1)
        touch(MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),100f)
        touch(MotionEvent.ACTION_MOVE,60f)
        touch(MotionEvent.ACTION_MOVE,40f)
        touch(MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),40f)
        touch(MotionEvent.ACTION_UP,40f,1)
        assertEquals("Bringing fingers together zooms out from the last viewport",60,repository.state.value.chartZoom["4h"])
        val actions=nodes().first { it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { text -> text.startsWith("1 dag-candles") }==true }.config[SemanticsActions.CustomActions]
        assertTrue(actions.single { it.label=="Uitzoomen" }.action())
        settle();assertEquals(60,repository.state.value.chartZoom["1d"])
        val image=android.graphics.Bitmap.createBitmap(decor.width,decor.height,android.graphics.Bitmap.Config.ARGB_8888)
        decor.draw(android.graphics.Canvas(image))
        val folder=java.io.File("build/reports/zoom-native").apply { mkdirs() }
        java.io.File(folder,"app-combi.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        assertTrue(switch().config[SemanticsActions.OnClick].action!!.invoke())
        settle();assertFalse(repository.state.value.ichimoku)
        controller.pause().stop().destroy()
    }
}
