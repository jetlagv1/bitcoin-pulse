package com.buildapptest.bitcoinpulse.data

import org.junit.Assert.*
import org.junit.Test

class CandleTest {
    private fun response(rows: String, currency: String="EUR") = """{"error":[],"result":{"XXBTZ$currency":[$rows],"last":123}}"""
    private val row="""[100,"100","120","90","110","105","2.5",3]"""
    @Test fun `preserves OHLC and volume and marks only final candle provisional`() {
        val data=CandleParser.parse(response("$row,${row.replace("[100,","[200,")}"),"EUR")
        assertEquals(2,data.size); assertFalse(data.first().provisional); assertTrue(data.last().provisional)
        assertEquals(120.0,data.first().high,0.0); assertEquals(2.5,data.first().volume,0.0)
    }
    @Test fun `retains calculation history and complete final candle flag`() {
        val rows=(1..600).joinToString(",") { row.replace("[100,","[$it,") }
        val data=CandleParser.parse(response(rows),"EUR")
        assertEquals(480,data.size); assertEquals(121L,data.first().timestamp); assertTrue(data.last().provisional)
    }
    @Test(expected=IllegalArgumentException::class) fun `rejects impossible high`() {
        CandleParser.parse(response(row.replace("\"120\"","\"99\"")),"EUR")
    }
    @Test(expected=IllegalArgumentException::class) fun `rejects duplicate timestamps`() {
        CandleParser.parse(response("$row,$row"),"EUR")
    }
    @Test(expected=IllegalArgumentException::class) fun `rejects kraken errors`() {
        CandleParser.parse("""{"error":["Rate limit"],"result":{}}""","EUR")
    }
    @Test fun `currency caches remain isolated`() {
        val eur=Source(CandleParser.parse(response(row),"EUR"),1000L)
        val usd=Source(CandleParser.parse(response(row.replace("\"110\"","\"115\""),"USD"),"USD"),2000L)
        val snapshot=Snapshot(candles=mapOf(candleKey("EUR","4h") to eur,candleKey("USD","4h") to usd))
        assertEquals(110.0,snapshot.history("4h").value!!.last().close,0.0)
        assertEquals(115.0,snapshot.copy(currency="USD").history("4h").value!!.last().close,0.0)
        assertNull(snapshot.history("1d").value)
    }
    @Test fun `combination uses requested intervals in stable order`() {
        assertEquals(listOf("4h","1w"),Snapshot(chartMode="combi",combo=setOf("1w","4h")).chartPeriods().map { it.id })
        assertEquals(listOf("1d"),Snapshot(chartMode="1d").chartPeriods().map { it.id })
    }
    @Test fun `extended block data retains fractional fees and uses weight fullness`() {
        val b=Parse.block("""[{"height":1,"timestamp":100,"tx_count":20,"weight":3997855,"extras":{"medianFee":3.02,"feeRange":[1.1,2,90.3],"totalFees":4141107,"reward":316641107,"pool":{"name":"F2Pool"}}}]""")
        assertEquals("F2Pool",b.pool); assertEquals(3.02,b.medianFee!!,0.0)
        assertEquals(99.946375,b.fullness!!,0.000001); assertEquals(4141107L,b.totalFees)
        assertEquals(90.3,b.feeMax!!,0.0)
    }
    @Test fun `missing pool or extras are unknown rather than fabricated`() {
        val b=Parse.block("""[{"height":1,"timestamp":100,"tx_count":20}]""")
        assertNull(b.pool); assertNull(b.medianFee); assertNull(b.fullness)
    }
    @Test fun `difficulty allows a negative estimated change`() {
        val d=Parse.difficulty("""{"difficultyChange":-2.4,"progressPercent":25.0,"remainingBlocks":1512}""")
        assertEquals(-2.4,d.change,0.0); assertEquals(1512L,d.remainingBlocks)
    }
}
