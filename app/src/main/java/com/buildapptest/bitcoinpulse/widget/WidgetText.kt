package com.buildapptest.bitcoinpulse.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue

/** Match TextView's SP conversion, including Android's nonlinear large-font scaling. */
internal object WidgetText {
    fun fit(context: Context,text: String,desired: Float,width: Int): Float {
        val metrics=context.resources.displayMetrics
        val available=(width-2).coerceAtLeast(1)*metrics.density
        val paint=Paint().apply { typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);fontFeatureSettings="tnum" }
        fun fits(size: Float): Boolean { paint.textSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,size,metrics);return paint.measureText(text)<=available }
        if(fits(desired)) return desired
        var low=.5f;var high=desired
        repeat(12) { val middle=(low+high)/2;if(fits(middle))low=middle else high=middle }
        return low
    }
}
