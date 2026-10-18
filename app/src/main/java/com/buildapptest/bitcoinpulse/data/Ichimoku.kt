package com.buildapptest.bitcoinpulse.data

/** Only the two leading spans are exposed. Windows include the source candle. */
object Ichimoku {
    const val CONVERSION = 20
    const val BASE = 60
    const val SPAN_B = 120
    const val DISPLACEMENT = 30
    // TradingView's built-in input counts the current bar: plot offset = displacement - 1.
    const val OFFSET = DISPLACEMENT - 1
    const val VISIBLE = 36
    const val REQUIRED = SPAN_B + OFFSET + VISIBLE - 1
    const val HISTORY = 240
    data class Point(val index: Int, val a: Double, val b: Double)

    fun calculate(history: List<Candle>): List<Point> {
        fun midpoint(index: Int, length: Int): Double {
            var high = Double.NEGATIVE_INFINITY
            var low = Double.POSITIVE_INFINITY
            for (i in index - length + 1..index) { high = maxOf(high, history[i].high); low = minOf(low, history[i].low) }
            return (high + low) / 2.0
        }
        return (SPAN_B - 1 until history.size).map { i ->
            Point(i + OFFSET, (midpoint(i, CONVERSION) + midpoint(i, BASE)) / 2.0, midpoint(i, SPAN_B))
        }
    }

}
