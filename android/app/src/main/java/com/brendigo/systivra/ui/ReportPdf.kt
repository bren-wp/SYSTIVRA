// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.availableRamText
import com.brendigo.systivra.data.batteryTemperatureText
import com.brendigo.systivra.data.chargeLevelText
import com.brendigo.systivra.data.thermalStatusText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * A local, user-requested PDF from real saved device readings. No image, device
 * identifier, synthetic health score or network request is added to the report.
 * Caller MUST close the returned PdfDocument after writing it to SAF.
 */
internal fun buildSessionPdf(samples: List<DeviceSnapshot>): PdfDocument {
    val doc = PdfDocument()
    val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val header = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(10, 63, 99)
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textSize = 19f
    }
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 43, 63)
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textSize = 9f
    }
    val label = Paint(body).apply { textSize = 10f; typeface = Typeface.DEFAULT_BOLD }
    val rule = Paint(body).apply { color = Color.LTGRAY; strokeWidth = 1f }
    val chunks = if (samples.isEmpty()) listOf(emptyList()) else samples.chunked(34)

    fun printable(value: String?, maxChars: Int): String =
        (value?.replace('\n', ' ')?.replace('\r', ' ')?.trim()
            ?.takeIf(String::isNotEmpty) ?: "Unavailable").take(maxChars)

    try {
        chunks.forEachIndexed { index, readings ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            canvas.drawText("SYSTIVRA  /  Session report", 28f, 45f, header)
            canvas.drawText(
                "Real local recordings · " + samples.size + " samples · page " +
                    (index + 1) + " of " + chunks.size,
                28f, 66f, body
            )
            canvas.drawText(
                "UTC timestamp", 28f, 103f, label
            )
            canvas.drawText("Charge", 178f, 103f, label)
            canvas.drawText("Temp", 245f, 103f, label)
            canvas.drawText("Available RAM", 318f, 103f, label)
            canvas.drawText("Thermal", 445f, 103f, label)
            canvas.drawLine(28f, 111f, 565f, 111f, rule)
            readings.forEachIndexed { row, reading ->
                val y = 130f + row * 18f
                canvas.drawText(stamp.format(Date(reading.time)), 28f, y, body)
                canvas.drawText(printable(reading.chargeLevelText(), 12), 178f, y, body)
                canvas.drawText(printable(reading.batteryTemperatureText(), 12), 245f, y, body)
                canvas.drawText(printable(reading.availableRamText(), 17), 318f, y, body)
                canvas.drawText(printable(reading.thermalStatusText(), 16), 445f, y, body)
            }
            canvas.drawLine(28f, 801f, 565f, 801f, rule)
            canvas.drawText(
                "On-device readings only. Unavailable values are not estimated.",
                28f, 815f, body
            )
            doc.finishPage(page)
        }
        return doc
    } catch (error: Exception) {
        doc.close()
        throw error
    }
}
