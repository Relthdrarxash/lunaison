package fr.lunaison.ui

import fr.lunaison.core.model.ofEpochDay
import fr.lunaison.core.model.toEpochDay
import kotlinx.datetime.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

/** Minuit UTC de ce jour en millisecondes : la valeur qu'attend le sélecteur de date de Material. */
internal fun LocalDate.toUtcMillis(): Long = toEpochDay() * MILLIS_PER_DAY

/** Le jour (UTC) qui contient cet instant, pour lire le sélecteur de date de Material. */
internal fun localDateFromUtcMillis(millis: Long): LocalDate = LocalDate.ofEpochDay(millis.floorDiv(MILLIS_PER_DAY))
