package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.LinearLayout
import android.widget.RadioButton
import com.buildapptest.bitcoinpulse.data.*
import com.buildapptest.bitcoinpulse.widget.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=PulseApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetSelectionTest {
    private fun setup() = (RuntimeEnvironment.getApplication() as PulseApplication).also { context ->
        context.getSharedPreferences("widget-options",0).edit().clear().commit()
        for(id in listOf(10,11)) AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(id,ComponentName(context,PulseWidget::class.java))
    }
    @Test fun legacyThreeChartsMigrateToTwoAndIgnoreLaterAppChanges() {
        val context=setup()
        val app=Snapshot(chartMode="combi")
        val migrated=WidgetCharts.read(context,10,app)
        assertEquals(listOf("4h","1d"),migrated.periods.map { it.id })
        migrated.save(context,10)
        assertEquals(migrated,WidgetCharts.read(context,10,app.copy(chartMode="1w")))
        assertEquals(3,app.chartPeriods().size)
    }
    @Test fun choicesPersistPerWidgetAndRememberSecondSlotAcrossCounts() {
        val context=setup();WidgetCharts().save(context,10);WidgetCharts().save(context,11)
        assertTrue(WidgetCharts.select(context,10,1,"1w"))
        assertFalse(WidgetCharts.select(context,10,0,"1w"))
        assertFalse(WidgetCharts.select(context,10,2,"1d"))
        assertFalse(WidgetCharts.select(context,999,0,"1d"))
        assertFalse(WidgetCharts.setCount(context,10,3))
        assertTrue(WidgetCharts.setCount(context,10,1))
        assertEquals(listOf("4h"),WidgetCharts.read(context,10).periods.map { it.id })
        assertTrue(WidgetCharts.setCount(context,10,2))
        assertEquals(listOf("4h","1w"),WidgetCharts.read(context,10).periods.map { it.id })
        assertEquals(listOf("4h","1d"),WidgetCharts.read(context,11).periods.map { it.id })
        PulseWidget().onDeleted(context,intArrayOf(10))
        assertFalse(context.getSharedPreferences("widget-options",0).contains("charts-10"))
    }
    @Test fun nativePickerDisablesDuplicateAndUpdatesOnlyItsWidget() {
        val context=setup();WidgetCharts().save(context,10);WidgetCharts().save(context,11)
        val intent=Intent(context,WidgetChartPickerActivity::class.java).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,10).putExtra("slot",1)
        val controller=Robolectric.buildActivity(WidgetChartPickerActivity::class.java,intent).setup()
        val root=controller.get().findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0) as LinearLayout
        val radios=(0 until root.childCount).map { root.getChildAt(it) }.filterIsInstance<RadioButton>()
        assertFalse(radios.single { it.text=="4 uur" }.isEnabled)
        val decor=controller.get().window.decorView
        decor.measure(android.view.View.MeasureSpec.makeMeasureSpec(320,android.view.View.MeasureSpec.EXACTLY),android.view.View.MeasureSpec.makeMeasureSpec(400,android.view.View.MeasureSpec.AT_MOST))
        decor.layout(0,0,decor.measuredWidth,decor.measuredHeight)
        val bitmap=android.graphics.Bitmap.createBitmap(decor.width,decor.height,android.graphics.Bitmap.Config.ARGB_8888)
        decor.draw(android.graphics.Canvas(bitmap))
        val folder=java.io.File("build/reports/widget-previews").apply { mkdirs() }
        java.io.File(folder,"interval-picker.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        radios.single { it.text=="1 week" }.performClick()
        assertEquals("1w",WidgetCharts.read(context,10).second)
        assertEquals("1d",WidgetCharts.read(context,11).second)
        assertTrue(controller.get().isFinishing)
        controller.pause().stop().destroy()
    }
    @Test fun appCountUpdatesExistingWidgetsAndNewDefaultsWithoutChangingPeriods() {
        val context=setup();WidgetCharts("1w","4h",2).save(context,10)
        WidgetCharts.configureCount(context,1)
        assertEquals(WidgetCharts("1w","4h",1),WidgetCharts.read(context,10))
        assertEquals(1,WidgetCharts.read(context,11).count)
        assertEquals(1,WidgetCharts.read(context,999).count)
        WidgetCharts.configureCount(context,2)
        assertEquals(listOf("1w","4h"),WidgetCharts.read(context,10).periods.map { it.id })
        val owl=context.assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() }
        assertTrue(Omalauncher.configuredSource(owl,1).contains("state graphCount = 1 persist"))
        assertTrue(Omalauncher.configuredSource(owl,2).contains("state graphCount = 2 persist"))
    }
}
