package com.buildapptest.bitcoinpulse

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import com.buildapptest.bitcoinpulse.data.*
import com.buildapptest.bitcoinpulse.ui.CandleBitmap
import com.buildapptest.bitcoinpulse.widget.PulseWidget
import com.buildapptest.bitcoinpulse.widget.WidgetUpdates
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChartZoomTest {
    @Test fun pinchAndButtonsHaveConsistentDirectionAndBounds() {
        assertEquals(24f,ChartZoom.pinch(36f,2f),0f)
        assertEquals(72f,ChartZoom.pinch(36f,.5f),0f)
        assertEquals(270f,ChartZoom.pinch(72f,.1f),0f)
        assertEquals(36f,ChartZoom.pinch(36f,Float.NaN),0f)
        assertEquals(60,ChartZoom.step(36,true));assertEquals(24,ChartZoom.step(36,false))
        assertEquals(24,ChartZoom.step(24,false));assertEquals(120,ChartZoom.step(90,true));assertEquals(270,ChartZoom.step(270,true))
        assertEquals(0,ChartZoom.visible(90,0));assertEquals(17,ChartZoom.visible(90,17))
    }
    @Test fun everyZoomRetainsCloudFillUsingFullHistory() {
        val history=(0 until 480).map { i -> Candle(i*14400L,70000.0+i*10,70100.0+i*10,69900.0+i*10,70050.0+i*10,1.0,i==479) }
        for(count in ChartZoom.steps) {
            val on=CandleBitmap.render(history,"EUR",640,320,ichimoku=true,visibleCount=count)
            assertTrue("Filled cloud at $count candles",cloudFillPixels(on,true)>500)
            val off=CandleBitmap.render(history,"EUR",640,320,visibleCount=count)
            assertTrue("Off viewport must be the last $count real candles",off.sameAs(CandleBitmap.render(history.takeLast(count),"EUR",640,320,visibleCount=count)))
        }
        assertEquals(238,Ichimoku.required(90));assertEquals(418,Ichimoku.required(270))
        val short=history.takeLast(200)
        assertTrue(CandleBitmap.render(short,"EUR",640,320,visibleCount=90).sameAs(CandleBitmap.render(short,"EUR",640,320,ichimoku=true,visibleCount=90)))
    }
    @Test fun appZoomPersistsPerPeriod() {
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("pulse-data-v1",0).edit().clear().commit()
        PulseRepository(context).apply { setChartZoom("4h",60);setChartZoom("1d",24) }
        val restored=PulseRepository(context).state.value
        assertEquals(60,restored.chartZoom["4h"]);assertEquals(24,restored.chartZoom["1d"])
        assertEquals(36,restored.chartZoom["1w"])
    }
    @Test fun widgetZoomIsOwnedIndependentAndDeletedWithWidget() {
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("widget-options",0).edit().clear().commit()
        val manager=AppWidgetManager.getInstance(context)
        manager.bindAppWidgetIdIfAllowed(10,ComponentName(context,PulseWidget::class.java))
        manager.bindAppWidgetIdIfAllowed(11,ComponentName(context,PulseWidget::class.java))
        assertTrue(WidgetUpdates.changeZoom(context,10,"4h",true))
        assertEquals(60,WidgetUpdates.zooms(context,10)["4h"])
        assertEquals(36,WidgetUpdates.zooms(context,10)["1d"])
        assertEquals(36,WidgetUpdates.zooms(context,11)["4h"])
        assertFalse(WidgetUpdates.changeZoom(context,99,"4h",true))
        assertFalse(WidgetUpdates.changeZoom(context,10,"bogus",true))
        PulseWidget().onDeleted(context,intArrayOf(10))
        assertEquals(36,WidgetUpdates.zooms(context,10)["4h"])
    }
}
