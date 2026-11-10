package fr.lunaison.core.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/*
 * Petite couche de compatibilité : le socle utilisait `java.time.LocalDate`, absent du Kotlin commun.
 * Elle reprend les noms et les comportements de `java.time` utilisés par le moteur, pour que le code
 * du cycle reste lisible et identique d'une plateforme à l'autre (Android aujourd'hui, iOS ensuite).
 */

fun LocalDate.plusDays(days: Long): LocalDate = plus(days, DateTimeUnit.DAY)

fun LocalDate.plusDays(days: Int): LocalDate = plusDays(days.toLong())

fun LocalDate.minusDays(days: Long): LocalDate = minus(days, DateTimeUnit.DAY)

fun LocalDate.minusDays(days: Int): LocalDate = minusDays(days.toLong())

fun LocalDate.plusMonths(months: Long): LocalDate = plus(months, DateTimeUnit.MONTH)

fun LocalDate.minusMonths(months: Long): LocalDate = minus(months, DateTimeUnit.MONTH)

fun LocalDate.plusYears(years: Long): LocalDate = plus(years, DateTimeUnit.YEAR)

fun LocalDate.minusYears(years: Long): LocalDate = minus(years, DateTimeUnit.YEAR)

fun LocalDate.isBefore(other: LocalDate): Boolean = this < other

fun LocalDate.isAfter(other: LocalDate): Boolean = this > other

/** Nombre de jours depuis le 1er janvier 1970 (comme `java.time.LocalDate.toEpochDay`). */
fun LocalDate.toEpochDay(): Long = toEpochDays().toLong()

/** Nombre de jours entiers de [this] à [other] (négatif si [other] est avant). */
fun LocalDate.daysTo(other: LocalDate): Int = daysUntil(other)

fun LocalDate.Companion.of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

fun LocalDate.Companion.ofEpochDay(epochDay: Long): LocalDate = fromEpochDays(epochDay.toInt())

/** Date du jour dans le fuseau du téléphone. */
fun LocalDate.Companion.now(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

// Heures et dates-heures (rappels).

fun LocalTime.Companion.of(hour: Int, minute: Int): LocalTime = LocalTime(hour, minute)

fun LocalDateTime.Companion.of(date: LocalDate, time: LocalTime): LocalDateTime =
    LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, time.hour, time.minute, time.second, time.nanosecond)

fun LocalDateTime.toLocalDate(): LocalDate = date

fun LocalDateTime.plusDays(days: Long): LocalDateTime = LocalDateTime.of(date.plusDays(days), time)

fun LocalDateTime.plusDays(days: Int): LocalDateTime = plusDays(days.toLong())

fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other

fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other

/** Date-heure actuelle dans le fuseau du téléphone. */
fun LocalDateTime.Companion.now(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
