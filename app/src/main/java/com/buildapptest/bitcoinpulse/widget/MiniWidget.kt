package com.buildapptest.bitcoinpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.buildapptest.bitcoinpulse.MainActivity
import com.buildapptest.bitcoinpulse.R
import com.buildapptest.bitcoinpulse.data.Format
import com.buildapptest.bitcoinpulse.data.Snapshot
import java.text.NumberFormat
import java.util.Locale

/** The smallest widget shares the existing worker, cache and per-instance currency storage. */
class MiniWidget : PulseWidget() {
    companion object {
        const val OPEN_WIDGETS = "open-mini-widgets"
        const val WIDGET_ID = "mini-widget-id"
        fun ids(context: Context): IntArray = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context,MiniWidget::class.java))

        fun pin(context: Context) {
            val manager=AppWidgetManager.getInstance(context)
            if(manager.isRequestPinAppWidgetSupported && manager.requestPinAppWidget(ComponentName(context,MiniWidget::class.java),null,null)) return
            Toast.makeText(context,"Houd je startscherm ingedrukt, kies Widgets en vervolgens Bitcoin Pulse · Mini 1×1.",Toast.LENGTH_LONG).show()
        }

        internal fun price(value: Double?,currency: String): String {
            val symbol=if(currency=="USD")"$" else "€"
            if(value==null) return "$symbol —"
            val compact=value>=1000
            val number=NumberFormat.getNumberInstance(Locale.GERMANY).apply { maximumFractionDigits=if(compact)1 else 0 }
            return "$symbol ${number.format(if(compact)value/1000 else value)}${if(compact)"k" else ""}"
        }

        internal fun views(context: Context,id: Int,snapshot: Snapshot,width: Int,height: Int,now: Long,busy: Boolean=false): RemoteViews {
            val views=RemoteViews(context.packageName,R.layout.widget_mini)
            val scale=context.resources.configuration.fontScale.coerceAtLeast(.5f)
            val density=context.resources.displayMetrics.density
            val inset=if(minOf(width,height)<60)4 else 6
            fun fit(text: String,desired: Float) = WidgetText.fit(context,text,desired,width-2*inset)
            val block=WidgetAppearance.number(snapshot.block.value?.height)
            val fee="${WidgetAppearance.fee(snapshot.fees.value?.next)} sat/vB"
            val quote=price(snapshot.quote?.price,snapshot.currency)
            val growth=(minOf(width,height)/80f).coerceIn(1f,1.5f)
            val blockSize=fit(block,(if(width<76)14f else 15f)*growth)
            val feeSize=fit(fee,(if(width<76)11f else 12f)*growth)
            val priceSize=fit(quote,12f*growth)
            val labelSize=(if(width<76)9f else 10f)*growth
            val showPrice=width>=76 && height>=2*inset+(labelSize+blockSize+feeSize+priceSize)*scale*1.2f+6
            val showLabel=height>=2*inset+(labelSize+blockSize+feeSize)*scale*1.2f+4
            val sources=listOf(snapshot.block,snapshot.fees)+if(showPrice)listOf(snapshot.prices)else emptyList()
            val old=sources.any { it.value!=null && (it.error!=null || it.isStale(now)) }
            views.setViewPadding(R.id.mini_root,(inset*density).toInt(),(inset*density).toInt(),(inset*density).toInt(),(inset*density).toInt())
            views.setTextViewText(R.id.mini_label,if(old)"Blok · oud" else "Blok")
            views.setTextViewText(R.id.mini_height,block)
            views.setTextViewText(R.id.mini_fee,fee)
            views.setTextViewText(R.id.mini_price,quote)
            for((view,size) in listOf(R.id.mini_label to labelSize,R.id.mini_height to blockSize,R.id.mini_fee to feeSize,R.id.mini_price to priceSize))
                views.setTextViewTextSize(view,TypedValue.COMPLEX_UNIT_SP,size)
            views.setViewVisibility(R.id.mini_price,if(showPrice)View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.mini_label,if(showLabel)View.VISIBLE else View.GONE)
            val status=when { busy || snapshot.refreshing->"Gegevens worden vernieuwd";old->"Opgeslagen of oudere gegevens";else->"Laatst bijgewerkt ${Format.time(sources.minOf { it.fetchedAt })}" }
            views.setContentDescription(R.id.mini_root,"Blok $block. Geschat tarief voor het volgende blok: $fee. Bitcoin-prijs ${Format.preciseMoney(snapshot.quote?.price,snapshot.currency)}. $status. Tik voor de miniwidget-instellingen.")
            if(id!=AppWidgetManager.INVALID_APPWIDGET_ID) {
                val open=Intent(context,MainActivity::class.java).setData(Uri.parse("bitcoinpulse://mini-widget/$id"))
                    .putExtra(OPEN_WIDGETS,true).putExtra(WIDGET_ID,id)
                views.setOnClickPendingIntent(R.id.mini_root,PendingIntent.getActivity(context,id,open,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            }
            return views
        }
    }
}
