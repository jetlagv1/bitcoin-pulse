package com.buildapptest.bitcoinpulse.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.buildapptest.bitcoinpulse.PulseApplication
import com.buildapptest.bitcoinpulse.data.PERIODS

/** Native interval chooser, opened by tapping a widget's period title. */
class WidgetChartPickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1)
        val slot=intent.getIntExtra("slot",-1)
        if(!WidgetCharts.owned(this,id)) { finish();return }
        val settings=WidgetCharts.read(this,id,(application as PulseApplication).repository.state.value)
        if(slot !in 0 until settings.count) { finish();return }
        val scale=resources.displayMetrics.density
        fun dp(n: Int)=(n*scale).toInt()
        title="Grafiek ${slot+1} · kies interval"
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(8),dp(16),dp(12));setBackgroundColor(Color.parseColor("#182230")) }
        for(period in PERIODS) {
            root.addView(RadioButton(this).apply {
                text=period.label;isChecked=settings.periods[slot].id==period.id
                isEnabled=settings.choose(slot,period.id)!=null
                setTextColor(Color.parseColor(if(isEnabled)"#F7FAFF" else "#718096"))
                minHeight=dp(48)
                setOnClickListener { if(WidgetCharts.select(this@WidgetChartPickerActivity,id,slot,period.id)) WidgetUpdates.render(this@WidgetChartPickerActivity);finish() }
            },LinearLayout.LayoutParams(-1,dp(48)))
        }
        root.addView(TextView(this).apply { text="Annuleren";setTextColor(Color.parseColor("#FF8A1F"));gravity=android.view.Gravity.CENTER_VERTICAL;minHeight=dp(48);setOnClickListener { finish() } })
        setContentView(root)
        setFinishOnTouchOutside(true)
    }
}
