package com.buildapptest.bitcoinpulse.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

data class Quote(val price: Double, val open: Double, val high: Double, val low: Double) {
    val todayPercent: Double get() = (price / open - 1.0) * 100.0
}
data class Prices(val eur: Quote, val usd: Quote) { fun inCurrency(code: String) = if (code == "USD") usd else eur }
data class Fees(val next: Double, val halfHour: Double, val hour: Double)
data class Mempool(val count: Long, val virtualBytes: Long)
data class Block(val height: Long, val timestamp: Long, val transactions: Long,
    val medianFee: Double? = null, val pool: String? = null, val weight: Long? = null,
    val totalFees: Long? = null, val feeMin: Double? = null, val feeMax: Double? = null,
    val reward: Long? = null) {
    val fullness: Double? get() = weight?.let { it / 4_000_000.0 * 100 }
}
data class Difficulty(val change: Double, val progress: Double, val remainingBlocks: Long)

data class Source<T>(val value: T? = null, val fetchedAt: Long = 0, val error: String? = null) {
    fun isStale(now: Long) = fetchedAt <= 0 || now - fetchedAt > 20 * 60_000L || now < fetchedAt
    fun status(now: Long): String = when {
        value == null -> if (error != null) "Unavailable · retry" else "Waiting for first update"
        error != null -> "Cached · refresh failed"
        isStale(now) -> "Older data · refresh"
        else -> "Updated ${Format.time(fetchedAt)}"
    }
}
data class Snapshot(
    val prices: Source<Prices> = Source(),
    val fees: Source<Fees> = Source(),
    val mempool: Source<Mempool> = Source(),
    val block: Source<Block> = Source(),
    val currency: String = "EUR",
    val refreshing: Boolean = false,
    val candles: Map<String, Source<List<Candle>>> = emptyMap(),
    val chartMode: String = "4h",
    val combo: Set<String> = setOf("4h", "1d", "1w"),
    val difficulty: Source<Difficulty> = Source(),
    val ichimoku: Boolean = false
) {
    fun history(period: String) = candles[candleKey(currency, period)] ?: Source()
    fun chartPeriods(mode: String = chartMode) = if (mode == "combi") PERIODS.filter { it.id in combo } else PERIODS.filter { it.id == mode }
    val quote get() = prices.value?.inCurrency(currency)
    val hasAnyData get() = listOf(prices.value, fees.value, mempool.value, block.value).any { it != null }
    val hasErrors get() = listOf(prices.error, fees.error, mempool.error, block.error, difficulty.error).any { it != null } || chartPeriods().any { history(it.id).error != null }
    fun oldestTimestamp(compact: Boolean = false): Long =
        if (compact) listOf(prices.fetchedAt, fees.fetchedAt).minOrNull() ?: 0
        else listOf(prices.fetchedAt, fees.fetchedAt, mempool.fetchedAt, block.fetchedAt).minOrNull() ?: 0
}

object Parse {
    private fun nonnegative(value: Double): Double {
        require(value.isFinite() && value >= 0) { "Invalid numeric value" }; return value
    }
    private fun positive(value: Double): Double { require(value.isFinite() && value > 0); return value }
    private fun whole(value: Long): Long { require(value >= 0); return value }
    fun fees(raw: String): Fees = JSONObject(raw).let {
        Fees(nonnegative(it.getDouble("fastestFee")), nonnegative(it.getDouble("halfHourFee")), nonnegative(it.getDouble("hourFee")))
    }
    fun mempool(raw: String): Mempool = JSONObject(raw).let { Mempool(whole(it.getLong("count")), whole(it.getLong("vsize"))) }
    fun block(raw: String): Block = JSONArray(raw).getJSONObject(0).let {
        val extras = it.optJSONObject("extras")
        fun optionalDouble(key: String) = extras?.takeIf { e -> e.has(key) && !e.isNull(key) }?.getDouble(key)?.let(::nonnegative)
        fun optionalLong(key: String) = extras?.takeIf { e -> e.has(key) && !e.isNull(key) }?.getLong(key)?.let(::whole)
        val range = extras?.optJSONArray("feeRange")
        Block(whole(it.getLong("height")), whole(it.getLong("timestamp")), whole(it.getLong("tx_count")),
            optionalDouble("medianFee"), extras?.optJSONObject("pool")?.optString("name")?.takeIf(String::isNotBlank),
            it.takeIf { b -> b.has("weight") && !b.isNull("weight") }?.getLong("weight")?.let(::whole),
            optionalLong("totalFees"), range?.takeIf { r -> r.length() > 0 }?.getDouble(0)?.let(::nonnegative),
            range?.takeIf { r -> r.length() > 0 }?.getDouble(range.length()-1)?.let(::nonnegative), optionalLong("reward"))
    }
    fun difficulty(raw: String): Difficulty = JSONObject(raw).let {
        val change = it.getDouble("difficultyChange"); require(change.isFinite())
        val progress = it.getDouble("progressPercent"); require(progress.isFinite() && progress in 0.0..100.0)
        Difficulty(change, progress, whole(it.getLong("remainingBlocks")))
    }
    fun prices(raw: String): Prices {
        val root = JSONObject(raw)
        require(root.getJSONArray("error").length() == 0) { "Price provider error" }
        val result = root.getJSONObject("result")
        fun quote(key: String): Quote = result.getJSONObject(key).let {
            Quote(positive(it.getJSONArray("c").getDouble(0)), positive(it.getDouble("o")),
                positive(it.getJSONArray("h").getDouble(1)), positive(it.getJSONArray("l").getDouble(1)))
        }
        return Prices(quote("XXBTZEUR"), quote("XXBTZUSD"))
    }
}

object Format {
    fun preciseMoney(value: Double?, code: String): String = value?.let {
        NumberFormat.getCurrencyInstance(if(code=="USD") Locale.US else Locale.GERMANY).apply {
            currency=Currency.getInstance(code); minimumFractionDigits=2; maximumFractionDigits=2
        }.format(it)
    } ?: "—"
    fun money(value: Double?, currency: String, compact: Boolean = false): String {
        if (value == null) return "—"
        if (compact && value >= 10_000) return (if (currency == "USD") "$" else "€") + String.format(Locale.US, "%.1fk", value / 1000)
        return NumberFormat.getCurrencyInstance(if (currency == "USD") Locale.US else Locale.GERMANY).apply {
            this.currency = Currency.getInstance(currency)
            maximumFractionDigits = 0
        }.format(value)
    }
    fun number(value: Long?): String = value?.let { NumberFormat.getIntegerInstance(Locale.getDefault()).format(it) } ?: "—"
    fun fee(value: Double?): String = value?.let { NumberFormat.getNumberInstance(Locale.getDefault()).apply { maximumFractionDigits = 2 }.format(it) } ?: "—"
    fun time(millis: Long): String = if (millis <= 0) "Not updated" else DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(millis))
    fun percent(quote: Quote?): String = quote?.let { String.format(Locale.getDefault(), "%+.2f%%", it.todayPercent) } ?: "—"
    fun blockAge(block: Block?, now: Long): String {
        if (block == null) return "—"
        val seconds = ((now / 1000) - block.timestamp).coerceAtLeast(0)
        return when { seconds < 60 -> "<1 min"; seconds < 3600 -> "${seconds / 60} min"; else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m" }
    }
}
