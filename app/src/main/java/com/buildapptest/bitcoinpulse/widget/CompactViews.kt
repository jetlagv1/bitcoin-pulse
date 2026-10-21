package com.buildapptest.bitcoinpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.buildapptest.bitcoinpulse.MainActivity
import com.buildapptest.bitcoinpulse.R
import com.buildapptest.bitcoinpulse.data.Format
import com.buildapptest.bitcoinpulse.data.Snapshot

/** Compact is a responsive stage, not a separate data/settings identity. */
internal object CompactViews {
    fun views(context: Context,id: Int,s: Snapshot,width: Int,height: Int,now: Long,busy: Boolean=false): RemoteViews {
        val views=RemoteViews(context.packageName,R.layout.widget_compact)
        val change=height>=220
        val estimates=height>=280
        val more=height>=240
        val details=height>=300
        val growth=((minOf(width,height)-160)/140f).coerceIn(0f,1f)
        val scale=context.resources.configuration.fontScale.coerceAtLeast(.5f)
        val body=(26+6*growth)+15+14+(if(more)24 else 0)+(if(details)24 else 0)+(if(change)12 else 0)+(if(estimates)24 else 0)
        val vertical=minOf(1f,(height-20-48-8-(if(more)6 else 0)-(if(details)6 else 0)-(if(estimates)6 else 0)-(if(change)2 else 0)-14*scale)/(body*scale*1.2f))
        fun text(view: Int,value: String,size: Float) {
            val desired=size*vertical.coerceAtLeast(.3f)
            val fitted=WidgetText.fit(context,value,desired,width-20)
            views.setTextViewText(view,value)
            views.setTextViewTextSize(view,TypedValue.COMPLEX_UNIT_SP,fitted)
        }
        text(R.id.widget_price,if(width>=240)WidgetAppearance.price(s.quote?.price,s.currency)else MiniWidget.price(s.quote?.price,s.currency),26+6*growth)
        text(R.id.compact_block,"Blok #${WidgetAppearance.number(s.block.value?.height)}",15+growth*2)
        text(R.id.widget_next,"${WidgetAppearance.fee(s.fees.value?.next)} sat/vB",14+growth)
        text(R.id.compact_change,"${WidgetAppearance.fixed(s.quote?.todayPercent,2)}% vandaag (UTC)",12f)
        views.setTextColor(R.id.compact_change,android.graphics.Color.parseColor(if((s.quote?.todayPercent ?: 0.0)<0)"#FF647C" else "#35E89A"))
        views.setViewVisibility(R.id.compact_change,if(change)View.VISIBLE else View.GONE)
        text(R.id.compact_half,"~30 min · ${WidgetAppearance.fee(s.fees.value?.halfHour)} sat/vB",12f)
        text(R.id.compact_hour,"~1 uur · ${WidgetAppearance.fee(s.fees.value?.hour)} sat/vB",12f)
        for(view in listOf(R.id.compact_half,R.id.compact_hour))views.setViewVisibility(view,if(estimates)View.VISIBLE else View.GONE)
        views.setContentDescription(R.id.widget_next,"Geschat tarief voor het volgende blok: ${WidgetAppearance.fee(s.fees.value?.next)} sat/vB")
        text(R.id.compact_age,"${WidgetAppearance.age(s.block.value?.timestamp,now)} geleden",12f)
        text(R.id.compact_tx,"${WidgetAppearance.number(s.block.value?.transactions)} tx · ${s.block.value?.pool ?: "—"}",12f)
        text(R.id.compact_median,"Mediaan blokfee ${WidgetAppearance.fixed(s.block.value?.medianFee,1)} sat/vB",12f)
        text(R.id.compact_mempool,s.mempool.value?.let { "Mempool ${WidgetAppearance.number(it.count)} tx · ${WidgetAppearance.fixed(it.virtualBytes/1000000.0,1)} vMB" } ?: "Mempool —",12f)
        for(view in listOf(R.id.compact_age,R.id.compact_tx)) views.setViewVisibility(view,if(more)View.VISIBLE else View.GONE)
        for(view in listOf(R.id.compact_median,R.id.compact_mempool)) views.setViewVisibility(view,if(details)View.VISIBLE else View.GONE)
        val sources=listOf(s.prices,s.fees,s.block)+if(details)listOf(s.mempool)else emptyList()
        val old=sources.any { it.value!=null && (it.error!=null || it.isStale(now)) }
        val stamp=sources.minOf { it.fetchedAt }
        text(R.id.widget_updated,when { busy || s.refreshing->"Vernieuwen…";old->"Oudere gegevens · ${Format.time(stamp)}";stamp==0L->"Gegevens laden…";else->"Bijgewerkt ${Format.time(stamp)}" },11f)
        views.setTextViewText(R.id.widget_currency,"BTC/${s.currency} ▾")
        views.setTextViewTextSize(R.id.widget_currency,TypedValue.COMPLEX_UNIT_SP,WidgetText.fit(context,"BTC/${s.currency} ▾",12f,width-68))
        views.setContentDescription(R.id.widget_currency,"Valuta ${s.currency}. Tik om te wisselen voor deze widget.")
        if(id==AppWidgetManager.INVALID_APPWIDGET_ID)return views
        val currency=Intent(context,PulseWidget::class.java).setAction(WidgetUpdates.CURRENCY).setData(Uri.parse("bitcoinpulse://widget/$id/currency"))
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id)
        views.setOnClickPendingIntent(R.id.widget_currency,PendingIntent.getBroadcast(context,id,currency,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        val refresh=Intent(context,PulseWidget::class.java).setAction(WidgetUpdates.REFRESH)
        views.setOnClickPendingIntent(R.id.widget_refresh,PendingIntent.getBroadcast(context,id,refresh,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(context,id,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        return views
    }
}
