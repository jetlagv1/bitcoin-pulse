package com.buildapptest.bitcoinpulse.widget

import android.content.Context
import android.content.ComponentName
import android.appwidget.AppWidgetManager
import com.buildapptest.bitcoinpulse.data.*

data class WidgetCharts(val first: String="4h",val second: String="1d",val count: Int=2) {
    init { require(count in 1..2 && first!=second && listOf(first,second).all { p -> PERIODS.any { it.id==p } }) }
    val periods get() = listOf(first,second).take(count).map { id -> PERIODS.single { it.id==id } }
    fun choose(slot: Int,period: String): WidgetCharts? {
        if(slot !in 0 until count || PERIODS.none { it.id==period }) return null
        if(count==2 && period==(if(slot==0)second else first)) return null
        return if(slot==0) copy(first=period,second=if(second==period)PERIODS.first { it.id!=period }.id else second) else copy(second=period)
    }
    fun save(context: Context,id: Int) {
        context.getSharedPreferences("widget-options",0).edit().putString("charts-$id","$first,$second").putInt("chart-count-$id",count).apply()
    }
    companion object {
        fun defaultCount(context: Context) = context.getSharedPreferences("widget-options",0).getInt("default-chart-count",2).coerceIn(1,2)
        /** App setting applies to existing widgets as well as widgets added later. */
        fun configureCount(context: Context,count: Int) {
            require(count in 1..2)
            context.getSharedPreferences("widget-options",0).edit().putInt("default-chart-count",count).apply()
            AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,PulseWidget::class.java)).forEach { id ->
                read(context,id).copy(count=count).save(context,id)
                context.getSharedPreferences("widget-options",0).edit().remove("picker-$id").apply()
            }
            WidgetUpdates.render(context)
        }
        fun owned(context: Context,id: Int) = id in AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,PulseWidget::class.java))
        fun read(context: Context,id: Int,snapshot: Snapshot=Snapshot()): WidgetCharts {
            val prefs=context.getSharedPreferences("widget-options",0)
            val saved=prefs.getString("charts-$id",null)?.split(",")?.filter { p -> PERIODS.any { it.id==p } }?.distinct()
            if(saved?.size==2) return WidgetCharts(saved[0],saved[1],prefs.getInt("chart-count-$id",2).coerceIn(1,2))
            val old=prefs.getString("period-$id",snapshot.chartMode) ?: "4h"
            val selected=snapshot.chartPeriods(old).map { it.id }.ifEmpty { listOf("4h") }.take(2)
            return WidgetCharts(selected[0],selected.getOrNull(1) ?: PERIODS.first { it.id!=selected[0] }.id,if(prefs.contains("default-chart-count"))defaultCount(context) else if(old=="combi")2 else 1)
        }
        fun select(context: Context,id: Int,slot: Int,period: String): Boolean {
            if(!owned(context,id)) return false
            val updated=read(context,id).choose(slot,period) ?: return false
            updated.save(context,id);return true
        }
        fun setCount(context: Context,id: Int,count: Int): Boolean {
            if(!owned(context,id) || count !in 1..2) return false
            read(context,id).copy(count=count).save(context,id);return true
        }
    }
}
