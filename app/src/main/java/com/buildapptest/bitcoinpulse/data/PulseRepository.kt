package com.buildapptest.bitcoinpulse.data

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class PulseRepository(context: Context) {
    private val preferences = context.getSharedPreferences("pulse-data-v1", Context.MODE_PRIVATE)
    private val http = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS).callTimeout(22, TimeUnit.SECONDS).build()
    private val mutex = Mutex()
    private fun <T> cached(key: String, parser: (String) -> T): Source<T> = try {
        val raw = preferences.getString(key, null)
        Source(raw?.let(parser), preferences.getLong("$key-time", 0), preferences.getString("$key-error", null))
    } catch (_: Exception) { Source(error = "Stored data needs a refresh") }

    private val mutable = MutableStateFlow(Snapshot(
        cached("prices", Parse::prices), cached("fees", Parse::fees),
        cached("mempool", Parse::mempool), cached("block", Parse::block),
        preferences.getString("currency", "EUR").let { if (it == "USD") "USD" else "EUR" },
        candles = listOf("EUR", "USD").flatMap { code -> PERIODS.map { period ->
            candleKey(code, period.id) to cached("candles-$code-${period.id}") { CandleParser.parse(it, code) }
        } }.toMap(),
        chartMode = preferences.getString("chart-mode", "4h").let { if (it in listOf("4h", "1d", "1w", "combi")) it!! else "4h" },
        combo = preferences.getStringSet("chart-combo", setOf("4h", "1d", "1w"))!!.filter { id -> PERIODS.any { it.id == id } }.toSet().let { if (it.size >= 2) it else setOf("4h", "1d", "1w") },
        difficulty = cached("difficulty", Parse::difficulty)
    ))
    val state = mutable.asStateFlow()

    fun setChartMode(mode: String) {
        require(mode in listOf("4h", "1d", "1w", "combi"))
        preferences.edit().putString("chart-mode", mode).apply()
        mutable.update { it.copy(chartMode = mode) }
    }
    fun setCombo(periods: Set<String>) {
        require(periods.size in 2..3 && periods.all { p -> PERIODS.any { it.id == p } })
        preferences.edit().putStringSet("chart-combo", periods).apply()
        mutable.update { it.copy(combo = periods) }
    }

    fun setCurrency(code: String) {
        require(code == "EUR" || code == "USD")
        preferences.edit().putString("currency", code).apply()
        mutable.update { it.copy(currency = code) }
    }

    private fun download(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", "BitcoinPulse/1.0 (Android)").build()
        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            response.body?.string() ?: throw IOException("Empty response")
        }
    }

    private suspend fun <T> fetch(key: String, url: String, old: Source<T>, parser: (String) -> T): Source<T> = withContext(Dispatchers.IO) {
        try {
            val raw = download(url)
            val value = parser(raw)
            val now = System.currentTimeMillis()
            preferences.edit().putString(key, raw).putLong("$key-time", now).remove("$key-error").apply()
            Source(value, now)
        } catch (cancel: CancellationException) { throw cancel }
        catch (_: Exception) {
            val message = "Could not refresh. Check your connection and try again."
            preferences.edit().putString("$key-error", message).apply()
            old.copy(error = message)
        }
    }

    suspend fun refresh(): Boolean = mutex.withLock {
        val old = mutable.value
        mutable.update { it.copy(refreshing = true) }
        try {
            coroutineScope {
                val prices = async { fetch("prices", "https://api.kraken.com/0/public/Ticker?pair=XBTEUR,XBTUSD", old.prices, Parse::prices) }
                val fees = async { fetch("fees", "https://mempool.space/api/v1/fees/recommended", old.fees, Parse::fees) }
                val mempool = async { fetch("mempool", "https://mempool.space/api/mempool", old.mempool, Parse::mempool) }
                val block = async { fetch("block", "https://mempool.space/api/v1/blocks", old.block, Parse::block) }
                val difficulty = async {
                    if (old.difficulty.value != null && System.currentTimeMillis() - old.difficulty.fetchedAt in 0..300_000L) old.difficulty
                    else fetch("difficulty", "https://mempool.space/api/v1/difficulty-adjustment", old.difficulty, Parse::difficulty)
                }
                val candles = async {
                    val updated = old.candles.toMutableMap()
                    for (period in PERIODS) {
                        val key = candleKey(old.currency, period.id)
                        val source = old.candles[key] ?: Source()
                        if (source.value == null || System.currentTimeMillis() - source.fetchedAt !in 0..90_000L) {
                            // Pace Kraken's public requests, and retain each currency independently.
                            delay(1100)
                            updated[key] = fetch("candles-${old.currency}-${period.id}",
                                "https://api.kraken.com/0/public/OHLC?pair=XBT${old.currency}&interval=${period.minutes}", source) { CandleParser.parse(it, old.currency) }
                        }
                    }
                    updated.toMap()
                }
                val p = prices.await(); val f = fees.await(); val m = mempool.await(); val b = block.await()
                mutable.update { it.copy(prices = p, fees = f, mempool = m, block = b) }
                val d = difficulty.await(); val c = candles.await()
                mutable.update { it.copy(difficulty = d, candles = c) }
            }
            !mutable.value.hasErrors
        } finally { mutable.update { it.copy(refreshing = false) } }
    }
}
