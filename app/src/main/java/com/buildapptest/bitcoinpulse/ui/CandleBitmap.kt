package com.buildapptest.bitcoinpulse.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.buildapptest.bitcoinpulse.data.Candle
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

/** Same chart renderer for Compose and RemoteViews; only real OHLC observations are drawn. */
object CandleBitmap {
    fun render(candles: List<Candle>, currency: String, width: Int, height: Int, selected: Int? = null, density: Float = 1f): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = density
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f * scale; typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL) }
        fun text(label: String, x: Float, y: Float, end: Boolean = false) {
            p.color = Color.parseColor("#BAC7DB"); p.style = Paint.Style.FILL
            p.textAlign = if (end) Paint.Align.RIGHT else Paint.Align.LEFT
            canvas.drawText(label, x, y, p)
        }
        if (candles.isEmpty()) { text("Geen candledata · vernieuw om te laden", 4*scale, height/2f); return bitmap }
        val left = 3*scale; val right = width-65*scale; val top = 10*scale; val bottom = height-25*scale
        if (bottom <= top) return bitmap
        val low = candles.minOf { it.low }; val high = candles.maxOf { it.high }
        val pad = max((high-low)*0.08, high*0.0001)
        val min = low-pad; val max = high+pad
        fun y(v: Double) = (top+(max-v)/(max-min)*(bottom-top)).toFloat()
        for (j in 0..2) {
            val value = min+(max-min)*j/2
            p.color = Color.parseColor("#38485F"); p.strokeWidth = scale
            canvas.drawLine(left,y(value),right,y(value),p)
            text(String.format(Locale.getDefault(), "%.1fk",value/1000),width-2*scale,y(value)+4*scale,true)
        }
        val step = (right-left)/candles.size
        candles.forEachIndexed { i, d ->
            val x = left+step*(i+.5f); val up = d.close >= d.open
            if (i == selected) {
                p.color = Color.parseColor("#243246"); p.style = Paint.Style.FILL
                canvas.drawRect(x-step*.5f,top,x+step*.5f,bottom,p)
            }
            p.color = Color.parseColor(if (up) "#35E89A" else "#FF647C"); p.strokeWidth = max(scale,1f)
            p.style = Paint.Style.STROKE
            canvas.drawLine(x,y(d.high),x,y(d.low),p)
            val body = RectF(x-step*.28f,y(maxOf(d.open,d.close)),x+step*.28f,y(minOf(d.open,d.close)))
            if (body.height() < scale*2) body.bottom=body.top+scale*2
            p.style = if (up) Paint.Style.STROKE else Paint.Style.FILL
            canvas.drawRect(body,p)
            // The current, uncommitted candle is explicitly marked with a dot.
            if (d.provisional) { p.style=Paint.Style.FILL; canvas.drawCircle(x,top-5*scale,2*scale,p) }
        }
        val formatter = DateTimeFormatter.ofPattern("d MMM",Locale.getDefault()).withZone(ZoneOffset.UTC)
        listOf(0,candles.lastIndex/2,candles.lastIndex).distinct().forEachIndexed { index,i ->
            val label = formatter.format(Instant.ofEpochSecond(candles[i].timestamp))
            val x = when(index) {0 -> left; 1 -> (left+right)/2; else -> right}
            p.textAlign = Paint.Align.LEFT
            val offset = if(index==1) p.measureText(label)/2 else 0f
            text(label,x-offset,height-7*scale,index==2)
        }
        text(currency,width-2*scale,height-7*scale,true)
        return bitmap
    }
}
