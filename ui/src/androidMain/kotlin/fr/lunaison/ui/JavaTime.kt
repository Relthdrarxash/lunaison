package fr.lunaison.ui

import kotlinx.datetime.LocalDate

/*
 * Le socle partagé manipule des dates `kotlinx-datetime`. Le bilan PDF (Android) garde `java.time`
 * pour la mise en forme selon la langue : on convertit à cette frontière seulement.
 */

internal fun LocalDate.toJava(): java.time.LocalDate = java.time.LocalDate.of(year, monthNumber, dayOfMonth)

internal fun java.time.LocalDate.toKotlin(): LocalDate = LocalDate(year, monthValue, dayOfMonth)
