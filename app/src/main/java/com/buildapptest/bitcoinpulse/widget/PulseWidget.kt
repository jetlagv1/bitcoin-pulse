package com.buildapptest.bitcoinpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import android.view.View
import android.net.Uri
import androidx.work.*
import com.buildapptest.bitcoinpulse.MainActivity
import com.buildapptest.bitcoinpulse.PulseApplication
import com.buildapptest.bitcoinpulse.R
import com.buildapptest.bitcoinpulse.data.Format
import com.buildapptest.bitcoinpulse.data.Snapshot
import com.buildapptest.bitcoinpulse.data.Ichimoku
import com.buildapptest.bitcoinpulse.data.ChartZoom
import com.buildapptest.bitcoinpulse.data.PERIODS
import com.buildapptest.bitcoinpulse.ui.CandleBitmap
import java.util.concurrent.TimeUnit

open class PulseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetUpdates.render(context)
        WidgetUpdates.schedule(context)
        WidgetUpdates.refresh(context)
    }
    override fun onEnabled(context: Context) { WidgetUpdates.schedule(context) }
    override fun onDisabled(context: Context) {
        if (!WidgetUpdates.hasWidgets(context)) WorkManager.getInstance(context).cancelUniqueWork("pulse-periodic")
    }
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) { WidgetUpdates.render(context) }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdates.REFRESH) WidgetUpdates.refresh(context)
        if (intent.action == WidgetUpdates.ZOOM) {
            val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
            val period=intent.getStringExtra("period") ?: ""
            val direction=intent.getIntExtra("direction",0)
            if(direction in listOf(-1,1) && WidgetUpdates.changeZoom(context,id,period,direction==1)) WidgetUpdates.render(context)
        }
        if (intent.action == WidgetUpdates.CLOUD) {
            val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
            val owned=AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,PulseWidget::class.java))
            if(id in owned) {
                val prefs=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE)
                val fallback=(context.applicationContext as PulseApplication).repository.state.value.ichimoku
                prefs.edit().putBoolean("ichimoku-$id",!prefs.getBoolean("ichimoku-$id",fallback)).apply()
                WidgetUpdates.render(context)
            }
        }
        if (intent.action == WidgetUpdates.PERIOD) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val mode = intent.getStringExtra("period")
            val owned = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, PulseWidget::class.java))
            if (id in owned && mode in listOf("4h", "1d", "1w", "combi")) {
                context.getSharedPreferences("widget-options", Context.MODE_PRIVATE).edit().putString("period-$id", mode).apply()
                WidgetUpdates.render(context)
            }
        }
    }
    override fun onDeleted(context: Context, ids: IntArray) {
        val editor=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).edit()
        ids.forEach { id -> editor.remove("period-$id");editor.remove("ichimoku-$id");PERIODS.forEach { editor.remove("zoom-$id-${it.id}") } }; editor.apply()
    }
}
class CompactWidget : PulseWidget()

object WidgetUpdates {
    const val REFRESH = "com.buildapptest.bitcoinpulse.REFRESH"
    const val PERIOD = "com.buildapptest.bitcoinpulse.PERIOD"
    const val CLOUD = "com.buildapptest.bitcoinpulse.CLOUD"
    const val ZOOM = "com.buildapptest.bitcoinpulse.ZOOM"
    private val minus=listOf(R.id.chart_minus_1,R.id.chart_minus_2,R.id.chart_minus_3)
    private val plus=listOf(R.id.chart_plus_1,R.id.chart_plus_2,R.id.chart_plus_3)
    internal fun zooms(context: Context,id: Int) = PERIODS.associate {
        it.id to ChartZoom.clamp(context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).getInt("zoom-$id-${it.id}",ChartZoom.DEFAULT))
    }
    internal fun changeZoom(context: Context,id: Int,period: String,out: Boolean): Boolean {
        val owned=AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,PulseWidget::class.java))
        if(id !in owned || PERIODS.none { it.id==period }) return false
        context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).edit()
            .putInt("zoom-$id-$period",ChartZoom.step(zooms(context,id).getValue(period),out)).apply()
        return true
    }
    private val providers = listOf(PulseWidget::class.java, CompactWidget::class.java)
    fun hasWidgets(context: Context): Boolean = providers.any {
        AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, it)).isNotEmpty()
    }
    fun schedule(context: Context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("pulse-periodic", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<PulseWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
    }
    fun refresh(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork("pulse-manual", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<PulseWorker>().build())
    }
    fun render(context: Context, busy: Boolean = false) {
        val snapshot = (context.applicationContext as PulseApplication).repository.state.value
        val manager = AppWidgetManager.getInstance(context)
        val now = System.currentTimeMillis()
        providers.forEach { provider ->
            val compact = provider == CompactWidget::class.java
            manager.getAppWidgetIds(ComponentName(context, provider)).forEach { id ->
                val options=manager.getAppWidgetOptions(id)
                val height=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,390)
                val width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320)
                val small=compact || height<360
                val views = RemoteViews(context.packageName, if (small) R.layout.widget_compact else R.layout.widget_expanded)
                views.setTextViewText(R.id.widget_currency, if (small) "BTC/${snapshot.currency}" else "Bitcoin · ${snapshot.currency}")
                views.setTextViewText(R.id.widget_price, Format.money(snapshot.quote?.price, snapshot.currency, small))
                views.setTextViewText(R.id.widget_next, if (small) "Next ${Format.fee(snapshot.fees.value?.next)} sat/vB" else Format.fee(snapshot.fees.value?.next))
                val mode=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).getString("period-$id",snapshot.chartMode) ?: "4h"
                if (!small) {
                    views.setTextViewText(R.id.widget_half, Format.fee(snapshot.fees.value?.halfHour))
                    views.setTextViewText(R.id.widget_hour, Format.fee(snapshot.fees.value?.hour))
                    views.setTextViewText(R.id.widget_block, "Blok ${Format.number(snapshot.block.value?.height)}")
                    views.setTextColor(R.id.widget_block, Color.parseColor("#49CFFF"))
                    views.setTextViewText(R.id.widget_change, "${Format.percent(snapshot.quote)} vandaag UTC")
                    views.setTextColor(R.id.widget_change, Color.parseColor(if ((snapshot.quote?.todayPercent ?: 0.0) < 0) "#FF647C" else "#35E89A"))
                    views.setTextViewText(R.id.widget_age, "${Format.blockAge(snapshot.block.value, now)} · ${Format.time(now)}")
                    views.setContentDescription(R.id.widget_age,"Blokleeftijd op moment van weergave: ${Format.blockAge(snapshot.block.value,now)} om ${Format.time(now)}; geen afteller")
                    views.setTextViewText(R.id.widget_tx, Format.number(snapshot.block.value?.transactions))
                    views.setTextViewText(R.id.widget_pool, snapshot.block.value?.pool ?: "Onbekend")
                    views.setTextViewText(R.id.widget_median, "${Format.fee(snapshot.block.value?.medianFee)} sat/vB")
                    val cloud=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).getBoolean("ichimoku-$id",snapshot.ichimoku)
                    views.setTextViewText(R.id.widget_ichimoku,"Cloud\n${if(cloud)"Aan" else "Uit"}")
                    views.setTextColor(R.id.widget_ichimoku,Color.parseColor(if(cloud)"#FF8A1F" else "#BAC7DB"))
                    views.setContentDescription(R.id.widget_ichimoku,"Ichimoku cloud ${if(cloud)"aan" else "uit"}; 20 / 60 / 120 / 30. Tik om te wisselen.")
                    val toggle=Intent(context,PulseWidget::class.java).setAction(CLOUD).setData(Uri.parse("bitcoinpulse://widget/$id/cloud"))
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id)
                    views.setOnClickPendingIntent(R.id.widget_ichimoku,PendingIntent.getBroadcast(context,id,toggle,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    charts(views,snapshot,mode,width,height,now,cloud,zooms(context,id))
                    snapshot.chartPeriods(mode).forEachIndexed { i,period ->
                        for((viewId,direction) in listOf(minus[i] to 1,plus[i] to -1)) {
                            val zoom=Intent(context,PulseWidget::class.java).setAction(ZOOM)
                                .setData(Uri.parse("bitcoinpulse://widget/$id/zoom/${period.id}/$direction"))
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("period",period.id).putExtra("direction",direction)
                            views.setOnClickPendingIntent(viewId,PendingIntent.getBroadcast(context,id,zoom,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                        }
                    }
                    listOf(R.id.period_4h to "4h",R.id.period_1d to "1d",R.id.period_1w to "1w",R.id.period_combi to "combi").forEach { (viewId,period) ->
                        views.setTextColor(viewId,Color.parseColor(if(period==mode)"#17120D" else "#BAC7DB"))
                        views.setInt(viewId,"setBackgroundResource",if(period==mode)R.drawable.widget_period_selected else R.drawable.widget_inner)
                        views.setContentDescription(viewId,"$period${if(period==mode) ", geselecteerd" else ""}")
                        val action=Intent(context,PulseWidget::class.java).setAction(PERIOD)
                            .setData(Uri.parse("bitcoinpulse://widget/$id/$period"))
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("period",period)
                        views.setOnClickPendingIntent(viewId,PendingIntent.getBroadcast(context,id,action,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    }
                }
                val required=mutableListOf(snapshot.prices.fetchedAt,snapshot.fees.fetchedAt)
                if(!small) { required+=snapshot.block.fetchedAt; required+=snapshot.chartPeriods(mode).map { snapshot.history(it.id).fetchedAt } }
                val oldest = required.minOrNull() ?: 0
                val errors = if (small) snapshot.prices.error != null || snapshot.fees.error != null
                    else snapshot.prices.error!=null || snapshot.fees.error!=null || snapshot.block.error!=null || snapshot.chartPeriods(mode).any { snapshot.history(it.id).error!=null }
                val status = when {
                    small && !compact -> "Vergroot tot 390 dp voor grafiek"
                    busy || snapshot.refreshing -> "Refreshing…"
                    oldest == 0L -> "Waiting for data · tap refresh"
                    errors -> "Cached · oldest ${Format.time(oldest)}"
                    now < oldest || now - oldest > 20 * 60_000L -> "Older data · ${Format.time(oldest)}"
                    else -> "Oldest update ${Format.time(oldest)}"
                }
                views.setTextViewText(R.id.widget_updated, status)
                views.setTextColor(R.id.widget_updated, Color.parseColor(if (errors || oldest == 0L || now - oldest > 20 * 60_000L) "#FFD166" else "#BAC7DB"))
                val open = PendingIntent.getActivity(context, id, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_root, open)
                val refresh = PendingIntent.getBroadcast(context, id, Intent(context, provider).setAction(REFRESH), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_refresh, refresh)
                manager.updateAppWidget(id, views)
            }
        }
    }

    internal fun charts(views: RemoteViews, s: Snapshot, mode: String, width: Int, height: Int, now: Long, ichimoku: Boolean = s.ichimoku, zooms: Map<String,Int> = emptyMap()) {
        val periods=s.chartPeriods(mode)
        val needed=when(periods.size) { 3 -> 600; 2 -> 500; else -> 390 }
        val fits=height>=needed
        val labels=listOf(R.id.chart_label_1,R.id.chart_label_2,R.id.chart_label_3)
        val headers=listOf(R.id.chart_header_1,R.id.chart_header_2,R.id.chart_header_3)
        val counts=listOf(R.id.chart_count_1,R.id.chart_count_2,R.id.chart_count_3)
        val images=listOf(R.id.chart_image_1,R.id.chart_image_2,R.id.chart_image_3)
        views.setViewVisibility(R.id.chart_hint,if(fits)View.GONE else View.VISIBLE)
        views.setTextViewText(R.id.chart_hint,"Vergroot tot $needed dp hoog voor ${periods.size} grafiek(en), of open de app. Kies één interval voor minder ruimte.")
        for(i in 0..2) {
            val period=periods.getOrNull(i)
            val show=fits && period!=null
            views.setViewVisibility(labels[i],if(show)View.VISIBLE else View.GONE)
            views.setViewVisibility(headers[i],if(show)View.VISIBLE else View.GONE)
            views.setViewVisibility(images[i],if(show)View.VISIBLE else View.GONE)
            if(show) {
                val source=s.history(period.id)
                val zoom=ChartZoom.clamp(zooms[period.id] ?: ChartZoom.DEFAULT)
                val visible=ChartZoom.visible(zoom,source.value.orEmpty().size)
                views.setTextViewText(counts[i],"$visible")
                views.setContentDescription(counts[i],"$visible candles")
                for((id,enabled,label) in listOf(Triple(minus[i],zoom<ChartZoom.MAX,"Uitzoomen"),Triple(plus[i],zoom>ChartZoom.MIN,"Inzoomen"))) {
                    views.setBoolean(id,"setEnabled",enabled)
                    views.setFloat(id,"setAlpha",if(enabled)1f else .3f)
                    views.setContentDescription(id,"$label ${period.label}; nu $visible candles")
                }
                val status=when { source.value==null -> "geen data"; source.error!=null -> "cache ${Format.time(source.fetchedAt)}"; source.isStale(now) -> "oud ${Format.time(source.fetchedAt)}"; else -> Format.time(source.fetchedAt) }
                val cloudHint=if(ichimoku && source.value.orEmpty().size<Ichimoku.required(visible)) " · cloud: vernieuw" else ""
                views.setTextViewText(labels[i],"${period.label} · $status · UTC · in opbouw ●$cloudHint")
                val w=(width.coerceIn(280,500)-24)*2
                val h=((height.coerceAtMost(800)-286)/periods.size-40).coerceIn(40,300)*2
                views.setImageViewBitmap(images[i],CandleBitmap.render(source.value.orEmpty(),s.currency,w,h,density=2f,ichimoku=ichimoku,visibleCount=zoom))
                val last=source.value?.lastOrNull()
                views.setContentDescription(images[i],"${period.label} per candle in ${s.currency}. ${source.status(now)}. Laatste slot ${Format.money(last?.close,s.currency)}; laatste candle in opbouw. Datums UTC. Tik om de details te openen.")
            }
        }
    }
}

class PulseWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = (applicationContext as PulseApplication).repository
        WidgetUpdates.render(applicationContext, busy = true)
        val complete = try { repository.refresh() } finally { WidgetUpdates.render(applicationContext) }
        return if (complete || runAttemptCount >= 2) Result.success() else Result.retry()
    }
}
