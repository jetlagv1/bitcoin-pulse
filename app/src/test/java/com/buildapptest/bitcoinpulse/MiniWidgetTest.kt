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
import com.buildapptest.bitcoinpulse.data.Snapshot
import com.buildapptest.bitcoinpulse.data.Source
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
class MiniWidgetTest {
    private fun render(context: Context,s: Snapshot,w: Int,h: Int,id: Int=AppWidgetManager.INVALID_APPWIDGET_ID): ViewGroup {
        val root=MiniWidget.views(context,id,s,w,h,System.currentTimeMillis()).apply(context,FrameLayout(context)) as ViewGroup
        val d=context.resources.displayMetrics.density
        root.measure(View.MeasureSpec.makeMeasureSpec((w*d).toInt(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec((h*d).toInt(),View.MeasureSpec.EXACTLY))
        root.layout(0,0,(w*d).toInt(),(h*d).toInt())
        return root
    }
    @Test fun bothCurrenciesFitSmallTilesWithoutLosingHeightOrFee() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val snapshot=context.repository.state.value
        for(code in listOf("EUR","USD")) for((w,h) in listOf(40 to 40,56 to 56,64 to 64,80 to 72,80 to 88,120 to 120)) {
            val s=snapshot.copy(currency=code)
            val root=render(context,s,w,h)
            assertEquals("971.890",root.findViewById<TextView>(R.id.mini_height).text.toString())
            assertEquals("3 sat/vB",root.findViewById<TextView>(R.id.mini_fee).text.toString())
            assertEquals(if(w>=76 && h>=80)View.VISIBLE else View.GONE,root.findViewById<View>(R.id.mini_price).visibility)
            for(id in listOf(R.id.mini_label,R.id.mini_height,R.id.mini_fee,R.id.mini_price)) {
                val view=root.findViewById<TextView>(id)
                if(view.visibility!=View.VISIBLE) continue
                val r=Rect();view.getDrawingRect(r);root.offsetDescendantRectToMyCoords(view,r)
                assertTrue("Clipped bounds at $w x $h: $r",r.top>=0 && r.bottom<=root.height && r.right<=root.width)
                assertTrue("Clipped text at $w x $h: ${view.text}",(0 until view.layout.lineCount).all { view.layout.getEllipsisCount(it)==0 })
                assertTrue("Full text must fit at $w x $h: ${view.text}",view.paint.measureText(view.text.toString())<=view.width+1)
            }
            val bitmap=Bitmap.createBitmap(root.width,root.height,Bitmap.Config.ARGB_8888);root.draw(Canvas(bitmap))
            val folder=File("build/reports/miniwidget").apply { mkdirs() }
            File(folder,"mini-$code-$w-$h.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
        val largeHeight=snapshot.copy(block=snapshot.block.copy(value=snapshot.block.value!!.copy(height=1000001)),fees=Source(snapshot.fees.value!!.copy(next=.14),System.currentTimeMillis()))
        val small=render(context,largeHeight,40,40)
        assertEquals("1.000.001",small.findViewById<TextView>(R.id.mini_height).text.toString())
        assertEquals("0,14 sat/vB",small.findViewById<TextView>(R.id.mini_fee).text.toString())
        for(id in listOf(R.id.mini_height,R.id.mini_fee)) {
            val v=small.findViewById<TextView>(id);assertTrue(v.paint.measureText(v.text.toString())<=v.width+1)
        }
        val scaled=context.createConfigurationContext(Configuration(context.resources.configuration).apply { fontScale=1.5f })
        val accessible=render(scaled,snapshot,64,64)
        assertEquals(View.GONE,accessible.findViewById<View>(R.id.mini_price).visibility)
        assertTrue(accessible.findViewById<View>(R.id.mini_fee).bottom<=accessible.height)
        val empty=render(context,Snapshot(),80,88)
        assertEquals("—",empty.findViewById<TextView>(R.id.mini_height).text.toString())
        assertEquals("— sat/vB",empty.findViewById<TextView>(R.id.mini_fee).text.toString())
        val stale=render(context,snapshot.copy(block=snapshot.block.copy(fetchedAt=1)),80,88)
        assertEquals("Blok · oud",stale.findViewById<TextView>(R.id.mini_label).text.toString())
    }
    @Test fun currencyAndDeletionAffectOnlyTheirOwnInstance() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        WidgetVisualFixture.seed(context,System.currentTimeMillis())
        val manager=AppWidgetManager.getInstance(context)
        for(id in listOf(51,52)) manager.bindAppWidgetIdIfAllowed(id,ComponentName(context,MiniWidget::class.java))
        assertTrue("Mini-only installations still need background updates",WidgetUpdates.hasWidgets(context))
        assertTrue(WidgetCurrency.set(context,51,"USD"))
        assertFalse(WidgetCurrency.set(context,999,"USD"));assertFalse(WidgetCurrency.set(context,52,"GBP"))
        WidgetUpdates.render(context)
        assertEquals("$ 78,3k",shadowOf(manager).getViewFor(51).findViewById<TextView>(R.id.mini_price).text.toString())
        assertEquals("€ 72,5k",shadowOf(manager).getViewFor(52).findViewById<TextView>(R.id.mini_price).text.toString())
        assertEquals("EUR",context.repository.state.value.currency)
        context.repository.setCurrency("USD")
        WidgetUpdates.render(context)
        assertEquals("EUR",WidgetCurrency.read(context,52,"EUR"))
        assertEquals("USD",WidgetCurrency.read(context,51,"EUR"))
        assertEquals("€ 72,5k",shadowOf(manager).getViewFor(52).findViewById<TextView>(R.id.mini_price).text.toString())
        MiniWidget().onDeleted(context,intArrayOf(51))
        assertFalse(context.getSharedPreferences("widget-options",0).contains("currency-51"))
        assertEquals("EUR",WidgetCurrency.read(context,52,"EUR"))
    }
    @Test fun eachTileOpensWidgetSettingsWithItsOwnId() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        for(id in listOf(61,62)) {
            val root=render(context,Snapshot(),80,88,id)
            assertTrue(root.performClick())
            val intent=shadowOf(context).nextStartedActivity
            assertEquals(MainActivity::class.java.name,intent.component!!.className)
            assertTrue(intent.getBooleanExtra(MiniWidget.OPEN_WIDGETS,false))
            assertEquals(id,intent.getIntExtra(MiniWidget.WIDGET_ID,-1))
        }
    }
}
