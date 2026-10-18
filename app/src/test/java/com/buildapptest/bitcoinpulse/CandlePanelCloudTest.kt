package com.buildapptest.bitcoinpulse

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import com.buildapptest.bitcoinpulse.data.PERIODS
import com.buildapptest.bitcoinpulse.ui.CandlePanel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=PulseApplication::class,qualifiers="w390dp-h900dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CandlePanelCloudTest {
    @Test fun oneAccessibleSwitchWorksForSingleAndAllCombinationCharts() {
        val app=RuntimeEnvironment.getApplication() as PulseApplication
        val now=System.currentTimeMillis()
        val prefs=app.getSharedPreferences("pulse-data-v1",0).edit().clear()
        for(period in PERIODS) {
            val rows=(0..239).joinToString(",") { i -> "[${1700000000L+i*period.minutes*60L},${1000+i},${1010+i},${990+i},${1001+i},${1000+i},1,1]" }
            prefs.putString("candles-EUR-${period.id}","""{"error":[],"result":{"XXBTZEUR":[$rows]}}""").putLong("candles-EUR-${period.id}-time",now)
        }
        prefs.commit()
        val repository=app.repository
        val controller=Robolectric.buildActivity(ComponentActivity::class.java).setup().visible()
        val activity=controller.get()
        activity.setContent {
            val s by repository.state.collectAsState()
            MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xffff8a1f))) {
                Column(Modifier.verticalScroll(rememberScrollState())) { CandlePanel(s,repository,now,detailed=false) }
            }
        }
        fun settle() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(350))
        settle()
        fun owner(v: View): SemanticsOwner? {
            if(v.javaClass.name.contains("AndroidComposeView")) return v.javaClass.getMethod("getSemanticsOwner").invoke(v) as SemanticsOwner
            if(v is ViewGroup) for(i in 0 until v.childCount) owner(v.getChildAt(i))?.let { return it }
            return null
        }
        val semantics=owner(activity.window.decorView)!!
        fun nodes()=semantics.getAllSemanticsNodes(false)
        fun switch()=nodes().single { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains("Ichimoku cloud voor alle grafieken")==true && it.config.getOrNull(SemanticsActions.OnClick)!=null }
        assertFalse(repository.state.value.ichimoku)
        assertTrue(switch().config[SemanticsActions.OnClick].action!!.invoke())
        settle();assertTrue(repository.state.value.ichimoku)
        repository.setChartMode("combi");settle()
        val images=nodes().filter { it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { text -> text.contains("-candles in EUR") }==true }
        assertEquals(3,images.size)
        assertTrue(repository.state.value.ichimoku)
        assertTrue(switch().config[SemanticsActions.OnClick].action!!.invoke())
        settle();assertFalse(repository.state.value.ichimoku)
        controller.pause().stop().destroy()
    }
}
