package com.buildapptest.bitcoinpulse.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Path
import android.graphics.DashPathEffect
import com.buildapptest.bitcoinpulse.data.Candle
import com.buildapptest.bitcoinpulse.data.Ichimoku
import com.buildapptest.bitcoinpulse.data.ChartZoom
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

/** Same chart renderer for Compose and RemoteViews; only real OHLC observations are drawn. */
object CandleBitmap {
    fun render(history: List<Candle>, currency: String, width: Int, height: Int, selected: Int? = null, density: Float = 1f, ichimoku: Boolean = false, visibleCount: Int = ChartZoom.DEFAULT): Bitmap {
        val candles=history.takeLast(ChartZoom.visible(visibleCount,history.size))
        val start=history.size-candles.size
        val cloud=if(ichimoku && candles.isNotEmpty() && history.size>=Ichimoku.required(candles.size)) Ichimoku.calculate(history).filter { it.index>=start } else emptyList()
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
        if (bottom <= top || right <= left) return bitmap
        val low = minOf(candles.minOf { it.low },cloud.minOfOrNull { minOf(it.a,it.b) } ?: Double.POSITIVE_INFINITY)
        val high = maxOf(candles.maxOf { it.high },cloud.maxOfOrNull { maxOf(it.a,it.b) } ?: Double.NEGATIVE_INFINITY)
        val pad = max((high-low)*0.08, high*0.0001)
        val min = low-pad; val max = high+pad
        fun y(v: Double) = (top+(max-v)/(max-min)*(bottom-top)).toFloat()
        for (j in 0..2) {
            val value = min+(max-min)*j/2
            p.color = Color.parseColor("#38485F"); p.strokeWidth = scale
            canvas.drawLine(left,y(value),right,y(value),p)
            text(String.format(Locale.getDefault(), "%.1fk",value/1000),width-2*scale,y(value)+4*scale,true)
        }
        val step = (right-left)/(candles.size + if(cloud.isNotEmpty()) Ichimoku.OFFSET else 0)
        fun x(index: Int) = left+step*(index-start+.5f)
        selected?.takeIf { it in candles.indices }?.let {
            p.color=Color.parseColor("#243246");p.style=Paint.Style.FILL
            canvas.drawRect(left+step*it,top,left+step*(it+1),bottom,p)
        }
        val greenFill=Path();val redFill=Path()
        fun fillCloud(x0: Float,a0: Double,b0: Double,x1: Float,a1: Double,b1: Double,up: Boolean) {
            (if(up)greenFill else redFill).apply { moveTo(x0,y(a0));lineTo(x1,y(a1));lineTo(x1,y(b1));lineTo(x0,y(b0));close() }
        }
        cloud.zipWithNext().forEach { (a,b) ->
            val d=a.a-a.b;val e=b.a-b.b
            if(d*e<0) {
                val t=d/(d-e);val cross=a.a+(b.a-a.a)*t;val xc=(x(a.index)+(x(b.index)-x(a.index))*t).toFloat()
                fillCloud(x(a.index),a.a,a.b,xc,cross,cross,d>=0)
                fillCloud(xc,cross,cross,x(b.index),b.a,b.b,e>=0)
            } else fillCloud(x(a.index),a.a,a.b,x(b.index),b.a,b.b,(if(d==0.0)e else d)>=0)
        }
        // Build both paths completely before painting their translucent interiors.
        p.style=Paint.Style.FILL
        p.color=Color.parseColor("#4243A047");canvas.drawPath(greenFill,p)
        p.color=Color.parseColor("#42F23645");canvas.drawPath(redFill,p)
        cloud.zipWithNext().forEach { (a,b) ->
            p.style=Paint.Style.STROKE;p.strokeWidth=scale
            p.color=Color.parseColor("#A5D6A7");canvas.drawLine(x(a.index),y(a.a),x(b.index),y(b.a),p)
            p.color=Color.parseColor("#EF9A9A");canvas.drawLine(x(a.index),y(a.b),x(b.index),y(b.b),p)
        }
        if(cloud.isNotEmpty()) {
            p.color=Color.parseColor("#6038485F");p.pathEffect=DashPathEffect(floatArrayOf(2*scale,3*scale),0f)
            canvas.drawLine(left+step*candles.size,top,left+step*candles.size,bottom,p);p.pathEffect=null
        }
        candles.forEachIndexed { i, d ->
            val x = left+step*(i+.5f); val up = d.close >= d.open
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
        val ticks=if(cloud.isNotEmpty()) listOf(0,candles.lastIndex) else listOf(0,candles.lastIndex/2,candles.lastIndex).distinct()
        ticks.forEachIndexed { index,i ->
            val label = formatter.format(Instant.ofEpochSecond(candles[i].timestamp))
            val x = if(cloud.isNotEmpty()) left+step*(i+.5f) else when(index) {0 -> left; 1 -> (left+right)/2; else -> right}
            p.textAlign = Paint.Align.LEFT
            val offset = if(index==1) p.measureText(label)/2 else 0f
            text(label,x-offset,height-7*scale,index==2)
        }
        if(cloud.isNotEmpty()) text("Cloud",right,height-7*scale,true)
        text(currency,width-2*scale,height-7*scale,true)
        return bitmap
    }
}
