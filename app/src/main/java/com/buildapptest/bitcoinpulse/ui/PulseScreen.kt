package com.buildapptest.bitcoinpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildapptest.bitcoinpulse.data.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Back = Color(0xFF0C1018)
private val Card = Color(0xFF182230)
private val Raised = Color(0xFF243246)
private val Ink = Color(0xFFF7FAFF)
private val Muted = Color(0xFFBAC7DB)
private val Orange = Color(0xFFFF8A1F)
private val Green = Color(0xFF35E89A)
private val Red = Color(0xFFFF647C)
private val Amber = Color(0xFFFFD166)
private val Cyan = Color(0xFF49CFFF)
private val Numeric = TextStyle(fontFeatureSettings = "tnum", fontWeight = FontWeight.SemiBold)

@Composable
fun PulseScreen(repository: PulseRepository, onRefresh: () -> Unit, onCurrency: (String) -> Unit, onPin: (Boolean) -> Unit,initialWidgets: Boolean=false) {
    val snapshot by repository.state.collectAsStateWithLifecycle()
    var widgets by rememberSaveable { mutableStateOf(initialWidgets) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    MaterialTheme(colorScheme = darkColorScheme(primary = Orange, onPrimary = Color(0xFF17120D), background = Back,
        surface = Card, onSurface = Ink, onSurfaceVariant = Muted, secondary = Cyan, onSecondary = Back,
        secondaryContainer = Raised, onSecondaryContainer = Ink, outline = Color(0xFF38485F))) {
        Surface(Modifier.fillMaxSize(), color = Back) {
            Column(Modifier.safeDrawingPadding().fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                .widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(8.dp))
                Header(snapshot, now, onRefresh)
                Row(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(28.dp)).padding(4.dp)) {
                    TabButton("Dashboard", !widgets, Modifier.weight(1f)) { widgets = false }
                    TabButton("Widgets", widgets, Modifier.weight(1f)) { widgets = true }
                }
                if (snapshot.refreshing) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Orange, trackColor = Raised)
                if (!snapshot.hasAnyData && !snapshot.refreshing) {
                    Text("Maak verbinding met internet en tik op vernieuwen om Bitcoin-gegevens te laden.", color = Amber, fontSize = 14.sp)
                } else if (snapshot.hasErrors) {
                    Text("Sommige bronnen konden niet worden vernieuwd. De laatst opgehaalde waarden blijven zichtbaar met hun oorspronkelijke tijdstempels.", color = Amber, fontSize = 14.sp)
                }
                if (widgets) {
                    WidgetPage(snapshot, repository, now, onRefresh, onPin)
                } else {
                    PriceCard(snapshot, repository, now, onCurrency)
                    FeesCard(snapshot, now)
                    NetworkCard(snapshot, now)
                    FilledTonalButton(onClick = { widgets = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(24.dp)) {
                        Icon(Icons.Outlined.Widgets, contentDescription = null)
                        Spacer(Modifier.width(10.dp)); Text("Toevoegen aan startscherm")
                    }
                }
                Text("De app vernieuwt ongeveer elke 30 seconden zolang deze openstaat. Widgets vragen elke 15 minuten een achtergrondverversing aan; Android kan die uitstellen om de batterij te sparen.",
                    color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                SourceLinks()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Header(s: Snapshot, now: Long, onRefresh: () -> Unit) {
    val fresh = s.hasAnyData && !s.hasErrors && s.oldestTimestamp() > 0 && now - s.oldestTimestamp() in 0..120_000
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Bitcoin Pulse", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text("●  Mainnet · ${if (fresh) "Live data" else "Latest available data"}", fontSize = 13.sp, color = if (fresh) Green else Muted)
        }
        IconButton(onClick = onRefresh, enabled = !s.refreshing, modifier = Modifier.size(48.dp).background(Card, RoundedCornerShape(24.dp))) {
            Icon(Icons.Outlined.Refresh, "Refresh all data", tint = if (s.refreshing) Muted else Orange)
        }
    }
}

@Composable
private fun TabButton(label: String, active: Boolean, modifier: Modifier, action: () -> Unit) {
    TextButton(onClick = action, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.textButtonColors(containerColor = if (active) Raised else Color.Transparent, contentColor = if (active) Ink else Muted)) { Text(label) }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Card, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
private fun PriceCard(s: Snapshot, repository: PulseRepository, now: Long, onCurrency: (String) -> Unit) {
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.background(Raised, RoundedCornerShape(24.dp)).padding(4.dp)) {
                listOf("EUR", "USD").forEach { code ->
                    TextButton(onClick = { onCurrency(code) }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.textButtonColors(
                        contentColor = if (s.currency == code) Ink else Muted,
                        containerColor = if (s.currency == code) Color(0xFF38485F) else Color.Transparent)) { Text(code) }
                }
            }
            Text("Kraken", fontSize = 12.sp, color = Muted)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(Format.money(s.quote?.price, s.currency), style = Numeric, fontSize = 40.sp, lineHeight = 46.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${Format.percent(s.quote)}  Today (UTC)", fontSize = 14.sp, color = if ((s.quote?.todayPercent ?: 0.0) < 0) Red else Green)
        }
        CandlePanel(s, repository, now)
        Row(Modifier.fillMaxWidth()) {
            Metric("24h high", Format.money(s.quote?.high, s.currency), Modifier.weight(1f), 14)
            Metric("24h low", Format.money(s.quote?.low, s.currency), Modifier.weight(1f), 14)
        }
        SourceStatus("Kraken", s.prices, now)
    }
}

@Composable
private fun FeesCard(s: Snapshot, now: Long) {
    Panel {
        Text("Network fees", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        FeeRow(s)
        Text("sat/vB · bevestigingstijden zijn schattingen", color = Muted, fontSize = 12.sp)
        val sats = s.fees.value?.next?.times(250)
        Text("Voorbeeld · 250 vB: ${Format.fee(sats)} sat ≈ ${Format.preciseMoney(sats?.let { it / 100_000_000 * (s.quote?.price ?: 0.0) }?.takeIf { s.quote != null }, s.currency)}", color = Muted, fontSize = 12.sp)
        SourceStatus("mempool.space", s.fees, now)
    }
}

@Composable
private fun FeeRow(s: Snapshot) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Triple("Next block", s.fees.value?.next, Orange), Triple("~30 min", s.fees.value?.halfHour, Ink), Triple("~1 hour", s.fees.value?.hour, Ink)).forEach { (label, value, color) ->
            Column(Modifier.weight(1f).background(Raised, RoundedCornerShape(16.dp)).padding(vertical = 12.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Format.fee(value), color = color, fontSize = 24.sp, style = Numeric)
                Text(label, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NetworkCard(s: Snapshot, now: Long) {
    Panel {
        Text("Netwerkactiviteit", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Cyan)
        Row(Modifier.fillMaxWidth()) {
            Metric("Laatste blok", Format.number(s.block.value?.height), Modifier.weight(1f))
            Metric("Blokleeftijd", Format.blockAge(s.block.value, now), Modifier.weight(1f))
        }
        SourceStatus("Blokken", s.block, now)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Transacties", Format.number(s.block.value?.transactions), Modifier.weight(1f), 15)
            Metric("Minerpool", s.block.value?.pool ?: "Onbekend", Modifier.weight(1f), 15)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Mediaan blokfee", "${Format.fee(s.block.value?.medianFee)} sat/vB", Modifier.weight(1f), 14)
            Metric("Blokvulling (gewicht)", s.block.value?.fullness?.let { String.format(Locale.getDefault(), "%.1f%%", it) } ?: "—", Modifier.weight(1f), 15)
        }
        Text("Fee-bereik: ${Format.fee(s.block.value?.feeMin)}–${Format.fee(s.block.value?.feeMax)} sat/vB", color=Muted,fontSize=12.sp)
        Text("Totale blokfees: ${s.block.value?.totalFees?.let { String.format(Locale.getDefault(), "%.8f BTC", it/100_000_000.0) } ?: "—"}",fontSize=13.sp)
        Text("Blokbeloning incl. fees: ${s.block.value?.reward?.let { String.format(Locale.getDefault(), "%.8f BTC", it/100_000_000.0) } ?: "—"}",color=Muted,fontSize=12.sp)
        Text("Poolherkenning door mempool.space. Blokfees zijn gerealiseerd; nieuw fee-advies staat hierboven. Blokleeftijd is berekend uit de laatst opgehaalde blokgegevens.",color=Muted,fontSize=12.sp)
        HorizontalDivider(color = Raised)
        Row(Modifier.fillMaxWidth()) {
            Metric("Pending transactions", Format.number(s.mempool.value?.count), Modifier.weight(1f))
            val vmb = s.mempool.value?.let { String.format(Locale.getDefault(), "%.1f vMB", it.virtualBytes / 1_000_000.0) } ?: "—"
            Metric("Mempool size", vmb, Modifier.weight(1f))
        }
        SourceStatus("Mempool", s.mempool, now)
        HorizontalDivider(color=Raised)
        Text("Difficulty", fontSize=18.sp,fontWeight=FontWeight.Medium,color=Cyan)
        val difficulty=s.difficulty.value
        Text("Geschatte wijziging: ${difficulty?.let { String.format(Locale.getDefault(), "%+.2f%%",it.change) } ?: "—"}",fontSize=15.sp)
        if(difficulty!=null) {
            LinearProgressIndicator(progress={ (difficulty.progress/100).toFloat().coerceIn(0f,1f) },modifier=Modifier.fillMaxWidth(),color=Orange,trackColor=Raised)
            Text("${String.format(Locale.getDefault(), "%.1f%%", difficulty.progress)} van periode · ${Format.number(difficulty.remainingBlocks)} blokken resterend",color=Muted,fontSize=12.sp)
        }
        SourceStatus("Difficulty · schatting",s.difficulty,now)
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier, size: Int = 18) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(value, style = Numeric, fontSize = size.sp)
    }
}

@Composable
private fun <T> SourceStatus(label: String, source: Source<T>, now: Long) {
    val stamp = if (source.fetchedAt > 0) DateTimeFormatter.ofPattern("d MMM, HH:mm:ss z").withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(source.fetchedAt)) else "No successful update yet"
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$label · ${source.status(now)}", color = if (source.error != null || source.isStale(now)) Amber else Muted, fontSize = 12.sp)
        Text(stamp, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun WidgetPage(s: Snapshot, repository: PulseRepository, now: Long, onRefresh: () -> Unit, onPin: (Boolean) -> Unit) {
    val context = LocalContext.current
    Text("Your home screen, at a glance", fontSize = 18.sp, fontWeight = FontWeight.Medium)
    Text("Tik in de widget op EUR/USD om de valuta te wisselen. Grafieken, intervallen, cloud en zoom bedien je ook rechtstreeks in de widget.", color = Muted, fontSize = 14.sp)
    var widgetCount by remember { mutableIntStateOf(com.buildapptest.bitcoinpulse.widget.WidgetCharts.defaultCount(context)) }
    Text("Aantal grafieken in de widget",fontSize=16.sp)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        for(count in 1..2) FilterChip(selected=widgetCount==count,onClick={
            widgetCount=count
            com.buildapptest.bitcoinpulse.widget.WidgetCharts.configureCount(context,count)
        },label={ Text(if(count==1)"1 grafiek" else "2 grafieken") })
    }
    Text("Geldt direct voor standaardwidgets. Voor Omalauncher: kies het aantal en voeg de widget hieronder opnieuw toe. De dashboardgrafieken blijven ongewijzigd.",color=Muted,fontSize=12.sp)
    Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Bitcoin · ${s.currency}", color = Muted, fontSize = 12.sp)
                Text(Format.money(s.quote?.price, s.currency), style = Numeric, fontSize = 30.sp)
            }
            Text("Block\n${Format.number(s.block.value?.height)}", fontSize = 12.sp, color = Muted)
        }
        Text("${Format.percent(s.quote)} vandaag (UTC)",color=if((s.quote?.todayPercent ?: 0.0)<0)Red else Green,fontSize=12.sp)
        CandlePanel(s, repository, now, detailed=false,widgetPreviewCount=widgetCount)
        Text("Blok ${Format.number(s.block.value?.height)} · ${Format.blockAge(s.block.value,now)} geleden",fontSize=14.sp)
        Text("${Format.number(s.block.value?.transactions)} tx · ${s.block.value?.pool ?: "Onbekende pool"} · mediaan ${Format.fee(s.block.value?.medianFee)} sat/vB",color=Muted,fontSize=12.sp)
        FeeRow(s)
        Text("Fee estimates · sat/vB", fontSize = 12.sp, color = Muted)
        Text("${Format.number(s.mempool.value?.count)} transactions pending", fontSize = 12.sp, color = Muted)
        WidgetFooter(s, false, now, onRefresh)
    }
    Button(onClick = { onPin(false) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Add network widget") }
    Text("De grafiekwidget schaalt mee, ook op 4×5. Kies hierboven één of twee grafieken, of wissel in de widget met 1 ↔ / 2 ↔ naast verversen. Tik in de widget op de grafiektitel voor 4 uur, 1 dag of 1 week. De −/+ knoppen zoomen per grafiek; het getal ertussen is het aantal candles. Bij één grafiek blijft alle informatie zichtbaar en groeit de grafiek mee. Bij twee grafieken is de netwerkinfo compacter. De intervalkeuze blijft binnen de widget.",color=Muted,fontSize=12.sp)
    androidx.compose.ui.viewinterop.AndroidView(
        factory={ ctx -> com.buildapptest.bitcoinpulse.widget.CompactViews.views(ctx,android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID,s,200,200,now).apply(ctx,android.widget.FrameLayout(ctx)) },
        update={ view -> com.buildapptest.bitcoinpulse.widget.CompactViews.views(context,android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID,s,200,200,now).reapply(context,view) },
        modifier=Modifier.size(200.dp,200.dp))
    OutlinedButton(onClick = { onPin(true) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Add compact widget") }
    Text("Alle widgets groeien automatisch van mini naar compact naar network zodra er genoeg breedte en hoogte is. Verkleinen behoudt je valuta en grafiekkeuzes.",color=Muted,fontSize=12.sp)
    MiniWidgetsPanel(s,now)
    Text("Deze voorbeelden tonen je laatst opgehaalde gegevens. Je kunt ook je startscherm ingedrukt houden, Widgets kiezen en vervolgens Bitcoin Pulse. De widgetmaten hangen af van je launcher.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    Panel {
        Text("Omalauncher", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Cyan)
        Text("Voeg de speciale OWL-widget toe met koers, candles en blokgegevens. Werkt zonder Linux. Kies EUR/USD en de candleperiode in de widget zelf.", color = Muted, fontSize = 14.sp)
        Button(onClick = { com.buildapptest.bitcoinpulse.widget.Omalauncher.install(context) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Toevoegen aan Omalauncher")
        }
        Text("Omalauncher toont eerst een importcontrole. Bevestig daar om de widget toe te voegen. Deze widget haalt zelf openbare gegevens op bij Kraken en mempool.space; instellingen staan los van de Android-widgets.", color = Muted, fontSize = 12.sp)
        OutlinedButton(onClick={ com.buildapptest.bitcoinpulse.widget.Omalauncher.installCompact(context) },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text("Compactwidget toevoegen aan Omalauncher") }
        Text("Mini, compact en network gebruiken dezelfde automatische indeling op basis van de beschikbare ruimte. Vergroten of verkleinen bewaart je instellingen.",color=Muted,fontSize=12.sp)
        HorizontalDivider(color=Color(0xFF38485F))
        Text("Miniwidget · 1×1",fontSize=18.sp)
        Text("Dezelfde compacte weergave met blokhoogte, fee-advies en BTC-prijs als die past.",color=Muted,fontSize=13.sp)
        var miniCurrency by rememberSaveable { mutableStateOf("EUR") }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("EUR","USD").forEach { code ->
                FilterChip(selected=miniCurrency==code,onClick={ miniCurrency=code },label={ Text(if(code=="EUR")"€ EUR" else "$ USD") })
            }
        }
        Button(onClick={ com.buildapptest.bitcoinpulse.widget.Omalauncher.installMini(context,miniCurrency) },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text("Miniwidget toevoegen aan Omalauncher") }
        Text("Kies hier de valuta en voeg elk exemplaar apart toe. Tik later op de miniwidget om alleen dat exemplaar tussen EUR en USD te wisselen. Omalauncher bewaart zijn instellingen zelf.",color=Muted,fontSize=12.sp)
    }
}

@Composable
private fun WidgetFooter(s: Snapshot, compact: Boolean, now: Long, onRefresh: () -> Unit) {
    val oldest = s.oldestTimestamp(compact)
    val label = when {
        oldest == 0L -> "Waiting for data"
        s.hasErrors -> "Cached · ${Format.time(oldest)}"
        now - oldest > 20 * 60_000L -> "Older data · ${Format.time(oldest)}"
        else -> "Oldest update ${Format.time(oldest)}"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
        IconButton(onClick = onRefresh, enabled = !s.refreshing) { Icon(Icons.Outlined.Refresh, "Refresh widget data", tint = Orange) }
    }
}

@Composable
private fun SourceLinks() {
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    var privacyOpen by remember { mutableStateOf(false) }
    val privacyText = remember { context.assets.open("privacy-policy.txt").bufferedReader().use { it.readText() } }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { uri.openUri("https://mempool.space") }) { Text("mempool.space", fontSize = 12.sp) }
            TextButton(onClick = { uri.openUri("https://www.kraken.com/prices/bitcoin") }) { Text("Kraken prices", fontSize = 12.sp) }
        }
        TextButton(onClick = { privacyOpen = true }) { Text("Privacyverklaring", fontSize = 12.sp) }
    }
    if (privacyOpen) {
        AlertDialog(onDismissRequest = { privacyOpen = false }, title = { Text("Privacyverklaring") },
            text = { Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) { Text(privacyText, fontSize = 14.sp) } },
            confirmButton = { TextButton(onClick = { privacyOpen = false }) { Text("Sluiten") } })
    }
}
