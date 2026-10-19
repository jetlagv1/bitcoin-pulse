package com.buildapptest.bitcoinpulse.widget

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.Deflater

/** Omalauncher's documented owl1 link format. The widget stays in the URI fragment. */
object Omalauncher {
    fun widgetLink(source: String): String {
        val compressor = Deflater(Deflater.BEST_COMPRESSION, true)
        try {
            compressor.setInput(source.toByteArray(Charsets.UTF_8))
            compressor.finish()
            val bytes = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (!compressor.finished()) bytes.write(buffer, 0, compressor.deflate(buffer))
            return "https://simonschubert.github.io/omarchy-widgets/w#owl1." +
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray())
        } finally {
            compressor.end()
        }
    }

    fun install(context: Context) {
        val currency=(context.applicationContext as com.buildapptest.bitcoinpulse.PulseApplication).repository.state.value.currency
        val source = configuredSource(context.assets.open("bitcoin-pulse.owl").bufferedReader().use { it.readText() },WidgetCharts.defaultCount(context),currency)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(widgetLink(source))).setPackage("dev.omalauncher")
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Installeer of update Omalauncher om deze widget toe te voegen.", Toast.LENGTH_LONG).show()
        }
    }
    fun configuredSource(source: String,count: Int,currency: String="EUR"): String {
        require(count in 1..2)
        require(currency in listOf("EUR","USD"))
        return source.replace("state graphCount = 2 persist","state graphCount = $count persist")
            .replace("state currency = \"EUR\" persist","state currency = \"$currency\" persist")
    }
}
