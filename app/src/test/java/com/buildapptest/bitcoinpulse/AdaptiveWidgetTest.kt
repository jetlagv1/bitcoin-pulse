package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
class AdaptiveWidgetTest {
    private fun layout(context:Context,root:View,w:Int,h:Int) {
        val d=context.resources.displayMetrics.density
        root.measure(View.MeasureSpec.makeMeasureSpec((w*d).toInt(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec((h*d).toInt(),View.MeasureSpec.EXACTLY));root.layout(0,0,(w*d).toInt(),(h*d).toInt())
    }
    private fun checkText(root:ViewGroup) {
        fun walk(v:View) {
            if(v.visibility!=View.VISIBLE)return
            if(v is TextView && v.text.isNotEmpty()) {
                val r=Rect();v.getDrawingRect(r);root.offsetDescendantRectToMyCoords(v,r)
                assertTrue("Clipped ${v.text}: $r of ${root.width}x${root.height}",r.top>=0 && r.bottom<=root.height+1 && r.right<=root.width+1)
                assertFalse("Overflow: ${v.text}",v.layout?.let { (0 until it.lineCount).any { i->it.getEllipsisCount(i)>0 } } ?: true)
            }
            if(v is ViewGroup)for(i in 0 until v.childCount)walk(v.getChildAt(i))
        }
        walk(root)
    }
    @Test fun allStartTypesGrowAndShrinkWithoutLosingSettingsOrControls() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        for((index,provider) in listOf(MiniWidget::class.java,CompactWidget::class.java,PulseWidget::class.java).withIndex()) {
            val id=100+index
            manager.bindAppWidgetIdIfAllowed(id,ComponentName(context,provider))
            assertTrue(WidgetCurrency.set(context,id,"USD"))
            WidgetCharts("4h","1w",2).save(context,id)
            context.getSharedPreferences("widget-options",0).edit().putBoolean("ichimoku-$id",true).putInt("zoom-$id-4h",270).apply()
            for((w,h) in listOf(80 to 88,120 to 120,160 to 160,200 to 240,220 to 300,250 to 400,320 to 480,320 to 840,200 to 240,80 to 88)) {
                manager.updateAppWidgetOptions(id,Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,w);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,h) })
                WidgetUpdates.render(context)
                var root=shadowOf(manager).getViewFor(id) as ViewGroup;layout(context,root,w,h)
                when(WidgetSizing.mode(w,h)) {
                    WidgetMode.MINI -> assertEquals("971.890",root.findViewById<TextView>(R.id.mini_height).text.toString())
                    WidgetMode.COMPACT -> {
                        assertEquals("Blok #971.890",root.findViewById<TextView>(R.id.compact_block).text.toString())
                        assertEquals(if(h>=240)View.VISIBLE else View.GONE,root.findViewById<View>(R.id.compact_age).visibility)
                        assertEquals(if(h>=300)View.VISIBLE else View.GONE,root.findViewById<View>(R.id.compact_mempool).visibility)
                        assertTrue(root.findViewById<View>(R.id.widget_refresh).height>=48*context.resources.displayMetrics.density)
                    }
                    WidgetMode.NETWORK -> {
                        assertEquals("4 uur ▾",root.findViewById<TextView>(R.id.chart_label_1).text.toString())
                        assertEquals("1 week ▾",root.findViewById<TextView>(R.id.chart_label_2).text.toString())
                        assertEquals("Cloud Aan",root.findViewById<TextView>(R.id.widget_ichimoku).text.toString())
                        if(h==480) {
                            assertTrue(root.findViewById<View>(R.id.widget_currency).performClick())
                            val broadcast=shadowOf(context).broadcastIntents.last { it.action==WidgetUpdates.CURRENCY }
                            PulseWidget().onReceive(context,broadcast)
                            assertEquals("EUR",WidgetCurrency.read(context,id,"USD"));WidgetCurrency.set(context,id,"USD")
                            assertTrue(WidgetUpdates.changeZoom(context,id,"4h",false));assertTrue(WidgetUpdates.changeZoom(context,id,"4h",true))
                            assertTrue(WidgetCharts.select(context,id,0,"1d"));assertTrue(WidgetCharts.select(context,id,0,"4h"))
                        }
                    }
                }
                if(WidgetSizing.mode(w,h)==WidgetMode.NETWORK) {
                    WidgetUpdates.render(context);root=shadowOf(manager).getViewFor(id) as ViewGroup;layout(context,root,w,h)
                    assertEquals("USD ▾",root.findViewById<TextView>(R.id.widget_currency).text.toString())
                } else checkText(root)
                assertEquals("USD",WidgetCurrency.read(context,id,"EUR"));assertEquals(WidgetCharts("4h","1w",2),WidgetCharts.read(context,id))
                assertEquals(270,WidgetUpdates.zooms(context,id).getValue("4h"))
                if(index==0) {
                    val image=Bitmap.createBitmap(root.width,root.height,Bitmap.Config.ARGB_8888);root.draw(Canvas(image))
                    val folder=File("build/reports/adaptive-widget").apply { mkdirs() }
                    File(folder,"android-$w-$h.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
                }
            }
        }
    }
    @Test fun compactContentFitsLargeSystemFontsAndLongValues() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val scaled=context.createConfigurationContext(Configuration(context.resources.configuration).apply { fontScale=1.5f })
        val s=context.repository.state.value
        for((w,h) in listOf(160 to 160,160 to 240,200 to 300,240 to 319)) {
            val root=CompactViews.views(scaled,-1,s,w,h,System.currentTimeMillis()).apply(scaled,FrameLayout(scaled)) as ViewGroup
            layout(scaled,root,w,h);checkText(root)
        }
    }
    @Test fun launcherSizesSelectRealLayoutsAndFetchHistoriesForEveryNetworkOrientation() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        manager.bindAppWidgetIdIfAllowed(150,ComponentName(context,MiniWidget::class.java))
        WidgetCurrency.set(context,150,"USD")
        val sizes=arrayListOf(android.util.SizeF(80f,88f),android.util.SizeF(200f,240f),android.util.SizeF(320f,480f))
        manager.updateAppWidgetOptions(150,Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,80);putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,88)
            putParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES,sizes)
        })
        assertEquals(sizes,WidgetSizing.sizes(context,150));assertTrue(WidgetSizing.hasNetworkSize(context,150))
        assertEquals(setOf("EUR","USD"),WidgetCurrency.required(context,"EUR"))
        WidgetUpdates.render(context)
        val remote=WidgetUpdates.viewsForWidget(context,150,MiniWidget::class.java,context.repository.state.value.copy(currency="USD"),System.currentTimeMillis())
        val choose=android.widget.RemoteViews::class.java.getMethod("getRemoteViewsToApply",Context::class.java,android.util.SizeF::class.java)
        for((size,expectedId) in sizes.zip(listOf(R.id.mini_height,R.id.compact_block,R.id.chart_label_1))) {
            val selected=choose.invoke(remote,context,size) as android.widget.RemoteViews
            val root=selected.apply(context,FrameLayout(context))
            assertNotNull("Framework must choose the matching layout at $size",root.findViewById<View>(expectedId))
        }
        assertEquals("USD",WidgetCurrency.read(context,150,"EUR"))
        assertEquals(WidgetMode.COMPACT,WidgetSizing.mode(249,400));assertEquals(WidgetMode.NETWORK,WidgetSizing.mode(250,320))
        manager.updateAppWidgetOptions(150,Bundle().apply { putParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES,arrayListOf(android.util.SizeF(80f,88f))) })
        assertEquals(setOf("EUR"),WidgetCurrency.required(context,"EUR"))
    }

}
