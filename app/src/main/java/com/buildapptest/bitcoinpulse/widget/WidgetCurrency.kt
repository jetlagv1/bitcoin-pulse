package com.buildapptest.bitcoinpulse.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/** Each widget has the same independent EUR/USD choice as the OWL widget. */
object WidgetCurrency {
    private fun ids(context: Context,compact: Boolean=false)=AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context,if(compact)CompactWidget::class.java else PulseWidget::class.java))
    fun read(context: Context,id: Int,fallback: String): String =
        context.getSharedPreferences("widget-options",0).getString("currency-$id",fallback).let { if(it=="USD")"USD" else "EUR" }
    fun toggle(context: Context,id: Int,fallback: String): Boolean {
        return set(context,id,if(read(context,id,fallback)=="EUR")"USD" else "EUR")
    }
    fun set(context: Context,id: Int,currency: String): Boolean {
        if(currency !in listOf("EUR","USD") || (id !in ids(context) && id !in ids(context,true) && id !in MiniWidget.ids(context))) return false
        context.getSharedPreferences("widget-options",0).edit().putString("currency-$id",currency).apply()
        return true
    }
    fun required(context: Context,fallback: String) = (listOf(fallback)+ids(context).map { read(context,it,fallback) }).toSet()
}
