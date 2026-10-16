package com.buildapptest.bitcoinpulse.data

import org.json.JSONObject

data class CandlePeriod(val id: String, val label: String, val minutes: Int)
val PERIODS = listOf(CandlePeriod("4h", "4 uur", 240), CandlePeriod("1d", "1 dag", 1440), CandlePeriod("1w", "1 week", 10080))
fun candleKey(currency: String, period: String) = "$currency:$period"
data class Candle(val timestamp: Long, val open: Double, val high: Double, val low: Double,
    val close: Double, val volume: Double, val provisional: Boolean)

object CandleParser {
    fun parse(raw: String, currency: String): List<Candle> {
        require(currency in setOf("EUR", "USD"))
        val root = JSONObject(raw)
        require(root.getJSONArray("error").length() == 0) { "Kraken OHLC error" }
        val result = root.getJSONObject("result")
        val rows = result.getJSONArray(if (currency == "USD") "XXBTZUSD" else "XXBTZEUR")
        require(rows.length() > 0) { "No candles" }
        var previous = -1L
        return (0 until rows.length()).map { i ->
            val r = rows.getJSONArray(i)
            val timestamp = r.getLong(0); require(timestamp >= 0 && timestamp > previous); previous = timestamp
            val o = r.getDouble(1); val h = r.getDouble(2); val l = r.getDouble(3); val c = r.getDouble(4); val v = r.getDouble(6)
            require(listOf(o,h,l,c).all { it.isFinite() && it > 0 })
            require(v.isFinite() && v >= 0 && h >= maxOf(o,c) && l <= minOf(o,c) && h >= l)
            Candle(timestamp, o,h,l,c,v, i == rows.length()-1)
        }.takeLast(36)
    }
}
