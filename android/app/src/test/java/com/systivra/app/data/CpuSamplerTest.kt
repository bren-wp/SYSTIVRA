package com.systivra.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CpuSamplerTest {
    @Test fun parsesAggregateLinuxCpuTicks() {
        val row = "cpu  100 20 50 700 20 5 5 0 0 0"
        assertEquals(CpuTicks(900, 720), parseCpuTicks(row))
    }

    @Test fun ignoresInvalidOrPerCoreRows() {
        assertNull(parseCpuTicks("cpu0 1 2 3 4 5 6 7"))
        assertNull(parseCpuTicks("cpu 1 2 3"))
        assertNull(parseCpuTicks("cpu A B C D E F G"))
        assertNull(parseCpuTicks(null))
    }

    @Test fun computesDeltaFromTwoActualSamples() {
        val old = CpuTicks(1000, 500)
        val now = CpuTicks(1200, 560)
        assertEquals(70.0, cpuLoadPercent(old, now)!!, 0.0001)
    }

    @Test fun refusesUnsupportedOrInvalidCounterDeltas() {
        val old = CpuTicks(1000, 500)
        assertNull(cpuLoadPercent(null, CpuTicks(1200, 560)))
        assertNull(cpuLoadPercent(old, CpuTicks(1000, 500)))
        assertNull(cpuLoadPercent(old, CpuTicks(900, 400)))
        assertNull(cpuLoadPercent(old, CpuTicks(1200, 750)))
    }
}
