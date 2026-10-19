package com.buildapptest.bitcoinpulse

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.buildapptest.bitcoinpulse.data.*
import com.buildapptest.bitcoinpulse.ui.CandleBitmap
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35], application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IchimokuRenderTest {
    private val history=(0 until 240).map { i -> Candle(i*14400L,70000.0+i*10,70100.0+i*10,69900.0+i*10,70050.0+i*10,1.0,i==239) }
    private fun capture(name: String, chart: Bitmap) {
        val image=Bitmap.createBitmap(chart.width,chart.height,Bitmap.Config.ARGB_8888)
        Canvas(image).apply { drawColor(Color.parseColor("#182230"));drawBitmap(chart,0f,0f,null) }
        val folder=File("build/reports/ichimoku-fill").apply { mkdirs() }
        File(folder,"$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun enabledCloudAddsTranslucentFillAndDisabledRetainsOriginalChart() {
        val off=CandleBitmap.render(history,"EUR",640,320)
        val on=CandleBitmap.render(history,"EUR",640,320,ichimoku=true)
        assertFalse(off.sameAs(on))
        assertTrue("Rising cloud must contain translucent green area",cloudFillPixels(on,true)>1000)
        capture("rising",on)
        assertTrue(off.sameAs(CandleBitmap.render(history.takeLast(36),"EUR",640,320)))
    }
    @Test fun fallingCloudContainsTranslucentRedArea() {
        val falling=history.reversed().mapIndexed { i,c -> c.copy(timestamp=i*14400L,provisional=i==239) }
        val image=CandleBitmap.render(falling,"EUR",640,320,ichimoku=true)
        assertTrue("Falling cloud must contain translucent red area",cloudFillPixels(image,false)>1000)
        capture("falling",image)
    }
    @Test fun insufficientHistoryShowsCandlesWithoutInventingACloud() {
        val short=history.takeLast(36)
        assertTrue(CandleBitmap.render(short,"EUR",640,320).sameAs(CandleBitmap.render(short,"EUR",640,320,ichimoku=true)))
    }
    @Test fun appSettingSurvivesRepositoryRecreation() {
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("pulse-data-v1",0).edit().clear().commit()
        assertFalse(PulseRepository(context).state.value.ichimoku)
        PulseRepository(context).setIchimoku(true)
        assertTrue(PulseRepository(context).state.value.ichimoku)
        PulseRepository(context).setIchimoku(false)
        assertFalse(PulseRepository(context).state.value.ichimoku)
    }
}
