package com.buildapptest.bitcoinpulse

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.buildapptest.bitcoinpulse.data.PulseRepository
import com.buildapptest.bitcoinpulse.ui.PulseScreen
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

/** Asset-export harness, copied into the test source set only during explicit asset capture. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class,qualifiers="w480dp-h853dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StoreAssetCaptureTest {
    @Test fun captureNativeDashboard() {
        val context=RuntimeEnvironment.getApplication()
        val folder=File("../../.tools/android-build/store-capture-data")
        val prefs=context.getSharedPreferences("pulse-data-v1",0).edit()
        folder.listFiles()!!.filter { it.extension=="json" }.forEach { f ->
            prefs.putString(f.nameWithoutExtension,f.readText()).putLong(f.nameWithoutExtension+"-time",File(folder,f.nameWithoutExtension+".time").readText().trim().toLong())
        }
        prefs.putString("currency","EUR").putString("chart-mode","4h").commit()
        val repository=PulseRepository(context)
        val controller=Robolectric.buildActivity(ComponentActivity::class.java)
        controller.get().setTheme(R.style.Theme_BitcoinPulse)
        controller.setup().visible()
        val activity=controller.get()
        activity.setContent { PulseScreen(repository,onRefresh={},onCurrency=repository::setCurrency,onPin={}) }
        shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        val view=activity.window.decorView
        val out=File("build/reports/store-capture").apply {mkdirs()}
        fun capture(name:String) {
            shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(300))
            view.measure(View.MeasureSpec.makeMeasureSpec(1440,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(2560,View.MeasureSpec.EXACTLY))
            view.layout(0,0,1440,2560)
            shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(200))
            val image=Bitmap.createBitmap(1440,2560,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(image))
            File(out,name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
        fun tap(x:Float,y:Float) {
            val time=SystemClock.uptimeMillis()
            for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)) {
                val event=MotionEvent.obtain(time,SystemClock.uptimeMillis(),action,x,y,0)
                view.dispatchTouchEvent(event);event.recycle()
                shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(80))
            }
        }
        fun swipeUp() {
            val time=SystemClock.uptimeMillis()
            for(step in 0..20) {
                val action=when(step){0->MotionEvent.ACTION_DOWN;20->MotionEvent.ACTION_UP;else->MotionEvent.ACTION_MOVE}
                val event=MotionEvent.obtain(time,SystemClock.uptimeMillis(),action,1300f,2200f-step*80,0)
                view.dispatchTouchEvent(event);event.recycle()
                shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(40))
            }
            shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(200))
        }
        capture("01-dashboard.png")
        repository.setChartMode("1d")
        capture("02-dagcandles.png")
        repository.setChartMode("4h")
        tap(1080f,360f)
        capture("03-widgets.png")
        tap(360f,360f)
        swipeUp()
        swipeUp()
        capture("04-netwerkdetails.png")
        controller.pause().stop().destroy()
    }
}
