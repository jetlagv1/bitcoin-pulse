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
        for((name,id) in listOf("price" to R.id.widget_price,"cloud" to R.id.widget_ichimoku,"first" to R.id.chart_label_1,"second" to R.id.chart_label_2,"block" to R.id.widget_block,"fee" to R.id.widget_next,"mempool" to R.id.widget_mempool,"refresh" to R.id.widget_refresh)) anchors.put(name,bounds(id).exactCenterY()/density)
        File(folder,"android-anchors.json").writeText(anchors.toString(2))
        manager.updateAppWidgetOptions(20,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,360);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,500) })
        WidgetUpdates.render(context)
        val small=shadowOf(manager).getViewFor(20)
        assertEquals(View.VISIBLE,small.findViewById<View>(R.id.chart_header_1).visibility)
        assertEquals(View.VISIBLE,small.findViewById<View>(R.id.chart_header_2).visibility)
    }

    @Test fun bothChartCountsRemainUsableFromFourByFiveToLargerSizes() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        manager.bindAppWidgetIdIfAllowed(21,ComponentName(context,PulseWidget::class.java))
        for(count in 1..2) for(width in listOf(280,320,360)) for(height in listOf(320,400,480,560,640,840)) {
            WidgetCharts(count=count).save(context,21)
            manager.updateAppWidgetOptions(21,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,width);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,height) })
            WidgetUpdates.render(context)
            val root=shadowOf(manager).getViewFor(21) as ViewGroup
            val d=context.resources.displayMetrics.density;val w=(width*d).toInt();val h=(height*d).toInt()
            root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h)
            for(id in listOf(R.id.chart_header_1,R.id.chart_image_1,R.id.widget_block,R.id.widget_refresh)+if(count==2)listOf(R.id.chart_header_2,R.id.chart_image_2)else emptyList()) {
                val v=root.findViewById<View>(id);val r=Rect();v.getDrawingRect(r);root.offsetDescendantRectToMyCoords(v,r)
                assertEquals(View.VISIBLE,v.visibility)
                assertTrue("Clipped $id: $width × $height, count=$count: $r",r.bottom<=h && r.right<=w && r.top>=0)
                if(id==R.id.chart_image_1 || id==R.id.chart_image_2) {
                    assertTrue("Readable plot: $width × $height, count=$count",v.height>=40*d)
                    assertNotNull((v as android.widget.ImageView).drawable)
                }
            }
            if(width==320 && height in listOf(400,480,560)) {
                val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(Canvas(bitmap))
                val folder=File("build/reports/widget-resize").apply { mkdirs() }
                File(folder,"android-$width-$height-$count.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            }
        }
    }
}
