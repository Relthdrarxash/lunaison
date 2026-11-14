package fr.lunaison.core.data

/** État persisté de la temporisation : échecs consécutifs et fin du blocage en temps `elapsedRealtime`. */
data class PinThrottleState(
    val failures: Int = 0,
    val blockedUntil: Long = 0L,
    /** Dernier instant `elapsedRealtime` vu à l'enregistrement : s'il est dans le futur, le téléphone a redémarré. */
    val savedAt: Long = 0L,
    /** Durée du dernier blocage, pour le réappliquer si l'horloge repart de zéro après un redémarrage. */
    val lastDelayMs: Long = 0L,
)

/**
 * Délai croissant après 5 échecs consécutifs au déverrouillage. Logique pure : l'heure est fournie par l'appelant
 * (`SystemClock.elapsedRealtime()`), ce qui la rend testable.
 */
object PinThrottle {
    const val FREE_ATTEMPTS = 5
    private val DELAYS_MS = longArrayOf(30_000L, 60_000L, 300_000L, 900_000L, 3_600_000L)

    /** Délai imposé après le n-ième échec consécutif (0 tant que les essais gratuits ne sont pas épuisés). */
    fun delayFor(failures: Int): Long =
        if (failures < FREE_ATTEMPTS) 0L else DELAYS_MS[(failures - FREE_ATTEMPTS).coerceAtMost(DELAYS_MS.lastIndex)]

    /**
     * Recale un état relu depuis le disque. Si `elapsedRealtime` est repassé sous la valeur enregistrée, le
     * téléphone a redémarré : on réapplique la durée complète du dernier blocage, sauf s'il était déjà expiré.
     * Limite connue : un redémarrage n'est détecté que si l'horloge repart sous `savedAt` ; après un long
     * fonctionnement ultérieur, il passerait inaperçu et le blocage serait seulement écourté.
     */
    fun normalize(state: PinThrottleState, now: Long): PinThrottleState =
        if (now < state.savedAt && state.blockedUntil > state.savedAt) {
            state.copy(blockedUntil = now + state.lastDelayMs, savedAt = now)
        } else {
            state
        }

    fun remainingMs(state: PinThrottleState, now: Long): Long = (state.blockedUntil - now).coerceAtLeast(0L)

    fun isBlocked(state: PinThrottleState, now: Long): Boolean = remainingMs(state, now) > 0L

    fun onFailure(state: PinThrottleState, now: Long): PinThrottleState {
        val failures = state.failures + 1
        val delay = delayFor(failures)
        return PinThrottleState(
            failures = failures,
            blockedUntil = if (delay > 0L) now + delay else 0L,
            savedAt = now,
            lastDelayMs = delay,
        )
    }

    fun onSuccess(): PinThrottleState = PinThrottleState()
}
