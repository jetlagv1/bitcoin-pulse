package com.buildapptest.bitcoinpulse

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.buildapptest.bitcoinpulse.widget.Omalauncher
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Base64
import java.util.zip.Inflater

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],application=Application::class)
class OwlMiniImportTest {
    @Test fun appImportsSeparateMiniWidgetsWithTheSelectedCurrency() {
        val controller=Robolectric.buildActivity(Activity::class.java).setup()
        val activity=controller.get()
        activity.getSharedPreferences("widget-options",0).edit().putInt("default-chart-count",1).apply()
        val imports=listOf("EUR","USD").map { currency ->
            Omalauncher.installMini(activity,currency)
            val intent=shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW,intent.action);assertEquals("dev.omalauncher",intent.`package`)
            val encoded=intent.dataString!!.substringAfter("#owl1.")
            val inflater=Inflater(true)
            try {
                inflater.setInput(Base64.getUrlDecoder().decode(encoded)+0)
                val output=ByteArray(131072);val count=inflater.inflate(output)
                assertTrue(inflater.finished())
                output.copyOf(count).toString(Charsets.UTF_8).also {
                    assertTrue(it.contains("size: 1x1"));assertTrue(it.contains("state currency = \"$currency\" persist"))
                    assertTrue(it.contains("Bitcoin Pulse · Mini 1×1"));assertTrue(it.contains("state graphCount = 1 persist"))
                }
            } finally { inflater.end() }
        }
        assertNotEquals(imports[0],imports[1])
        assertTrue(activity.assets.open("bitcoin-pulse-mini.owl").bufferedReader().use { it.readText() }.contains("state currency = \"EUR\" persist"))
        controller.pause().stop().destroy()
    }
    @Test @Config(application=PulseApplication::class)
    fun compactImportCarriesItsSizeAndTheAppsCurrencyAndChartCount() {
        val controller=Robolectric.buildActivity(Activity::class.java).setup();val activity=controller.get()
        val app=activity.application as PulseApplication
        app.repository.setCurrency("USD")
        app.getSharedPreferences("widget-options",0).edit().putInt("default-chart-count",1).apply()
        Omalauncher.installCompact(activity)
        val intent=shadowOf(activity).nextStartedActivity
        assertEquals("dev.omalauncher",intent.`package`)
        val inflater=Inflater(true)
        try {
            inflater.setInput(Base64.getUrlDecoder().decode(intent.dataString!!.substringAfter("#owl1."))+0)
            val out=ByteArray(131072);val count=inflater.inflate(out);assertTrue(inflater.finished())
            val source=out.copyOf(count).toString(Charsets.UTF_8)
            assertTrue(source.contains("size: 2x2"));assertTrue(source.contains("state currency = \"USD\" persist"));assertTrue(source.contains("state graphCount = 1 persist"))
        } finally { inflater.end() }
        controller.pause().stop().destroy()
    }

}
