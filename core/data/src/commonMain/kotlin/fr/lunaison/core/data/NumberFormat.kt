package fr.lunaison.core.data

import kotlin.math.abs
import kotlin.math.roundToLong

/** Une décimale avec un point, quelle que soit la langue (comme `"%.1f"` avec `Locale.ROOT`), pour les exports CSV. */
internal fun Float.toOneDecimal(): String {
    if (isNaN() || isInfinite()) return toString()
    val tenths = (toDouble() * 10).roundToLong()
    val magnitude = abs(tenths)
    return (if (tenths < 0) "-" else "") + "${magnitude / 10}.${magnitude % 10}"
}
