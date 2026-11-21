// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PagedContentTest {
    @Test fun onePageExistsForEmptyResults() {
        assertEquals(1, pageCount(0, 2))
        assertEquals(emptyList<Int>(), pageItems(emptyList<Int>(), 7, 2))
    }

    @Test fun allResultsAreAccessibleWithoutScroll() {
        val readings = (1..101).toList()
        assertEquals(51, pageCount(readings.size, 2))
        val combined = (0 until pageCount(readings.size, 2)).flatMap {
            pageItems(readings, it, 2)
        }
        assertEquals(readings, combined)
    }

    @Test fun pageAfterDatasetShrinkClampsToLastValidPage() {
        assertEquals(listOf(3), pageItems(listOf(1, 2, 3), 17, 2))
        assertEquals(listOf(1, 2), pageItems(listOf(1, 2, 3), -100, 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun pageSizeMustBePositive() { pageCount(3, 0) }
}
