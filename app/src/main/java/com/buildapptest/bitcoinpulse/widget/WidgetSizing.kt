package com.buildapptest.bitcoinpulse.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.SizeF

enum class WidgetMode { MINI, COMPACT, NETWORK }

/** Actual content bounds, independent of the launcher's cell count or original provider. */
object WidgetSizing {
    fun mode(width: Int,height: Int) = when {
        width>=250 && height>=320 -> WidgetMode.NETWORK
        width>=160 && height>=160 -> WidgetMode.COMPACT
        else -> WidgetMode.MINI
    }
    internal val providers=listOf(PulseWidget::class.java,CompactWidget::class.java,MiniWidget::class.java)
    fun ids(context: Context): List<Int> = providers.flatMap { AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,it)).toList() }
    fun sizes(context: Context,id: Int): List<SizeF> {
        val manager=AppWidgetManager.getInstance(context)
        val provider=providers.firstOrNull { id in manager.getAppWidgetIds(ComponentName(context,it)) }
        val defaults=when(provider) { MiniWidget::class.java -> 80 to 88;CompactWidget::class.java -> 160 to 160;else -> 320 to 320 }
        val options=manager.getAppWidgetOptions(id)
        if(Build.VERSION.SDK_INT>=31) {
            @Suppress("DEPRECATION")
            val sizes=options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES).orEmpty().filter { it.width.isFinite() && it.height.isFinite() && it.width>0 && it.height>0 }.distinct().take(16)
            if(sizes.isNotEmpty())return sizes
        }
        val minWidth=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,defaults.first).coerceAtLeast(1)
        val minHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,defaults.second).coerceAtLeast(1)
        val maxWidth=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,minWidth).coerceAtLeast(minWidth)
        val maxHeight=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,minHeight).coerceAtLeast(minHeight)
        return listOf(SizeF(minWidth.toFloat(),maxHeight.toFloat()),SizeF(maxWidth.toFloat(),minHeight.toFloat())).distinct()
    }
    fun bounds(context: Context,id: Int): Pair<Int,Int> {
        val sizes=sizes(context,id)
        val landscape=context.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE
        val best=sizes.firstOrNull { (it.width>it.height)==landscape } ?: sizes.first()
        return best.width.toInt() to best.height.toInt()
    }
    fun hasNetworkSize(context: Context,id: Int)=sizes(context,id).any { mode(it.width.toInt(),it.height.toInt())==WidgetMode.NETWORK }
    fun mode(context: Context,id: Int) = bounds(context,id).let { (w,h)->mode(w,h) }
}
