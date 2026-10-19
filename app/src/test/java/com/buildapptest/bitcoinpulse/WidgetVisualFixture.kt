package com.buildapptest.bitcoinpulse

import android.content.Context
import com.buildapptest.bitcoinpulse.data.PERIODS
import kotlin.math.sin

/** Identical synthetic values for visual checks of both real widget renderers. */
internal object WidgetVisualFixture {
    fun data(now: Long): Map<String,String> {
        val data=mutableMapOf(
            "ticker" to """{"error":[],"result":{"XXBTZEUR":{"c":["72521.90"],"o":"72550","h":["74000","74000"],"l":["71000","71000"]},"XXBTZUSD":{"c":["78323.65"],"o":"78350","h":["80000","80000"],"l":["77000","77000"]}}}""",
            "blocks" to """[{"height":971890,"timestamp":${now/1000-567},"tx_count":3550,"extras":{"medianFee":2.6,"pool":{"name":"Foundry USA"}}}]""",
            "fees" to """{"fastestFee":3,"halfHourFee":2,"hourFee":1}""",
            "pool" to """{"count":18420,"vsize":12800000}"""
        )
        for(code in listOf("EUR","USD")) for(period in PERIODS) {
            val rows=(0..239).joinToString(",") { i ->
                val base=(71500+i*4+750*sin(i*.09))*(if(code=="USD")1.08 else 1.0)
                val close=base+100*sin(i*1.7)
                "[${1792096320L-(239-i)*period.minutes*60L},$base,${maxOf(base,close)+90},${minOf(base,close)-80},$close,$close,1,1]"
            }
            val name=code.lowercase()+when(period.id){"4h"->"4h";"1d"->"Day";else->"Week"}
            data[name]="""{"error":[],"result":{"XXBTZ$code":[$rows]}}"""
        }
        return data
    }
    fun seed(context: Context,now: Long) {
        val data=data(now);val edit=context.getSharedPreferences("pulse-data-v1",0).edit().clear().putBoolean("ichimoku",true)
        for((key,name) in listOf("prices" to "ticker","block" to "blocks","fees" to "fees","mempool" to "pool")) edit.putString(key,data.getValue(name)).putLong("$key-time",now)
        for(code in listOf("EUR","USD")) for(period in PERIODS) {
            val name=code.lowercase()+when(period.id){"4h"->"4h";"1d"->"Day";else->"Week"}
            val key="candles-$code-${period.id}";edit.putString(key,data.getValue(name)).putLong("$key-time",now)
        }
        edit.commit()
    }
}
