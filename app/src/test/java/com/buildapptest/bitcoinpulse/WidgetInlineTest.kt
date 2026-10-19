package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.buildapptest.bitcoinpulse.widget.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=PulseApplication::class,qualifiers="xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetInlineTest {
    @Test fun realWidgetClicksChoosePeriodsWithoutLaunchingAnActivity() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        manager.bindAppWidgetIdIfAllowed(71,ComponentName(context,PulseWidget::class.java))
        manager.bindAppWidgetIdIfAllowed(72,ComponentName(context,PulseWidget::class.java))
        WidgetCharts().save(context,72)
        val d=context.resources.displayMetrics.density
        for(count in 1..2) for(height in listOf(400,480,640,840)) {
            WidgetCharts(count=count).save(context,71)
            context.getSharedPreferences("widget-options",0).edit().remove("picker-71").commit()
            manager.updateAppWidgetOptions(71,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,height) })
            fun render():ViewGroup {
                WidgetUpdates.render(context)
                val root=shadowOf(manager).getViewFor(71) as ViewGroup
                val w=(320*d).toInt();val h=(height*d).toInt()
                root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h)
                val refresh=root.findViewById<View>(R.id.widget_refresh);val r=android.graphics.Rect();refresh.getDrawingRect(r);root.offsetDescendantRectToMyCoords(refresh,r)
                assertTrue("Footer clipped with inline menu at $height, count=$count: $r",r.bottom<=h)
                return root
            }
            fun click(root:View,id:Int) {
                val start=shadowOf(context).broadcastIntents.size
                assertTrue(root.findViewById<View>(id).performClick())
                shadowOf(Looper.getMainLooper()).idle()
                val intent=shadowOf(context).broadcastIntents.drop(start).lastOrNull() ?: error("Widget control must send a broadcast")
                assertEquals(PulseWidget::class.java.name,intent.component?.className)
                assertNull("Period controls must never open the app or a picker activity",shadowOf(context).nextStartedActivity)
            }
            val currency=context.repository.state.value.currency
            val initialCurrency=WidgetCurrency.read(context,71,currency)
            click(render(),R.id.widget_currency)
            val selectedCurrency=if(initialCurrency=="EUR")"USD" else "EUR"
            assertEquals(selectedCurrency,WidgetCurrency.read(context,71,currency))
            assertEquals("$selectedCurrency ▾",render().findViewById<TextView>(R.id.widget_currency).text.toString())
            assertEquals(currency,context.repository.state.value.currency)
            assertEquals(currency,WidgetCurrency.read(context,72,currency))
            assertEquals(setOf("EUR","USD"),WidgetCurrency.required(context,currency))
            click(render(),R.id.widget_currency)
            assertEquals(initialCurrency,WidgetCurrency.read(context,71,currency))
            click(render(),R.id.chart_label_1)
            val open=render()
            assertEquals(View.VISIBLE,open.findViewById<View>(R.id.chart_picker_1).visibility)
            if(count==2) assertFalse(open.findViewById<View>(R.id.chart_pick_1_1d).isEnabled)
            val bitmap=Bitmap.createBitmap((320*d).toInt(),(height*d).toInt(),Bitmap.Config.ARGB_8888);open.draw(Canvas(bitmap))
            val folder=File("build/reports/widget-inline").apply { mkdirs() }
            File(folder,"android-320-$height-$count-open.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            click(open,R.id.chart_pick_1_1w)
            assertEquals("1w",WidgetCharts.read(context,71).first)
            val chosen=render()
            assertEquals(View.GONE,chosen.findViewById<View>(R.id.chart_picker_1).visibility)
            assertEquals("1 week ▾",chosen.findViewById<TextView>(R.id.chart_label_1).text.toString())
            if(count==2) {
                click(chosen,R.id.chart_label_2);val second=render()
                assertFalse(second.findViewById<View>(R.id.chart_pick_2_1w).isEnabled)
                click(second,R.id.chart_pick_2_4h)
                assertEquals("4h",WidgetCharts.read(context,71).second)
            }
            val saved=WidgetCharts.read(context,71)
            for(slot in 0 until count) {
                val minus=if(slot==0)R.id.chart_minus_1 else R.id.chart_minus_2
                val plus=if(slot==0)R.id.chart_plus_1 else R.id.chart_plus_2
                val period=if(slot==0)saved.first else saved.second
                for(target in listOf(60,90,120,180,270)) {
                    click(render(),minus)
                    assertEquals(target,WidgetUpdates.zooms(context,71).getValue(period))
                }
                assertFalse(render().findViewById<View>(minus).isEnabled)
                for(target in listOf(180,120,90,60,36)) {
                    click(render(),plus)
                    assertEquals(target,WidgetUpdates.zooms(context,71).getValue(period))
                }
            }
            click(render(),R.id.chart_label_1)
            click(render(),R.id.widget_graph_count)
            assertEquals(3-count,WidgetCharts.read(context,71).count)
            assertEquals(saved.first,WidgetCharts.read(context,71).first)
            assertEquals(saved.second,WidgetCharts.read(context,71).second)
            assertEquals(-1,context.getSharedPreferences("widget-options",0).getInt("picker-71",-1))
            click(render(),R.id.widget_graph_count)
            assertEquals("Switching back restores both interval choices",saved,WidgetCharts.read(context,71))
            assertEquals("Other widget counts remain independent",WidgetCharts(),WidgetCharts.read(context,72))
        }
    }
}
