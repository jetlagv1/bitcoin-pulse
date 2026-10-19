package com.buildapptest.bitcoinpulse.widget

import java.text.NumberFormat
import java.util.Locale

/** Display conventions shared with the Dutch OWL widget; app formatting is unchanged. */
internal object WidgetAppearance {
    const val SINGLE_HEIGHT=320
    const val DOUBLE_HEIGHT=320
    const val DENSE_HEIGHT=600
    fun dense(height: Int,count: Int) = count==1 || height<760
    fun plotHeight(height: Int,count: Int) = if(count==1) (height-288).coerceAtLeast(40) else if(dense(height,count)) ((height-264+if(height<400)32 else 0)/2).coerceAtLeast(40) else 116
    fun number(value: Long?) = value?.let { NumberFormat.getIntegerInstance(Locale.GERMANY).format(it) } ?: "—"
    fun fee(value: Double?) = value?.let { NumberFormat.getNumberInstance(Locale.GERMANY).apply { maximumFractionDigits=2 }.format(it) } ?: "—"
    fun fixed(value: Double?,decimals: Int) = value?.let { String.format(Locale.GERMANY,"%.${decimals}f",it) } ?: "—"
    fun price(value: Double?,currency: String) = value?.let { (if(currency=="USD")"$ " else "€ ")+String.format(Locale.GERMANY,"%,.2f",it) } ?: "Koers laden…"
    fun age(timestamp: Long?,now: Long): String {
        if(timestamp==null) return "—"
        val seconds=(now/1000-timestamp).coerceAtLeast(0)
        return when { seconds>=86400 -> "${seconds/86400}d ${seconds%86400/3600}h"; seconds>=3600 -> "${seconds/3600}h ${seconds%3600/60}m"; seconds>=60 -> "${seconds/60}m ${seconds%60}s"; else -> "${seconds}s" }
    }
}
