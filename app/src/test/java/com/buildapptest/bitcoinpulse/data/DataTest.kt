package com.buildapptest.bitcoinpulse.data

import org.junit.Assert.*
import org.junit.Test

class DataTest {
    @Test fun `fractional fee rates are retained`() {
        val fees = Parse.fees("""{"fastestFee":2.5,"halfHourFee":1.1,"hourFee":0.2}""")
        assertEquals(2.5, fees.next, 0.0)
        assertEquals(0.2, fees.hour, 0.0)
    }
    @Test(expected = IllegalArgumentException::class) fun `negative fees are rejected`() {
        Parse.fees("""{"fastestFee":-1,"halfHourFee":1,"hourFee":1}""")
    }
    @Test fun `empty mempool is valid and not treated as missing`() {
        assertEquals(Mempool(0, 0), Parse.mempool("""{"count":0,"vsize":0}"""))
    }
    @Test fun `latest block uses first response entry`() {
        val block = Parse.block("""[{"height":971594,"timestamp":1792078874,"tx_count":5282},{"height":971593,"timestamp":1792078488,"tx_count":4554}]""")
        assertEquals(971594L, block.height)
        assertEquals(5282L, block.transactions)
    }
    @Test fun `ticker uses last trade and rolling range for both currencies`() {
        val prices = Parse.prices(ticker)
        assertEquals(96000.0, prices.eur.price, 0.0)
        assertEquals(99000.0, prices.eur.high, 0.0)
        assertEquals(92000.0, prices.eur.low, 0.0)
        assertEquals(104000.0, prices.inCurrency("USD").price, 0.0)
    }
    @Test fun `change uses midnight UTC open instead of high or low`() {
        assertEquals(100.0, Quote(200.0, 100.0, 300.0, 50.0).todayPercent, 0.00001)
    }
    @Test(expected = IllegalArgumentException::class) fun `provider errors are rejected before reading result`() {
        Parse.prices("""{"error":["EGeneral:Temporary lockout"],"result":{}}""")
    }
    @Test(expected = IllegalArgumentException::class) fun `zero price is invalid rather than displayed as real price`() {
        Parse.prices(ticker.replace("96000", "0"))
    }
    @Test fun `missing values are represented by a dash`() {
        assertEquals("—", Format.money(null, "EUR"))
        assertEquals("—", Format.fee(null))
        assertEquals("—", Format.number(null))
    }
    @Test fun `source failure preserves original age and cached value`() {
        val source = Source(Fees(1.0, 1.0, 1.0), 1000L)
        val failed = source.copy(error = "Offline")
        assertEquals(source.value, failed.value)
        assertEquals(1000L, failed.fetchedAt)
        assertEquals("Cached · refresh failed", failed.status(2000))
    }
    @Test fun `stale threshold and clock rollback are detected`() {
        val source = Source(1, 1000L)
        assertFalse(source.isStale(1_201_000L))
        assertTrue(source.isStale(1_201_001L))
        assertTrue(source.isStale(999L))
    }
    @Test fun `widget freshness uses oldest source instead of newest`() {
        val s = Snapshot(prices = Source(Parse.prices(ticker), 4000), fees = Source(Fees(1.0, 1.0, 1.0), 1000),
            mempool = Source(Mempool(1, 1), 3000), block = Source(Block(1, 1, 1), 2000))
        assertEquals(1000L, s.oldestTimestamp())
        assertEquals(1000L, s.oldestTimestamp(compact = true))
        assertEquals(0L, s.copy(fees = Source()).oldestTimestamp())
    }
    @Test fun `future block timestamps do not create negative ages`() {
        assertEquals("<1 min", Format.blockAge(Block(1, 999999, 1), 1000))
    }
    private val ticker = """{"error":[],"result":{"XXBTZEUR":{"c":["96000","0.1"],"o":"94000","h":["97000","99000"],"l":["93000","92000"]},"XXBTZUSD":{"c":["104000","0.1"],"o":"102000","h":["105000","107000"],"l":["101000","100000"]}}}"""
}
