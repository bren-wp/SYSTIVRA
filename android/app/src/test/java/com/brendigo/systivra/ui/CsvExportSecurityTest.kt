// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportSecurityTest {
    @Test fun formulasWithHiddenPrefixesArePlainTextAndCannotSplitRows() {
        val sample = DeviceSnapshot.empty().copy(
            time = 0L,
            battery = listOf(
                Reading("Charge level", "=1+1", "Android"),
                Reading("Battery temperature", "  \u200B+SUM(1,1)", "Android")
            ),
            thermal = listOf(
                Reading("Thermal status", "safe\r\n=HYPERLINK(\"x\")", "Android")
            )
        )
        val csv = buildSessionCsv(listOf(sample))
        assertEquals(2, csv.trimEnd().split('\n').size)
        assertTrue(csv.contains("\"'=1+1\""))
        assertTrue(csv.contains("\"'+SUM(1,1)\""))
        assertTrue(csv.contains("\"safe  =HYPERLINK(\"\"x\"\")\""))
    }

    @Test fun ordinaryValuesRemainUnchangedAndCsvQuotesEscaped() {
        val sample = DeviceSnapshot.empty().copy(
            time = 0L,
            thermal = listOf(Reading("Thermal status", "Warm, \"safe\"", "Android"))
        )
        val csv = buildSessionCsv(listOf(sample))
        assertTrue(csv.contains("\"Warm, \"\"safe\"\"\""))
        assertFalse(csv.contains("'Warm"))
    }
}
