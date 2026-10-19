package com.buildapptest.bitcoinpulse

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.ImageView
import android.widget.TextView
import com.buildapptest.bitcoinpulse.data.*
import com.buildapptest.bitcoinpulse.widget.WidgetUpdates
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class,qualifiers="mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetChartTest {
    private val now=1792096320000L
    private val candles=(0..35).map { i ->
        val o=94000+i*65.0; val c=o+if(i%3==0)-120 else 180
        Candle(1791576000+i*14400L,o,maxOf(o,c)+80,minOf(o,c)-100,c,20.5,i==35)
    }
    private val s=Snapshot(candles=PERIODS.associate { candleKey("EUR",it.id) to Source(candles,now) })

    private fun layout(mode:String,height:Int,snapshot:Snapshot=s,zooms:Map<String,Int> = emptyMap()):View {
        val context=RuntimeEnvironment.getApplication()
        val views=RemoteViews(context.packageName,R.layout.widget_expanded)
        WidgetUpdates.charts(views,snapshot,mode,320,height,now,zooms=zooms)
        views.setTextViewText(R.id.widget_ichimoku,"Cloud\n${if(snapshot.ichimoku)"Aan" else "Uit"}")
        listOf(R.id.period_4h to "4h",R.id.period_1d to "1d",R.id.period_1w to "1w",R.id.period_combi to "combi").forEach { (id,period) ->
            views.setInt(id,"setBackgroundResource",if(period==mode)R.drawable.widget_period_selected else R.drawable.widget_inner)
            views.setTextColor(id,android.graphics.Color.parseColor(if(period==mode)"#17120D" else "#BAC7DB"))
        }
        views.setTextViewText(R.id.widget_price,"€ 96.420")
        views.setTextViewText(R.id.widget_change,"+2,40% vandaag UTC")
        views.setTextViewText(R.id.widget_block,"Blok 971.613")
        views.setTextViewText(R.id.widget_age,"4 min · 14:32")
        views.setTextViewText(R.id.widget_tx,"4.359")
        views.setTextViewText(R.id.widget_pool,"F2Pool")
        views.setTextViewText(R.id.widget_median,"3,02 sat/vB")
        views.setTextViewText(R.id.widget_next,"3"); views.setTextViewText(R.id.widget_half,"2"); views.setTextViewText(R.id.widget_hour,"1")
        views.setTextViewText(R.id.widget_updated,"Oudste update 14:32 · testdata")
        val parent=FrameLayout(context)
        val root=views.apply(context,parent); parent.addView(root)
        parent.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
        parent.layout(0,0,320,height)
        val image=root.findViewById<ImageView>(R.id.chart_image_1)
        if(image.visibility==View.VISIBLE) assertTrue("Chart must retain readable height: ${image.height}px at $height dp",image.height>=60)
        val bitmap=Bitmap.createBitmap(320,height,Bitmap.Config.ARGB_8888)
        parent.draw(Canvas(bitmap))
        val output=File("build/reports/widget-previews").apply { mkdirs() }
        File(output,"${if(zooms.isNotEmpty())"zoom-" else ""}$mode-$height.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        return root
    }
    @Test fun singleChartFitsMinimumAndContainsActualBitmap() {
        val root=layout("4h",390)
        assertNotNull(root.findViewById<ImageView>(R.id.chart_image_1).drawable)
        assertEquals(View.GONE,root.findViewById<View>(R.id.chart_hint).visibility)
        assertTrue(root.findViewById<TextView>(R.id.chart_label_1).text.contains("4 uur"))
    }
    @Test fun comboUsesThreeIndependentChartsWhenTallEnough() {
        val root=layout("combi",600)
        assertEquals(View.VISIBLE,root.findViewById<View>(R.id.chart_image_3).visibility)
        assertTrue(root.findViewById<TextView>(R.id.chart_label_3).text.contains("1 week"))
    }
    @Test fun comboAtSmallSizeRequestsMoreSpaceInsteadOfClipping() {
        val root=layout("combi",360)
        assertEquals(View.VISIBLE,root.findViewById<View>(R.id.chart_hint).visibility)
        assertEquals(View.GONE,root.findViewById<View>(R.id.chart_image_1).visibility)
    }
    @Test fun failedCandleSourceKeepsItsTimestampAndShowsCache() {
        val cached=s.copy(candles=s.candles.mapValues { it.value.copy(error="Offline") })
        val root=layout("1d",390,cached)
        assertTrue(root.findViewById<TextView>(R.id.chart_label_1).text.contains("cache"))
        assertNotNull(root.findViewById<ImageView>(R.id.chart_image_1).drawable)
    }
    @Test fun emptyHistoryStillInflatesWithAnHonestMissingDataState() {
        val root=layout("1w",390,Snapshot())
        assertTrue(root.findViewById<TextView>(R.id.chart_label_1).text.contains("geen data"))
    }
    @Test fun cloudIsIncludedInEveryCombinationChart() {
        val history=(0 until 240).map { i -> Candle(1791576000+i*14400L,70000.0+i*10,70100.0+i*10,69900.0+i*10,70050.0+i*10,1.0,i==239) }
        val state=s.copy(ichimoku=true,candles=PERIODS.associate { candleKey("EUR",it.id) to Source(history,now) })
        val root=layout("combi",600,state)
        for(id in listOf(R.id.chart_image_1,R.id.chart_image_2,R.id.chart_image_3)) {
            val image=(root.findViewById<ImageView>(id).drawable as android.graphics.drawable.BitmapDrawable).bitmap
            assertTrue("Each Combi chart needs its own green cloud area",cloudFillPixels(image,true)>100)
        }
    }
    @Test fun compactZoomHeadersKeepIndependentCountsAndDisableLimits() {
        val history=(0 until 240).map { i -> Candle(i*14400L,70000.0+i*10,70100.0+i*10,69900.0+i*10,70050.0+i*10,1.0,i==239) }
        val state=s.copy(ichimoku=true,candles=PERIODS.associate { candleKey("EUR",it.id) to Source(history,now) })
        val root=layout("combi",600,state,mapOf("4h" to 24,"1d" to 90,"1w" to 60))
        for((id,count) in listOf(R.id.chart_count_1 to "24",R.id.chart_count_2 to "90",R.id.chart_count_3 to "60"))
            assertEquals(count,root.findViewById<TextView>(id).text.toString())
        assertFalse(root.findViewById<View>(R.id.chart_plus_1).isEnabled)
        assertFalse(root.findViewById<View>(R.id.chart_minus_2).isEnabled)
        val header=root.findViewById<View>(R.id.chart_header_1)
        val button=root.findViewById<View>(R.id.chart_minus_1)
        assertEquals("No separate control row",40,header.height)
        assertEquals(40,button.height)
        assertTrue(button.right<=root.width)
        for(id in listOf(R.id.chart_minus_1,R.id.chart_plus_1,R.id.chart_minus_2,R.id.chart_plus_2,R.id.chart_minus_3,R.id.chart_plus_3)) {
            val view=root.findViewById<View>(id)
            val bounds=android.graphics.Rect();view.getDrawingRect(bounds)
            (root as android.view.ViewGroup).offsetDescendantRectToMyCoords(view,bounds)
            assertTrue("Zoom control clipped: $id at $bounds",bounds.left>=12 && bounds.right<=root.width-12)
        }
    }
}
