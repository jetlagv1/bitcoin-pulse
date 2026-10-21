package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import com.buildapptest.bitcoinpulse.ui.MiniWidgetsPanel
import com.buildapptest.bitcoinpulse.widget.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=PulseApplication::class,qualifiers="w390dp-h900dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MiniWidgetsPanelTest {
    @Test fun appControlsChangeOnlyTheSelectedRealWidget() {
        val context=RuntimeEnvironment.getApplication() as PulseApplication
        val now=System.currentTimeMillis();WidgetVisualFixture.seed(context,now)
        val manager=AppWidgetManager.getInstance(context)
        for(id in listOf(81,82)) manager.bindAppWidgetIdIfAllowed(id,ComponentName(context,MiniWidget::class.java))
        val snapshot=context.repository.state.value
        val controller=Robolectric.buildActivity(ComponentActivity::class.java)
        val activity=controller.get();activity.enableEdgeToEdge();controller.setup().visible()
        activity.setContent {
            MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xffff8a1f),onPrimary=Color(0xff17120d),onSurface=Color(0xfff7faff))) {
                Surface(color=Color(0xff0c1018),contentColor=Color(0xfff7faff)) {
                    Column(Modifier.verticalScroll(rememberScrollState())) { MiniWidgetsPanel(snapshot,now) }
                }
            }
        }
        val decor=activity.window.decorView;val d=context.resources.displayMetrics.density
        val w=(390*d).toInt();val h=(900*d).toInt()
        fun settle() {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(350))
            decor.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));decor.layout(0,0,w,h)
        }
        fun owner(v: View): SemanticsOwner? {
            if(v.javaClass.name.contains("AndroidComposeView"))return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
            if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
            return null
        }
        settle()
        fun choose(index: Int,code: String) {
            val node=owner(decor)!!.getAllSemanticsNodes(false).single { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains("Widget $index: valuta $code")==true && it.config.getOrNull(SemanticsActions.OnClick)!=null }
            assertTrue(node.config[SemanticsActions.OnClick].action!!.invoke());settle()
        }
        choose(1,"USD")
        assertEquals("USD",WidgetCurrency.read(context,81,"EUR"));assertEquals("EUR",WidgetCurrency.read(context,82,"EUR"))
        choose(2,"USD");choose(1,"EUR")
        assertEquals("EUR",WidgetCurrency.read(context,81,"EUR"));assertEquals("USD",WidgetCurrency.read(context,82,"EUR"))
        assertEquals("EUR",context.repository.state.value.currency)
        assertEquals(2,MiniWidget.ids(context).size)
        val image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);decor.draw(Canvas(image))
        val folder=File("build/reports/miniwidget").apply { mkdirs() }
        File(folder,"app-settings.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
        controller.pause().stop().destroy()
    }
}
