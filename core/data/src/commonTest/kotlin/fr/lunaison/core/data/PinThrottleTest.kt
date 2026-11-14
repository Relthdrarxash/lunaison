package fr.lunaison.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PinThrottleTest {

    @Test
    fun delaysGrow() {
        assertEquals(0L, PinThrottle.delayFor(4))
        assertEquals(30_000L, PinThrottle.delayFor(5))
        assertEquals(60_000L, PinThrottle.delayFor(6))
        assertEquals(300_000L, PinThrottle.delayFor(7))
        assertEquals(900_000L, PinThrottle.delayFor(8))
        assertEquals(3_600_000L, PinThrottle.delayFor(50))
    }

    @Test
    fun blocksOnlyAfterFiveFailures() {
        var state = PinThrottleState()
        repeat(4) { state = PinThrottle.onFailure(state, 1_000L) }
        assertFalse(PinThrottle.isBlocked(state, 1_000L))
        state = PinThrottle.onFailure(state, 1_000L)
        assertTrue(PinThrottle.isBlocked(state, 1_000L))
        assertEquals(30_000L, PinThrottle.remainingMs(state, 1_000L))
        assertFalse(PinThrottle.isBlocked(state, 31_000L))
    }

    @Test
    fun successResets() {
        assertEquals(PinThrottleState(), PinThrottle.onSuccess())
    }

    @Test
    fun rebootReappliesFullDelay() {
        var state = PinThrottleState()
        repeat(5) { state = PinThrottle.onFailure(state, 100_000L) }
        val normalized = PinThrottle.normalize(state, 500L) // elapsedRealtime repartie de zéro
        assertEquals(30_000L, PinThrottle.remainingMs(normalized, 500L))
        assertEquals(state, PinThrottle.normalize(state, 110_000L)) // pas de redémarrage : inchangé
    }
}
