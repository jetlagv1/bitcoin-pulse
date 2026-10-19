package com.buildapptest.bitcoinpulse.data

/** Zoom changes the viewport only: all calculations still use the full OHLC history. */
object ChartZoom {
    const val MIN = 24
    const val DEFAULT = 36
    const val MAX = 90
    val steps = listOf(24, 36, 60, 90)
    fun clamp(count: Int) = count.coerceIn(MIN, MAX)
    fun visible(requested: Int, available: Int) = clamp(requested).coerceAtMost(available.coerceAtLeast(0))
    fun step(count: Int, out: Boolean): Int = if(out) steps.firstOrNull { it>count } ?: MAX
        else steps.lastOrNull { it<count } ?: MIN
    fun pinch(count: Float, scale: Float): Float =
        if(scale.isFinite() && scale>0) (count/scale).coerceIn(MIN.toFloat(),MAX.toFloat()) else count
}
