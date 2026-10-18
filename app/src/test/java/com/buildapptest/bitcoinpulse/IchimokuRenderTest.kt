package com.buildapptest.bitcoinpulse

import android.app.Application
import com.buildapptest.bitcoinpulse.data.*
import com.buildapptest.bitcoinpulse.ui.CandleBitmap
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35], application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IchimokuRenderTest {
    private val history=(0 until 240).map { i -> Candle(i*14400L,70000.0+i*10,70100.0+i*10,69900.0+i*10,70050.0+i*10,1.0,i==239) }
    @Test fun enabledCloudAddsTranslucentFillAndDisabledRetainsOriginalChart() {
        val off=CandleBitmap.render(history,"EUR",640,320)
        val on=CandleBitmap.render(history,"EUR",640,320,ichimoku=true)
        assertFalse(off.sameAs(on))
        var translucent=0
        for(y in 0 until on.height) for(x in 0 until on.width) if(android.graphics.Color.alpha(on.getPixel(x,y)) in 60..75)translucent++
        assertTrue("Cloud fill must be translucent",translucent>100)
        assertTrue(off.sameAs(CandleBitmap.render(history.takeLast(36),"EUR",640,320)))
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
