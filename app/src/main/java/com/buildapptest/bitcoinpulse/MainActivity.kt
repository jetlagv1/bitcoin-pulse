package com.buildapptest.bitcoinpulse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buildapptest.bitcoinpulse.ui.PulseScreen
import com.buildapptest.bitcoinpulse.widget.CompactWidget
import com.buildapptest.bitcoinpulse.widget.PulseWidget
import com.buildapptest.bitcoinpulse.widget.WidgetUpdates
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val repository = (application as PulseApplication).repository
        setContent {
            PulseScreen(repository,
                onRefresh = { lifecycleScope.launch { repository.refresh(); WidgetUpdates.render(this@MainActivity) } },
                onCurrency = { code -> lifecycleScope.launch { repository.setCurrency(code); WidgetUpdates.render(this@MainActivity); repository.refresh(); WidgetUpdates.render(this@MainActivity) } },
                onPin = ::pinWidget)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    repository.refresh()
                    WidgetUpdates.render(this@MainActivity)
                    delay(30_000)
                }
            }
        }
    }
    private fun pinWidget(compact: Boolean) {
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(this, if (compact) CompactWidget::class.java else PulseWidget::class.java)
            val accepted = manager.requestPinAppWidget(provider, null, null)
            if (!accepted) showWidgetInstructions()
        } else showWidgetInstructions()
    }
    private fun showWidgetInstructions() {
        Toast.makeText(this, "Houd je startscherm ingedrukt, kies Widgets en vervolgens Bitcoin Pulse.", Toast.LENGTH_LONG).show()
    }
}
