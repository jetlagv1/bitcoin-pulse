package com.buildapptest.bitcoinpulse.data

import org.junit.Assert.*
import org.junit.Test

class IchimokuTest {
    private fun candles(n: Int) = (0 until n).map { i -> Candle(i.toLong(),1000.0+i,1010.0+i,990.0+i,1001.0+i,1.0,i==n-1) }
    @Test fun windowsUseHighLowMidpointsAndInclusiveDisplacement() {
        val p=Ichimoku.calculate(candles(240))
        assertEquals(121,p.size)
        assertEquals(148,p.first().index) // source 119 + (30 - 1)
        assertEquals(1099.5,p.first().a,0.000001)
        assertEquals(1059.5,p.first().b,0.000001)
        assertEquals(268,p.last().index)
        assertEquals(1219.5,p.last().a,0.000001)
        assertEquals(1179.5,p.last().b,0.000001)
    }
    @Test fun warmupDoesNotFabricateValues() {
        assertTrue(Ichimoku.calculate(candles(119)).isEmpty())
        assertEquals(1,Ichimoku.calculate(candles(120)).size)
        val points=Ichimoku.calculate(candles(Ichimoku.REQUIRED))
        assertEquals(Ichimoku.REQUIRED-Ichimoku.VISIBLE,points.first().index)
    }
    @Test fun futureObservationsNeverChangeEarlierSpans() {
        val original=candles(240)
        val changed=original.dropLast(1)+original.last().copy(high=9999.0)
        val a=Ichimoku.calculate(original);val b=Ichimoku.calculate(changed)
        assertEquals(a.dropLast(1),b.dropLast(1))
        assertNotEquals(a.last(),b.last())
    }
    @Test fun closePriceAloneIsNotUsedAsAMovingAverage() {
        val original=candles(240)
        assertEquals(Ichimoku.calculate(original),Ichimoku.calculate(original.map { it.copy(close=it.low+1) }))
    }
}
