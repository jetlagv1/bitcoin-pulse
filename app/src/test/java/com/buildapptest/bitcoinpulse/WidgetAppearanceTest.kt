package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
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
class WidgetAppearanceTest {
    @Test fun productionWidgetUsesSharedVisualFixtureAndApprovedHierarchy() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        manager.bindAppWidgetIdIfAllowed(20,ComponentName(context,PulseWidget::class.java))
        WidgetCharts().save(context,20)
        manager.updateAppWidgetOptions(20,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,360);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,840) })
        WidgetUpdates.render(context)
        val root=shadowOf(manager).getViewFor(20) as ViewGroup
        val density=context.resources.displayMetrics.density;val w=(360*density).toInt();val h=(840*density).toInt()
        root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h)
        fun bounds(id:Int)=Rect().also { r -> root.findViewById<View>(id).getDrawingRect(r);root.offsetDescendantRectToMyCoords(root.findViewById(id),r) }
        assertTrue(bounds(R.id.widget_ichimoku).bottom<=bounds(R.id.chart_header_1).top)
        assertTrue(bounds(R.id.chart_status_2).bottom<bounds(R.id.widget_block).top)
        assertTrue(bounds(R.id.widget_refresh).bottom<=h)
        assertEquals("€ 72.521,90",root.findViewById<TextView>(R.id.widget_price).text.toString())
        assertEquals("Cloud Aan",root.findViewById<TextView>(R.id.widget_ichimoku).text.toString())
        assertEquals("3 sat/vB",root.findViewById<TextView>(R.id.widget_next).text.toString())
        assertEquals("Mempool 18.420 tx · 12,8 vMB",root.findViewById<TextView>(R.id.widget_mempool).text.toString())
        val image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(Canvas(image))
        val folder=File("build/reports/widget-parity").apply { mkdirs() }
        File(folder,"android-360.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
        val anchors=org.json.JSONObject()
        for((name,id) in listOf("price" to R.id.widget_price,"count" to R.id.widget_two_charts,"cloud" to R.id.widget_ichimoku,"first" to R.id.chart_label_1,"second" to R.id.chart_label_2,"block" to R.id.widget_block,"fee" to R.id.widget_next,"mempool" to R.id.widget_mempool,"refresh" to R.id.widget_refresh)) anchors.put(name,bounds(id).exactCenterY()/density)
        File(folder,"android-anchors.json").writeText(anchors.toString(2))
        manager.updateAppWidgetOptions(20,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,360);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,500) })
        WidgetUpdates.render(context)
        val small=shadowOf(manager).getViewFor(20)
        assertNull("Small existing widgets use the compact fallback",small.findViewById<View>(R.id.chart_header_1))
        assertTrue(small.findViewById<TextView>(R.id.widget_updated).text.contains("620"))
    }
}
