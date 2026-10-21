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
        val imports=listOf("EUR","USD").map { currency ->
            Omalauncher.installMini(activity,currency)
            val intent=shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW,intent.action);assertEquals("dev.omalauncher",intent.`package`)
            val encoded=intent.dataString!!.substringAfter("#owl1.")
            val inflater=Inflater(true)
            try {
                inflater.setInput(Base64.getUrlDecoder().decode(encoded)+0)
                val output=ByteArray(16384);val count=inflater.inflate(output)
                assertTrue(inflater.finished())
                output.copyOf(count).toString(Charsets.UTF_8).also {
                    assertTrue(it.contains("size: 1x1"));assertTrue(it.contains("state currency = \"$currency\" persist"))
                    assertTrue(it.contains("Bitcoin Pulse · Mini 1×1"))
                }
            } finally { inflater.end() }
        }
        assertNotEquals(imports[0],imports[1])
        assertTrue(activity.assets.open("bitcoin-pulse-mini.owl").bufferedReader().use { it.readText() }.contains("state currency = \"EUR\" persist"))
        controller.pause().stop().destroy()
    }
}
