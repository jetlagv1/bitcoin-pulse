package com.buildapptest.bitcoinpulse.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.buildapptest.bitcoinpulse.data.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CandlePanel(s: Snapshot, repository: PulseRepository, now: Long, detailed: Boolean = true) {
    val context = LocalContext.current
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
        val source=s.history(period.id)
        val candles=source.value.orEmpty()
        var chosen by remember(s.currency,period.id) { mutableIntStateOf(-1) }
        val selected=chosen.takeIf { it in candles.indices } ?: candles.lastIndex
        val candle=candles.getOrNull(selected)
        Text("${period.label} per candle · ${s.currency}",fontSize=13.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density=LocalDensity.current
            val width=with(density){maxWidth.roundToPx()}.coerceAtLeast(240)
            val height=with(density){(if(s.chartMode=="combi")160.dp else 220.dp).roundToPx()}
            val chartDensity=density.density*density.fontScale
            val image=remember(candles,width,height,selected,s.currency,chartDensity) { CandleBitmap.render(candles,s.currency,width,height,selected,chartDensity).asImageBitmap() }
            Image(image,contentDescription="${period.label}-candles in ${s.currency}. Stijging: open candle, daling: gevuld. Datums in UTC. Selecteer met de knoppen hieronder.",
                modifier=Modifier.fillMaxWidth().height(if(s.chartMode=="combi")160.dp else 220.dp).pointerInput(candles,width) {
                    detectTapGestures { offset ->
                        if(candles.isNotEmpty()) chosen=((offset.x-3*chartDensity)/((width-68*chartDensity)/candles.size)).toInt().coerceIn(candles.indices)
                    }
                })
        }
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
