package com.buildapptest.bitcoinpulse.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import com.buildapptest.bitcoinpulse.data.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CandlePanel(s: Snapshot, repository: PulseRepository, now: Long, detailed: Boolean = true) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().heightIn(min=48.dp), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Ichimoku cloud",fontSize=14.sp)
            Text("20 / 60 / 120 / 30",fontSize=11.sp,color=Color(0xFFBAC7DB))
        }
        Switch(checked=s.ichimoku,onCheckedChange={ repository.setIchimoku(it); com.buildapptest.bitcoinpulse.widget.WidgetUpdates.render(context) },
            modifier=Modifier.semantics { contentDescription="Ichimoku cloud voor alle grafieken" })
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (PERIODS.map { it.id to it.label } + ("combi" to "Combi")).forEach { (id,label) ->
            TextButton(onClick={ repository.setChartMode(id); com.buildapptest.bitcoinpulse.widget.WidgetUpdates.render(context) },
                modifier=Modifier.weight(1f).heightIn(min=48.dp), contentPadding=PaddingValues(2.dp),
                colors=ButtonDefaults.textButtonColors(containerColor=if(s.chartMode==id)Color(0xFFFF8A1F) else Color.Transparent,
                    contentColor=if(s.chartMode==id)Color(0xFF17120D) else Color(0xFFBAC7DB))) { Text(label,fontSize=12.sp,maxLines=1) }
        }
    }
    if(s.chartMode=="combi") {
        Text("Kies twee of drie intervallen",fontSize=12.sp,color=Color(0xFFBAC7DB))
        Row(Modifier.fillMaxWidth()) {
            PERIODS.forEach { period ->
                FilterChip(selected=period.id in s.combo, enabled=period.id !in s.combo || s.combo.size>2,
                    onClick={ repository.setCombo(if(period.id in s.combo)s.combo-period.id else s.combo+period.id); com.buildapptest.bitcoinpulse.widget.WidgetUpdates.render(context) },
                    label={Text(period.label,fontSize=12.sp)},modifier=Modifier.weight(1f).padding(horizontal=2.dp).heightIn(min=48.dp))
            }
        }
    }
    s.chartPeriods().forEach { period ->
      key(period.id) {
        val source=s.history(period.id)
        val history=source.value.orEmpty()
        var zoom by remember(period.id) { mutableIntStateOf(s.chartZoom[period.id] ?: ChartZoom.DEFAULT) }
        val candles=history.takeLast(ChartZoom.visible(zoom,history.size))
        var chosen by remember(s.currency,period.id) { mutableIntStateOf(-1) }
        val selected=chosen.takeIf { it in candles.indices } ?: candles.lastIndex
        val candle=candles.getOrNull(selected)
        Text("${period.label} per candle · ${s.currency}",fontSize=13.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density=LocalDensity.current
            val width=with(density){maxWidth.roundToPx()}.coerceAtLeast(240)
            val height=with(density){(if(s.chartMode=="combi")160.dp else 220.dp).roundToPx()}
            val chartDensity=density.density*density.fontScale
            val image=remember(history,width,height,selected,s.currency,chartDensity,s.ichimoku,zoom) { CandleBitmap.render(history,s.currency,width,height,selected,chartDensity,s.ichimoku,zoom).asImageBitmap() }
            Image(image,contentDescription="${period.label}-candles in ${s.currency}. ${candles.size} candles. Zoom met twee vingers of de toegankelijkheidsacties. Stijging: open candle, daling: gevuld. Datums in UTC. Tik op een candle voor selectie.",
                modifier=Modifier.fillMaxWidth().height(if(s.chartMode=="combi")160.dp else 220.dp).semantics {
                    customActions=listOf(
                        CustomAccessibilityAction("Inzoomen") { zoom=ChartZoom.step(zoom,false);chosen=-1;repository.setChartZoom(period.id,zoom);true },
                        CustomAccessibilityAction("Uitzoomen") { zoom=ChartZoom.step(zoom,true);chosen=-1;repository.setChartZoom(period.id,zoom);true })
                }.pointerInput(history,width,s.ichimoku,chartDensity) {
                    chartGestures(currentCount={zoom},onZoom={zoom=it;chosen=-1},onZoomEnd={repository.setChartZoom(period.id,zoom)}) { offset ->
                        val count=ChartZoom.visible(zoom,history.size)
                        val slots=count + if(s.ichimoku && history.size>=Ichimoku.required(count)) Ichimoku.OFFSET else 0
                        if(count>0) {
                            val index=((offset.x-3*chartDensity)/((width-68*chartDensity)/slots)).toInt()
                            if(offset.x>=3*chartDensity && index in 0 until count) chosen=index
                        }
                    }
                })
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text("Zoom met twee vingers",fontSize=11.sp,color=Color(0xFFBAC7DB))
            Text("${candles.size} candles",fontSize=11.sp,color=Color(0xFFBAC7DB))
        }
        if(s.ichimoku && history.size<Ichimoku.required(candles.size)) Text("Cloud: minstens ${Ichimoku.required(candles.size)} historische candles nodig. Vernieuw om te laden.",fontSize=12.sp,color=Color(0xFFFFD166))
        if(candle!=null && detailed) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                TextButton(onClick={chosen=(selected-1).coerceAtLeast(0)},enabled=selected>0) { Text("Vorige") }
                TextButton(onClick={chosen=(selected+1).coerceAtMost(candles.lastIndex)},enabled=selected<candles.lastIndex) { Text("Volgende") }
                TextButton(onClick={chosen=candles.lastIndex}) { Text("Laatste") }
            }
            val stamp=DateTimeFormatter.ofPattern("d MMM yyyy HH:mm 'UTC'").withZone(ZoneOffset.UTC).format(Instant.ofEpochSecond(candle.timestamp))
            Text("$stamp · ${if(candle.provisional)"In opbouw ●" else "Gesloten"}",fontSize=12.sp,color=Color(0xFFBAC7DB))
            Text("O ${Format.money(candle.open,s.currency)} · H ${Format.money(candle.high,s.currency)}\nL ${Format.money(candle.low,s.currency)} · C ${Format.money(candle.close,s.currency)}",fontSize=12.sp)
            Text("Volume ${String.format(Locale.getDefault(),"%.4f",candle.volume)} BTC",fontSize=12.sp,color=Color(0xFFBAC7DB))
        }
        Text("Kraken · ${source.status(now)} · as: UTC",fontSize=12.sp,color=if(source.isStale(now)||source.error!=null)Color(0xFFFFD166) else Color(0xFFBAC7DB))
      }
    }
}
