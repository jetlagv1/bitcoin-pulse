package com.buildapptest.bitcoinpulse

import com.buildapptest.bitcoinpulse.widget.Omalauncher
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64
import java.util.zip.Inflater

class OmalauncherTest {
    @Test fun importStartsWithSelectedCountAndCurrency() {
        val source="state graphCount = 2 persist\nstate currency = \"EUR\" persist"
        assertEquals("state graphCount = 1 persist\nstate currency = \"USD\" persist",Omalauncher.configuredSource(source,1,"USD"))
        assertEquals(source,Omalauncher.configuredSource(source,2))
    }
    @Test fun importLinkUsesCompatibleRawDeflateAndUtf8() {
        val source = "widget \"Bitcoin €\" { Text { text: \"₿\" } }"
        val link = Omalauncher.widgetLink(source)
        assertTrue(link.startsWith("https://simonschubert.github.io/omarchy-widgets/w#owl1."))
        assertFalse(link.contains("="))
        val compressed = Base64.getUrlDecoder().decode(link.substringAfter("#owl1."))
        val inflater = Inflater(true)
        try {
            inflater.setInput(compressed + 0)
            val output = ByteArray(4096)
            val count = inflater.inflate(output)
            assertTrue(inflater.finished())
            assertEquals(source, output.copyOf(count).toString(Charsets.UTF_8))
        } finally { inflater.end() }
    }
}
