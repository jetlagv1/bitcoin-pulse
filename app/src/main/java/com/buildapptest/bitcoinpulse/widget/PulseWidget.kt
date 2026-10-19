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
        if (intent.action == WidgetUpdates.COUNT) {
            val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1)
            if(WidgetCharts.owned(context,id) && WidgetCharts.setCount(context,id,3-WidgetCharts.read(context,id).count)) WidgetUpdates.render(context)
        }
        if (intent.action == WidgetUpdates.PICKER || intent.action == WidgetUpdates.PERIOD) {
            val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1)
            val slot=intent.getIntExtra("slot",-1)
            if(WidgetCharts.owned(context,id)) {
                val prefs=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE)
                if(intent.action==WidgetUpdates.PICKER && slot in 0 until WidgetCharts.read(context,id).count) {
                    prefs.edit().putInt("picker-$id",if(prefs.getInt("picker-$id",-1)==slot)-1 else slot).apply()
                    WidgetUpdates.render(context)
                } else if(intent.action==WidgetUpdates.PERIOD && WidgetCharts.select(context,id,slot,intent.getStringExtra("period") ?: "")) {
                    prefs.edit().remove("picker-$id").apply()
                    WidgetUpdates.render(context)
                }
            }
        }
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
    }
    override fun onDeleted(context: Context, ids: IntArray) {
        val editor=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).edit()
        ids.forEach { id -> editor.remove("period-$id");editor.remove("picker-$id");editor.remove("ichimoku-$id");editor.remove("charts-$id");editor.remove("chart-count-$id");PERIODS.forEach { editor.remove("zoom-$id-${it.id}") } }; editor.apply()
    }
}
class CompactWidget : PulseWidget()

object WidgetUpdates {
    const val REFRESH = "com.buildapptest.bitcoinpulse.REFRESH"
    const val CLOUD = "com.buildapptest.bitcoinpulse.CLOUD"
    const val ZOOM = "com.buildapptest.bitcoinpulse.ZOOM"
    const val PICKER = "com.buildapptest.bitcoinpulse.PICKER"
    const val PERIOD = "com.buildapptest.bitcoinpulse.PERIOD"
    const val COUNT = "com.buildapptest.bitcoinpulse.COUNT"
    private val minus=listOf(R.id.chart_minus_1,R.id.chart_minus_2)
    private val plus=listOf(R.id.chart_plus_1,R.id.chart_plus_2)
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
                val height=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,WidgetAppearance.SINGLE_HEIGHT)
                val width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320)
                val small=compact
                val selection=WidgetCharts.read(context,id,snapshot)
                val dense=!small && WidgetAppearance.dense(height,selection.count)
                val views = RemoteViews(context.packageName, if (small) R.layout.widget_compact else if(selection.count==1) R.layout.widget_single else if(dense) R.layout.widget_expanded_dense else R.layout.widget_expanded)
                views.setTextViewText(R.id.widget_currency, if (small) "BTC/${snapshot.currency}" else "${snapshot.currency} ▾")
                views.setTextViewText(R.id.widget_price, if(small)Format.money(snapshot.quote?.price,snapshot.currency,true) else WidgetAppearance.price(snapshot.quote?.price,snapshot.currency))
                views.setTextViewText(R.id.widget_next, if (small) "Next ${Format.fee(snapshot.fees.value?.next)} sat/vB" else "${WidgetAppearance.fee(snapshot.fees.value?.next)} sat/vB")
                if (!small) {
                    views.setTextViewText(R.id.widget_graph_count,"${selection.count} ↔")
                    views.setContentDescription(R.id.widget_graph_count,"${selection.count} grafiek(en). Tik voor ${3-selection.count} grafiek(en).")
                    val countIntent=Intent(context,PulseWidget::class.java).setAction(COUNT).setData(Uri.parse("bitcoinpulse://widget/$id/count"))
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id)
                    views.setOnClickPendingIntent(R.id.widget_graph_count,PendingIntent.getBroadcast(context,id,countIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    if(selection.count==1) {
                        val inset=((if(height<400)5 else 8)*context.resources.displayMetrics.density).toInt()
                        views.setViewPadding(R.id.widget_root,inset,inset,inset,inset)
                    }
                    if(dense) views.setViewVisibility(R.id.widget_fee_row,if(selection.count==2 && height<400)View.GONE else View.VISIBLE)
                    views.setTextViewText(R.id.widget_half, "${WidgetAppearance.fee(snapshot.fees.value?.halfHour)} sat/vB")
                    views.setTextViewText(R.id.widget_hour, "${WidgetAppearance.fee(snapshot.fees.value?.hour)} sat/vB")
                    views.setTextViewText(R.id.widget_block, "Blok #${WidgetAppearance.number(snapshot.block.value?.height)}")
                    views.setTextViewTextSize(R.id.widget_price,android.util.TypedValue.COMPLEX_UNIT_SP,if(dense)26f else if(width<340)28f else 32f)
                    views.setContentDescription(R.id.widget_currency,"Valuta ${snapshot.currency}; tik om in de app te wijzigen")
                    views.setTextViewText(R.id.widget_mempool,snapshot.mempool.value?.let { "Mempool ${WidgetAppearance.number(it.count)} tx · ${WidgetAppearance.fixed(it.virtualBytes/1000000.0,1)} vMB" } ?: "Mempool laden…")
                    views.setTextColor(R.id.widget_block, Color.parseColor("#49CFFF"))
                    views.setTextViewText(R.id.widget_change, "${snapshot.quote?.let { WidgetAppearance.fixed(it.todayPercent,2) } ?: "—"}% vandaag (UTC) · Kraken")
                    views.setTextColor(R.id.widget_change, Color.parseColor(if ((snapshot.quote?.todayPercent ?: 0.0) < 0) "#FF647C" else "#35E89A"))
                    views.setTextViewText(R.id.widget_age, "${WidgetAppearance.age(snapshot.block.value?.timestamp, now)} geleden")
                    views.setContentDescription(R.id.widget_age,"Blokleeftijd op moment van weergave: ${Format.blockAge(snapshot.block.value,now)} om ${Format.time(now)}; geen afteller")
                    views.setTextViewText(R.id.widget_tx, "${WidgetAppearance.number(snapshot.block.value?.transactions)} transacties · ")
                    views.setTextViewText(R.id.widget_pool, snapshot.block.value?.pool ?: "Onbekend")
                    views.setTextViewText(R.id.widget_median, "Mediaan blokfee ${WidgetAppearance.fixed(snapshot.block.value?.medianFee,1)} sat/vB")
                    val cloud=context.getSharedPreferences("widget-options",Context.MODE_PRIVATE).getBoolean("ichimoku-$id",snapshot.ichimoku)
                    views.setTextViewText(R.id.widget_ichimoku,"Cloud ${if(cloud)"Aan" else "Uit"}")
                    views.setTextColor(R.id.widget_ichimoku,Color.parseColor(if(cloud)"#FF8A1F" else "#BAC7DB"))
                    views.setInt(R.id.widget_ichimoku,"setBackgroundResource",if(cloud)R.drawable.widget_cloud_on else R.drawable.widget_cloud_off)
                    views.setContentDescription(R.id.widget_ichimoku,"Ichimoku cloud ${if(cloud)"aan" else "uit"}; 20 / 60 / 120 / 30. Tik om te wisselen.")
                    val toggle=Intent(context,PulseWidget::class.java).setAction(CLOUD).setData(Uri.parse("bitcoinpulse://widget/$id/cloud"))
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id)
                    views.setOnClickPendingIntent(R.id.widget_ichimoku,PendingIntent.getBroadcast(context,id,toggle,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    selection.save(context,id)
                    val picker=context.getSharedPreferences("widget-options",0).getInt("picker-$id",-1).takeIf { it in 0 until selection.count } ?: -1
                    charts(views,snapshot,selection,width,height,now,cloud,zooms(context,id),picker)
                    selection.periods.forEachIndexed { i,period ->
                        val pick=Intent(context,PulseWidget::class.java).setAction(PICKER).setData(Uri.parse("bitcoinpulse://widget/$id/pick/$i"))
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("slot",i)
                        views.setOnClickPendingIntent(if(i==0)R.id.chart_label_1 else R.id.chart_label_2,PendingIntent.getBroadcast(context,id,pick,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                        PERIODS.forEachIndexed { j,choice ->
                            val choose=Intent(context,PulseWidget::class.java).setAction(PERIOD).setData(Uri.parse("bitcoinpulse://widget/$id/select/$i/${choice.id}"))
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("slot",i).putExtra("period",choice.id)
                            views.setOnClickPendingIntent(pickerChoices[i][j],PendingIntent.getBroadcast(context,id,choose,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                        }
                        for((viewId,direction) in listOf(minus[i] to 1,plus[i] to -1)) {
                            val zoom=Intent(context,PulseWidget::class.java).setAction(ZOOM)
                                .setData(Uri.parse("bitcoinpulse://widget/$id/zoom/${period.id}/$direction"))
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("period",period.id).putExtra("direction",direction)
                            views.setOnClickPendingIntent(viewId,PendingIntent.getBroadcast(context,id,zoom,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                        }
                    }
                }
                val required=mutableListOf(snapshot.prices.fetchedAt,snapshot.fees.fetchedAt)
                if(!small) { required+=snapshot.block.fetchedAt; required+=snapshot.mempool.fetchedAt; required+=selection.periods.map { snapshot.history(it.id).fetchedAt } }
                val oldest = required.minOrNull() ?: 0
                val errors = if (small) snapshot.prices.error != null || snapshot.fees.error != null
                    else snapshot.prices.error!=null || snapshot.fees.error!=null || snapshot.block.error!=null || snapshot.mempool.error!=null || selection.periods.any { snapshot.history(it.id).error!=null }
                val status = when {
                    busy || snapshot.refreshing -> "Refreshing…"
                    oldest == 0L -> "Waiting for data · tap refresh"
                    errors -> "Cached · oldest ${Format.time(oldest)}"
                    now < oldest || now - oldest > 20 * 60_000L -> "Older data · ${Format.time(oldest)}"
                    else -> "Bijgewerkt ${Format.time(oldest)}"
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

    private val pickerRows=listOf(R.id.chart_picker_1,R.id.chart_picker_2)
    private val pickerChoices=listOf(listOf(R.id.chart_pick_1_4h,R.id.chart_pick_1_1d,R.id.chart_pick_1_1w),listOf(R.id.chart_pick_2_4h,R.id.chart_pick_2_1d,R.id.chart_pick_2_1w))
    internal fun charts(views: RemoteViews, s: Snapshot, selection: WidgetCharts, width: Int, height: Int, now: Long, ichimoku: Boolean = s.ichimoku, zooms: Map<String,Int> = emptyMap(), picker: Int = -1) {
        val periods=selection.periods
        val dense=WidgetAppearance.dense(height,selection.count)
        val fits=true
        val labels=listOf(R.id.chart_label_1,R.id.chart_label_2)
        val headers=listOf(R.id.chart_header_1,R.id.chart_header_2)
        val counts=listOf(R.id.chart_count_1,R.id.chart_count_2)
        val images=listOf(R.id.chart_image_1,R.id.chart_image_2)
        val captions=listOf(R.id.chart_status_1,R.id.chart_status_2)
        views.setViewVisibility(R.id.chart_hint,if(fits)View.GONE else View.VISIBLE)
        for(i in 0..1) {
            val period=periods.getOrNull(i)
            val show=fits && period!=null
            views.setViewVisibility(labels[i],if(show)View.VISIBLE else View.GONE)
            views.setViewVisibility(headers[i],if(show)View.VISIBLE else View.GONE)
            views.setViewVisibility(images[i],if(show)View.VISIBLE else View.GONE)
            views.setViewVisibility(captions[i],if(show && (selection.count==1 || (!dense && picker<0)))View.VISIBLE else View.GONE)
            views.setViewVisibility(pickerRows[i],if(show && picker==i)View.VISIBLE else View.GONE)
            PERIODS.forEachIndexed { j,choice ->
                val available=selection.count==1 || choice.id!=(if(i==0)selection.second else selection.first)
                views.setBoolean(pickerChoices[i][j],"setEnabled",available)
                views.setTextColor(pickerChoices[i][j],Color.parseColor(if(!available)"#718096" else if(choice.id==period?.id)"#FF8A1F" else "#F7FAFF"))
            }
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
                views.setTextViewText(labels[i],"${period.label} ▾")
                views.setTextViewText(captions[i],when { cloudHint.isNotEmpty() -> "Cloud: vernieuw voor meer historie"; source.value==null -> "Candles laden…"; source.error!=null -> "Cache · vernieuwen mislukt · ${Format.time(source.fetchedAt)}"; else -> "Laatste candle loopt nog · ${Format.time(source.fetchedAt)}" })
                views.setTextColor(captions[i],Color.parseColor(if(source.error!=null || source.isStale(now) || cloudHint.isNotEmpty())"#FFD166" else "#BAC7DB"))
                views.setTextColor(labels[i],Color.parseColor(if(source.error!=null || source.isStale(now))"#FFD166" else "#F7FAFF"))
                views.setContentDescription(labels[i],"Kies interval voor grafiek ${i+1}; ${period.label} · $status · UTC · in opbouw$cloudHint")
                val w=(width.coerceIn(250,500)-if(dense)16 else 28)*2
                val h=(WidgetAppearance.plotHeight(height,selection.count)-if(picker>=0 && dense)32/selection.count else 0).coerceAtLeast(24)*2
                views.setImageViewBitmap(images[i],CandleBitmap.render(source.value.orEmpty(),s.currency,w,h,density=2f,ichimoku=ichimoku,visibleCount=zoom,widgetStyle=true))
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
