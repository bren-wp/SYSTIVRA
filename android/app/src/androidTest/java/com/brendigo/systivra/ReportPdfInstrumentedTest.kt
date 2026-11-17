package com.brendigo.systivra

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.buildSessionPdf
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportPdfInstrumentedTest {
    @Test fun exportsActualSamplesAsReadableMultipagePdf() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val output = File.createTempFile("systivra-test-", ".pdf", context.cacheDir)
        try {
            val samples = (1..69).map { index ->
                DeviceSnapshot.empty().copy(
                    time = 1_733_000_000_000L + index * 1_000L,
                    battery = listOf(
                        Reading("Battery temperature", "29.0 °C", "Android"),
                        Reading("Charge level", "$index%", "Android")
                    ),
                    ram = listOf(Reading("Available RAM", "3.0 GiB", "Android")),
                    thermal = listOf(Reading("Thermal status", "Light", "Android"))
                )
            }
            buildSessionPdf(samples).use { document ->
                output.outputStream().use { document.writeTo(it) }
            }
            assertTrue(output.length() > 1500)
            val signature = ByteArray(5)
            output.inputStream().use { stream ->
                assertEquals(5, stream.read(signature))
            }
            assertEquals("%PDF-", String(signature, Charsets.US_ASCII))
            ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    assertEquals(3, renderer.pageCount) // 34 + 34 + 1
                    renderer.openPage(0).use { page ->
                        assertEquals(595, page.width)
                        assertEquals(842, page.height)
                    }
                }
            }
        } finally {
            output.delete()
        }
    }
}
