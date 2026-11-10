package fr.lunaison.ui

import fr.lunaison.core.model.ofEpochDay
import fr.lunaison.core.model.toEpochDay
import kotlinx.datetime.LocalDate

/*
 * Le socle partagé manipule des dates `kotlinx-datetime`. L'interface Android, elle, garde `java.time`
 * pour la mise en forme selon la langue (DateTimeFormatter) : on convertit à cette frontière seulement.
 */

internal fun LocalDate.toJava(): java.time.LocalDate = java.time.LocalDate.of(year, monthNumber, dayOfMonth)

internal fun java.time.LocalDate.toKotlin(): LocalDate = LocalDate(year, monthValue, dayOfMonth)

private const val MILLIS_PER_DAY = 86_400_000L

/** Minuit UTC de ce jour en millisecondes : la valeur qu'attend le sélecteur de date de Material. */
internal fun LocalDate.toUtcMillis(): Long = toEpochDay() * MILLIS_PER_DAY

/** Le jour (UTC) qui contient cet instant, pour lire le sélecteur de date de Material. */
internal fun localDateFromUtcMillis(millis: Long): LocalDate = LocalDate.ofEpochDay(Math.floorDiv(millis, MILLIS_PER_DAY))
