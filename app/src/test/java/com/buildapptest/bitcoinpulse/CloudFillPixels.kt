package com.buildapptest.bitcoinpulse

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/** Count actual fill color, not antialiased text, candles or cloud boundaries. */
internal fun cloudFillPixels(bitmap: Bitmap, up: Boolean): Int {
    val expected=Color.parseColor(if(up) "#4243A047" else "#42F23645")
    var count=0
    for(y in 0 until bitmap.height) for(x in 0 until bitmap.width) {
        val pixel=bitmap.getPixel(x,y)
        if(abs(Color.alpha(pixel)-Color.alpha(expected))<=1 &&
            abs(Color.red(pixel)-Color.red(expected))<=3 &&
            abs(Color.green(pixel)-Color.green(expected))<=3 &&
            abs(Color.blue(pixel)-Color.blue(expected))<=3) count++
    }
    return count
}
