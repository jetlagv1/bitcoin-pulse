package com.buildapptest.bitcoinpulse

import android.app.Application
import com.buildapptest.bitcoinpulse.data.PulseRepository
import com.buildapptest.bitcoinpulse.widget.WidgetUpdates

class PulseApplication : Application() {
    val repository by lazy { PulseRepository(this) }
    override fun onCreate() {
        super.onCreate()
        if (WidgetUpdates.hasWidgets(this)) WidgetUpdates.schedule(this)
    }
}
