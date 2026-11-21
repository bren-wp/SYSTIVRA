package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticResultFilterTest {
    private val actual = listOf(
        "Touch" to "Pass (user-confirmed)",
        "Vibration" to "Fail (user-confirmed)",
        "Speaker" to "Not assessed",
        "Camera" to "Unavailable — access not requested",
        "Other" to "Skipped by user"
    )

    @Test fun partitionsActualResultsWithoutCountingUnknownsAsSuccess() {
        assertEquals(listOf("Touch"), filteredDiagnosticResults(actual, "Passed").map { it.first })
        assertEquals(listOf("Vibration"), filteredDiagnosticResults(actual, "Failed").map { it.first })
        assertEquals(listOf("Other"), filteredDiagnosticResults(actual, "Skipped").map { it.first })
        assertEquals(listOf("Speaker", "Camera"),
            filteredDiagnosticResults(actual, "Unavailable").map { it.first })
    }

    @Test fun unrecognizedFilterDoesNotReturnMisleadingData() {
        assertTrue(filteredDiagnosticResults(actual, "Excellent").isEmpty())
        assertEquals(actual, filteredDiagnosticResults(actual, null))
    }
}
