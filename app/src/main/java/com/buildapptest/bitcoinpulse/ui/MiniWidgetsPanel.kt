package com.buildapptest.bitcoinpulse.ui

import android.appwidget.AppWidgetManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.buildapptest.bitcoinpulse.data.Snapshot
import com.buildapptest.bitcoinpulse.widget.MiniWidget
import com.buildapptest.bitcoinpulse.widget.WidgetCurrency
import com.buildapptest.bitcoinpulse.widget.WidgetUpdates

@Composable
internal fun MiniWidgetsPanel(snapshot: Snapshot,now: Long) {
    val context=LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    // The screen clock also refreshes this list after returning from the launcher's pin dialog.
    val ids=remember(now,revision) { com.buildapptest.bitcoinpulse.widget.WidgetSizing.ids(context).filter { it in MiniWidget.ids(context) || com.buildapptest.bitcoinpulse.widget.WidgetSizing.mode(context,it)==com.buildapptest.bitcoinpulse.widget.WidgetMode.MINI }.sorted() }
    Surface(color=Color(0xFF182230),shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Miniwidget · 1×1",fontSize=18.sp)
            Text("Blokhoogte, fee-advies en BTC-prijs.",color=Color(0xFFBAC7DB),fontSize=13.sp)
            Row(Modifier.fillMaxWidth().background(Color(0xFF243246),RoundedCornerShape(20.dp)).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                AndroidView(factory={ ctx -> MiniWidget.views(ctx,AppWidgetManager.INVALID_APPWIDGET_ID,snapshot.copy(currency="EUR"),80,88,now).apply(ctx,FrameLayout(ctx)) },
                    update={ view -> MiniWidget.views(context,AppWidgetManager.INVALID_APPWIDGET_ID,snapshot.copy(currency="EUR"),80,88,now).reapply(context,view) },
                    modifier=Modifier.size(80.dp,88.dp))
                Column(Modifier.weight(1f).padding(top=12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Text("Compact op je startscherm",fontSize=13.sp)
                    Text("De BTC-prijs verschijnt als er genoeg ruimte is.",fontSize=12.sp,color=Color(0xFFBAC7DB))
                }
            }
            Button(onClick={ MiniWidget.pin(context) },modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text("Miniwidget toevoegen") }
            Text("Je miniwidgets",fontSize=16.sp)
            if(ids.isEmpty()) Text("Voeg een miniwidget toe om de valuta per exemplaar in te stellen.",fontSize=12.sp,color=Color(0xFFBAC7DB))
            ids.forEachIndexed { index,id ->
                val currency=WidgetCurrency.read(context,id,"EUR")
                BoxWithConstraints(Modifier.fillMaxWidth().background(Color(0xFF243246),RoundedCornerShape(16.dp)).padding(12.dp)) {
                    val choices: @Composable () -> Unit = {
                        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            listOf("EUR","USD").forEach { code ->
                                OutlinedButton(onClick={
                                    if(WidgetCurrency.set(context,id,code)) { revision++;WidgetUpdates.render(context) }
                                },modifier=Modifier.heightIn(min=48.dp).widthIn(min=68.dp).semantics { contentDescription="Widget ${index+1}: valuta $code";selected=currency==code },
                                    contentPadding=PaddingValues(horizontal=10.dp),
                                    border=BorderStroke(1.dp,if(currency==code)Color(0xFFFF8A1F)else Color(0xFF38485F)),
                                    colors=ButtonDefaults.outlinedButtonColors(containerColor=if(currency==code)Color(0xFF38485F)else Color.Transparent,contentColor=if(currency==code)Color(0xFFFF8A1F)else Color(0xFFBAC7DB))) {
                                    Text(if(code=="EUR")"€ EUR" else "$ USD",fontSize=12.sp)
                                }
                            }
                        }
                    }
                    if(maxWidth<260.dp) Column(verticalArrangement=Arrangement.spacedBy(8.dp)) { Text("Widget ${index+1}",fontSize=14.sp);choices() }
                    else Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f).padding(top=6.dp)) { Text("Widget ${index+1}",fontSize=14.sp);Text("Miniwidget · 1×1",fontSize=10.sp,color=Color(0xFFBAC7DB)) }
                        choices()
                    }
                }
            }
            Text("De valuta geldt alleen voor deze widget. Je kunt meerdere miniwidgets tegelijk toevoegen.",fontSize=12.sp,color=Color(0xFFBAC7DB))
            Text("Bij weinig ruimte verdwijnt alleen de prijsregel. Het fee-advies is het geschatte tarief voor het volgende blok; bevestigingstijden kunnen afwijken.",fontSize=12.sp,color=Color(0xFFBAC7DB))
        }
    }
}
