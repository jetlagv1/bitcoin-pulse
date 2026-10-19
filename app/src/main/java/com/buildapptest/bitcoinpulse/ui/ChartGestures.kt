package com.buildapptest.bitcoinpulse.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import com.buildapptest.bitcoinpulse.data.ChartZoom
import kotlin.math.roundToInt

/** Consume only two-finger gestures; one-finger scrolling stays with the parent. */
internal suspend fun PointerInputScope.chartGestures(currentCount: () -> Int, onZoom: (Int) -> Unit, onZoomEnd: () -> Unit, onTap: (Offset) -> Unit) {
    awaitEachGesture {
        val down=awaitFirstDown(requireUnconsumed=false)
        var pinched=false
        var moved=false
        var count=currentCount().toFloat()
        do {
            val event=awaitPointerEvent()
            if(event.changes.count { it.pressed }>=2) {
                pinched=true
                count=ChartZoom.pinch(count,event.calculateZoom())
                onZoom(count.roundToInt())
                event.changes.forEach { it.consume() }
            } else if(!pinched) {
                if(event.changes.any { it.isConsumed || (it.position-down.position).getDistance()>viewConfiguration.touchSlop }) moved=true
                if(event.changes.none { it.pressed } && !moved) onTap(event.changes.first().position)
            } else event.changes.forEach { it.consume() }
        } while(event.changes.any { it.pressed })
        if(pinched) onZoomEnd()
    }
}
