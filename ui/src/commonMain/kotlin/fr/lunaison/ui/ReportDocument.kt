package fr.lunaison.ui

import fr.lunaison.core.content.Sources
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import kotlinx.datetime.LocalDate

// ---------------------------------------------------------------------------------------------
// Contenu et palette du bilan : partagés entre Android et iOS, testables sur la JVM.
// ---------------------------------------------------------------------------------------------

/** Un chiffre clé de l'encadré « Situation actuelle ». */
data class ReportTile(val value: String, val label: String, val detail: String? = null)

/** Une barre de la liste des douleurs : [days] jours sur la période de [ReportDocument.PERIOD_DAYS]. */
data class ReportBar(val label: String, val days: Int, val valueText: String)

/** Une alerte du bilan, titre et explication. */
data class ReportAlert(val title: String, val message: String)

/** Un bloc de contenu à l'intérieur d'une rubrique. */
sealed interface ReportBlock {
    /** Lignes libellé / valeur. */
    data class Rows(val rows: List<Pair<String, String>>) : ReportBlock

    /** Liste à puces. */
    data class Bullets(val items: List<String>) : ReportBlock

    /** Paragraphe de texte ; [muted] pour une remarque secondaire. */
    data class Paragraph(val text: String, val muted: Boolean = false) : ReportBlock

    /** Tableau des cycles, du plus récent au plus ancien, avec une ligne de résumé facultative. */
    data class CycleTable(val headers: List<String>, val rows: List<List<String>>, val summary: String?) : ReportBlock

    /** Libellés avec un nombre de jours aligné à droite et une barre proportionnelle. */
    data class Bars(val bars: List<ReportBar>) : ReportBlock

    /** Encadré d'alerte. */
    data class Alerts(val items: List<ReportAlert>) : ReportBlock
}

/** Une rubrique cochée et non vide. */
data class ReportPart(val section: ReportSection, val title: String, val blocks: List<ReportBlock>)

/** Badge de catégorie affiché dans le périmètre du bilan. */
data class ReportCategoryBadge(val section: ReportSection, val label: String, val hasData: Boolean)

/** Encadré affiché lorsque le suivi vient de commencer (aucun cycle complet ou peu de données). */
data class ReportEarlyTracking(val title: String, val message: String)

/** Couleurs ARGB d'une rubrique du bilan (accent et fond doux). */
internal data class ReportSectionPalette(val accent: Long, val soft: Long)

internal fun reportSectionPalette(section: ReportSection?): ReportSectionPalette = when (section) {
    ReportSection.ALERTS -> ReportSectionPalette(0xFFA8473FL, 0xFFF6DCD6L)
    ReportSection.OVERVIEW -> ReportSectionPalette(0xFF4A2C4FL, 0xFFF1E2EAL)
    ReportSection.CYCLES_HISTORY -> ReportSectionPalette(0xFF5B4B8AL, 0xFFEFEBF7L)
    ReportSection.PAIN_AND_ENDOMETRIOSIS -> ReportSectionPalette(0xFFB84A49L, 0xFFFCE8E6L)
    ReportSection.SYMPTOMS_AND_MOODS -> ReportSectionPalette(0xFF8E4162L, 0xFFF9E8EFL)
    ReportSection.MEDICATIONS -> ReportSectionPalette(0xFF2F6F62L, 0xFFE3F1EDL)
    ReportSection.WEIGHT -> ReportSectionPalette(0xFF3E6482L, 0xFFE6EFF5L)
    ReportSection.OVULATION_SIGNS -> ReportSectionPalette(0xFFA66A2CL, 0xFFFBF0E2L)
    ReportSection.INTIMACY -> ReportSectionPalette(0xFF7A5178L, 0xFFF4EAF3L)
    ReportSection.NOTES -> ReportSectionPalette(0xFF6E5A4FL, 0xFFF4EFEAL)
    null -> ReportSectionPalette(0xFF4A2C4FL, 0xFFF1E2EAL)
}

/** Le bilan prêt à dessiner. */
data class ReportDocument(
    val title: String,
    val subtitle: String,
    val generatedLabel: String,
    val confidentialBadge: String,
    val situationTitle: String,
    val tiles: List<ReportTile>,
    val categoriesTitle: String = "",
    val categoryBadges: List<ReportCategoryBadge> = emptyList(),
    val earlyTracking: ReportEarlyTracking? = null,
    val consultationNotesTitle: String = "",
    /** Rubriques dans l'ordre d'affichage : points d'attention d'abord, puis l'ordre de [ReportSection]. */
    val parts: List<ReportPart>,
    val referencesTitle: String,
    val references: List<String>,
) {
    companion object {
        /** Période couverte par les rubriques chiffrées. */
        const val PERIOD_DAYS = 90
    }
}

internal expect fun formatReportFullDate(date: LocalDate): String
internal expect fun formatReportShortDate(date: LocalDate): String

private const val MAX_NOTE_LENGTH = 600

/**
 * Construit le contenu du bilan : mêmes comptes et mêmes conditions d'affichage que l'écran,
 * uniquement pour les rubriques cochées dans [sections].
 */
fun buildReportDocument(
    report: HealthReport,
    medicationLabel: String,
    logsLast90Days: List<DayLog>,
    sections: Set<ReportSection>,
): ReportDocument {
    val overview = report.overview
    val period = ReportText.last90Days()

    val tiles = buildList {
        add(
            ReportTile(
                report.dayOfCycle.toString(),
                tr(
                    fr = "Jour du cycle",
                    en = "Cycle day",
                    pl = "Dzień cyklu",
                    uk = "День циклу",
                    es = "Día del ciclo",
                    de = "Zyklustag",
                ),
            ),
        )
        add(ReportTile(Format.dayMonth(report.lastPeriodStart), ReportText.lastPeriodStart()))
        if (ReportSection.OVERVIEW in sections && overview != null) {
            add(
                ReportTile(
                    Format.days(overview.usualLength),
                    usualCycleLength(),
                    overview.usualPeriodLength?.let {
                        tr(
                            fr = "règles : ${Format.days(it)}",
                            en = "period: ${Format.days(it)}",
                            pl = "miesiączka: ${Format.days(it)}",
                            uk = "менструація: ${Format.days(it)}",
                            es = "menstruación: ${Format.days(it)}",
                            de = "Blutungsdauer: ${Format.days(it)}",
                        )
                    },
                ),
            )
        }
        if (report.spottingCountLast90Days > 0) {
            add(
                ReportTile(
                    Format.days(report.spottingCountLast90Days),
                    "${ReportText.spotting()} (${ReportText.ninetyDays()})",
                ),
            )
        }
    }

    val parts = ReportSection.entries.filter { it in sections }.mapNotNull { section ->
        when (section) {
            ReportSection.OVERVIEW -> overviewPart(overview)
            ReportSection.CYCLES_HISTORY -> cyclesHistoryPart(overview)
            ReportSection.PAIN_AND_ENDOMETRIOSIS -> painPart(logsLast90Days, period)
            ReportSection.SYMPTOMS_AND_MOODS -> symptomsPart(report)
            ReportSection.MEDICATIONS -> medicationsPart(medicationLabel, logsLast90Days, period)
            ReportSection.WEIGHT -> weightPart(logsLast90Days, period)
            ReportSection.OVULATION_SIGNS -> ovulationPart(logsLast90Days, period)
            ReportSection.INTIMACY -> intimacyPart(logsLast90Days, period)
            ReportSection.NOTES -> notesPart(logsLast90Days, period)
            ReportSection.ALERTS -> alertsPart(report)
        }
    }.sortedBy { if (it.section == ReportSection.ALERTS) 0 else 1 }

    val shown = parts.map { it.section }.toSet()
    val categoryBadges = ReportSection.entries
        .filter { it in sections }
        .sortedBy { if (it == ReportSection.ALERTS) 0 else 1 }
        .map { section -> ReportCategoryBadge(section, section.label, section in shown) }

    val loggedDays = logsLast90Days.size
    val earlyTracking = if (overview == null || parts.isEmpty()) {
        ReportEarlyTracking(
            title = tr(
                fr = "Début de suivi",
                en = "Early tracking",
                pl = "Początek obserwacji",
                uk = "Початок спостереження",
                es = "Inicio del seguimiento",
                de = "Beginn der Aufzeichnung",
            ),
            message = tr(
                fr = "Suivi récent : $loggedDays jour(s) noté(s) sur les ${ReportDocument.PERIOD_DAYS} derniers jours, aucun cycle complet encore clôturé. Les courbes, moyennes et tendances par phase apparaîtront au fil des prochains cycles.",
                en = "Recent tracking: $loggedDays day(s) logged over the last ${ReportDocument.PERIOD_DAYS} days, no completed cycle yet. Averages, tables and phase trends will populate over the coming cycles.",
                pl = "Nowa obserwacja: zapisano dni ($loggedDays) w ciągu ostatnich ${ReportDocument.PERIOD_DAYS} dni, brak jeszcze pełnego zakończonego cyklu. Średnie i trendy faz pojawią się w kolejnych cyklach.",
                uk = "Нещодавній початок: за останні ${ReportDocument.PERIOD_DAYS} днів занотовано днів ($loggedDays), завершених циклів ще немає. Середні показники та тенденції з'являться в наступних циклах.",
                es = "Seguimiento reciente: $loggedDays día(s) registrado(s) en los últimos ${ReportDocument.PERIOD_DAYS} días, sin ciclo completo cerrado aún. Las medias y tendencias aparecerán en los próximos ciclos.",
                de = "Kurze Beobachtungsdauer: $loggedDays Tag(e) in den letzten ${ReportDocument.PERIOD_DAYS} Tagen erfasst, noch kein vollständiger Zyklus abgeschlossen. Durchschnittswerte und Phasentrends folgen in den nächsten Zyklen.",
            ),
        )
    } else {
        null
    }

    val references = buildList {
        if (ReportSection.OVERVIEW in shown || ReportSection.CYCLES_HISTORY in shown) {
            add(Sources.FIGO_2018.label)
            add(Sources.ACOG_ADOLESCENTS_2015.label)
        }
        if (ReportSection.PAIN_AND_ENDOMETRIOSIS in shown) add(Sources.ESHRE_ENDOMETRIOSIS_2022.label)
    }

    return ReportDocument(
        title = tr(
            fr = "Bilan de cycle",
            en = "Cycle report",
            pl = "Raport o cyklu",
            uk = "Звіт про цикл",
            es = "Informe del ciclo",
            de = "Zyklusbericht",
        ),
        subtitle = ReportText.subtitle(),
        generatedLabel = tr(
            fr = "Généré le ${formatReportFullDate(report.generatedOn)}",
            en = "Generated on ${formatReportFullDate(report.generatedOn)}",
            pl = "Wygenerowano: ${formatReportFullDate(report.generatedOn)}",
            uk = "Створено: ${formatReportFullDate(report.generatedOn)}",
            es = "Generado el ${formatReportFullDate(report.generatedOn)}",
            de = "Erstellt am ${formatReportFullDate(report.generatedOn)}",
        ),
        confidentialBadge = tr(
            fr = "LUNAISON · CONFIDENTIEL",
            en = "LUNAISON · CONFIDENTIAL",
            pl = "LUNAISON · POUFNE",
            uk = "LUNAISON · КОНФІДЕНЦІЙНО",
            es = "LUNAISON · CONFIDENCIAL",
            de = "LUNAISON · VERTRAULICH",
        ),
        situationTitle = tr(
            fr = "Situation actuelle",
            en = "Current status",
            pl = "Stan obecny",
            uk = "Поточна ситуація",
            es = "Situación actual",
            de = "Aktueller Stand",
        ),
        tiles = tiles,
        categoriesTitle = tr(
            fr = "Catégories partagées",
            en = "Shared categories",
            pl = "Udostępnione kategorie",
            uk = "Обрані категорії",
            es = "Categorías compartidas",
            de = "Geteilte Kategorien",
        ),
        categoryBadges = categoryBadges,
        earlyTracking = earlyTracking,
        consultationNotesTitle = tr(
            fr = "Notes de consultation (à compléter par le praticien)",
            en = "Consultation notes (for practitioner use)",
            pl = "Notatki z wizyty (do wypełnienia przez lekarza)",
            uk = "Нотатки лікаря під час консультації",
            es = "Notas de consulta (para el profesional)",
            de = "Gesprächsnotizen (für die Praxis)",
        ),
        parts = parts,
        referencesTitle = tr(
            fr = "Repères utilisés",
            en = "References used",
            pl = "Wykorzystane źródła i wartości referencyjne",
            uk = "Використані орієнтири",
            es = "Referencias utilizadas",
            de = "Verwendete Referenzen",
        ),
        references = references,
    )
}

private fun usualCycleLength(): String = tr(
    fr = "Durée habituelle du cycle",
    en = "Usual cycle length",
    pl = "Typowa długość cyklu",
    uk = "Звична тривалість циклу",
    es = "Duración habitual del ciclo",
    de = "Übliche Zykluslänge",
)

private fun overviewPart(overview: CycleOverview?): ReportPart? {
    if (overview == null) return null
    val rows = buildList {
        add(
            tr(
                fr = "Cycles complets analysés",
                en = "Completed cycles analysed",
                pl = "Przeanalizowane pełne cykle",
                uk = "Проаналізовані повні цикли",
                es = "Ciclos completos analizados",
                de = "Ausgewertete vollständige Zyklen",
            ) to overview.records.size.toString(),
        )
        add(usualCycleLength() to Format.days(overview.usualLength))
        if (overview.shortest != overview.longest) {
            add(
                tr(
                    fr = "Cycle le plus court et le plus long",
                    en = "Shortest and longest cycle",
                    pl = "Najkrótszy i najdłuższy cykl",
                    uk = "Найкоротший і найдовший цикл",
                    es = "Ciclo más corto y más largo",
                    de = "Kürzester und längster Zyklus",
                ) to tr(
                    fr = "${overview.shortest} à ${Format.days(overview.longest)}",
                    en = "${overview.shortest} to ${Format.days(overview.longest)}",
                    pl = "${overview.shortest} do ${Format.days(overview.longest)}",
                    uk = "від ${overview.shortest} до ${Format.days(overview.longest)}",
                    es = "${overview.shortest} a ${Format.days(overview.longest)}",
                    de = "${overview.shortest} bis ${Format.days(overview.longest)}",
                ),
            )
        }
        overview.usualPeriodLength?.let {
            add(
                tr(
                    fr = "Durée habituelle des règles",
                    en = "Usual period length",
                    pl = "Typowy czas trwania miesiączki",
                    uk = "Звична тривалість менструації",
                    es = "Duración habitual de la menstruación",
                    de = "Übliche Blutungsdauer",
                ) to Format.days(it),
            )
        }
    }
    val reference = if (overview.withinReferences) ReportText.referencesWithin() else ReportText.referencesOutside()
    return ReportPart(
        ReportSection.OVERVIEW,
        ReportText.overviewTitle(),
        listOf(ReportBlock.Rows(rows), ReportBlock.Paragraph(reference)),
    )
}

private fun cyclesHistoryPart(overview: CycleOverview?): ReportPart? {
    if (overview == null || overview.records.isEmpty()) return null
    val rows = overview.records.reversed().map { rec ->
        listOf(
            formatReportShortDate(rec.start),
            Format.days(rec.length),
            rec.periodLength?.let { Format.days(it) } ?: "-",
            if (rec.painDays > 0) Format.days(rec.painDays) else "-",
        )
    }
    val count = overview.records.size
    val summary = if (overview.shortest == overview.longest) {
        tr(
            fr = "Sur $count cycles : ${Format.days(overview.usualLength)} à chaque fois.",
            en = "Over $count cycles: ${Format.days(overview.usualLength)} every time.",
            pl = "Liczba cykli: $count. Za każdym razem: ${Format.days(overview.usualLength)}.",
            uk = "Кількість циклів: $count. Щоразу: ${Format.days(overview.usualLength)}.",
            es = "Ciclos analizados: $count. Duración en todos: ${Format.days(overview.usualLength)}.",
            de = "Anzahl der Zyklen: $count. Jedes Mal: ${Format.days(overview.usualLength)}.",
        )
    } else {
        tr(
            fr = "Sur $count cycles : durée habituelle ${Format.days(overview.usualLength)} (médiane), de ${overview.shortest} à ${Format.days(overview.longest)}.",
            en = "Over $count cycles: usual length ${Format.days(overview.usualLength)} (median), from ${overview.shortest} to ${Format.days(overview.longest)}.",
            pl = "Liczba cykli: $count. Typowa długość: ${Format.days(overview.usualLength)} (mediana), zakres: od ${overview.shortest} do ${Format.days(overview.longest)}.",
            uk = "Кількість циклів: $count. Звична тривалість: ${Format.days(overview.usualLength)} (медіана), діапазон: від ${overview.shortest} до ${Format.days(overview.longest)}.",
            es = "Ciclos analizados: $count. Duración habitual: ${Format.days(overview.usualLength)} (mediana), de ${overview.shortest} a ${Format.days(overview.longest)}.",
            de = "Anzahl der Zyklen: $count. Übliche Länge: ${Format.days(overview.usualLength)} (Median), Spanne: ${overview.shortest} bis ${Format.days(overview.longest)}.",
        )
    }
    val headers = listOf(
        tr(fr = "Début", en = "Start", pl = "Początek", uk = "Початок", es = "Inicio", de = "Beginn"),
        ReportText.cycleLength(),
        ReportText.periodLength(),
        ReportText.painBothersomeOrSevere(),
    )
    return ReportPart(
        ReportSection.CYCLES_HISTORY,
        ReportText.cyclesHistoryTitle(),
        listOf(ReportBlock.CycleTable(headers, rows, summary)),
    )
}

private fun painPart(logs: List<DayLog>, period: String): ReportPart? {
    val severe = logs.count { it.pain == Pain.SEVERE }
    val moderate = logs.count { it.pain == Pain.MODERATE }
    val pelvic = logs.count { Symptom.PELVIC_PAIN in it.symptoms }
    val dyspareunia = logs.count { Symptom.DYSPAREUNIA in it.symptoms }
    val digestive = logs.count { Symptom.DIGESTIVE_PAIN in it.symptoms }
    val urinary = logs.count { Symptom.URINARY_PAIN in it.symptoms }
    val radiating = logs.count { Symptom.RADIATING_PAIN in it.symptoms }
    val cramps = logs.count { Symptom.CRAMPS in it.symptoms }
    val hasPainData = severe > 0 || moderate > 0 || pelvic > 0 || dyspareunia > 0 ||
        digestive > 0 || urinary > 0 || radiating > 0 || cramps > 0
    if (!hasPainData) return null

    fun bar(label: String, days: Int) = ReportBar(label, days, Format.days(days))
    val bars = buildList {
        if (severe > 0 || moderate > 0) {
            add(bar(ReportText.painBothersomeOrSevere(), severe + moderate))
            if (severe > 0) {
                add(
                    bar(
                        tr(
                            fr = "dont douleur forte",
                            en = "of which severe pain",
                            pl = "w tym ból silny",
                            uk = "з них сильний біль",
                            es = "de los cuales, dolor fuerte",
                            de = "davon starke Schmerzen",
                        ),
                        severe,
                    ),
                )
            }
        }
        if (cramps > 0) add(bar(ReportText.painCramps(), cramps))
        if (pelvic > 0) add(bar(ReportText.painPelvic(), pelvic))
        if (dyspareunia > 0) add(bar(ReportText.painDyspareunia(), dyspareunia))
        if (digestive > 0) add(bar(ReportText.painDigestive(), digestive))
        if (urinary > 0) add(bar(ReportText.painUrinary(), urinary))
        if (radiating > 0) add(bar(ReportText.painRadiating(), radiating))
    }
    val daysNote = tr(
        fr = "Nombre de jours notés sur ${ReportDocument.PERIOD_DAYS}.",
        en = "Number of days logged out of ${ReportDocument.PERIOD_DAYS}.",
        pl = "Liczba dni z zapisem na ${ReportDocument.PERIOD_DAYS}.",
        uk = "Кількість днів із записами з ${ReportDocument.PERIOD_DAYS}.",
        es = "Número de días con registro sobre ${ReportDocument.PERIOD_DAYS}.",
        de = "Anzahl der erfassten Tage von ${ReportDocument.PERIOD_DAYS}.",
    )
    return ReportPart(
        ReportSection.PAIN_AND_ENDOMETRIOSIS,
        tr(
            fr = "Douleurs et repères endométriose ($period)",
            en = "Pain and endometriosis indicators ($period)",
            pl = "Ból i wskaźniki endometriozy ($period)",
            uk = "Біль та ознаки ендометріозу ($period)",
            es = "Dolor e indicadores de endometriosis ($period)",
            de = "Schmerzen und Endometriose-Anhaltspunkte ($period)",
        ),
        listOf(ReportBlock.Bars(bars), ReportBlock.Paragraph("$daysNote ${ReportText.painGuidelines()}", muted = true)),
    )
}

private fun symptomsPart(report: HealthReport): ReportPart? {
    if (report.phasePatterns.isEmpty()) return null
    val rows = report.phasePatterns.map { p ->
        val items = p.symptoms.map { "${ReportText.inline(it.first.label)} (${it.second}/${p.cycles})" } +
            p.moods.map { "${ReportText.inline(it.first.label)} (${it.second}/${p.cycles})" }
        Format.phaseName(p.phase) to items.joinToString(", ")
    }
    return ReportPart(
        ReportSection.SYMPTOMS_AND_MOODS,
        ReportText.symptomsTitle(),
        listOf(
            ReportBlock.Rows(rows),
            ReportBlock.Paragraph(
                tr(
                    fr = "Entre parenthèses : nombre de cycles où l'élément a été noté, sur le nombre de cycles renseignés pour la phase.",
                    en = "In brackets: number of cycles where the item was logged, out of the cycles logged for that phase.",
                    pl = "W nawiasach: liczba cykli, w których dany element zapisano, w stosunku do liczby cykli z zapisami dla danej fazy.",
                    uk = "У дужках: кількість циклів, у яких пункт було відмічено, з кількості циклів із записами для фази.",
                    es = "Entre paréntesis: número de ciclos en los que se registró el elemento, sobre el número de ciclos con datos de esa fase.",
                    de = "In Klammern: Anzahl der Zyklen, in denen der Punkt erfasst wurde, bezogen auf die Zyklen mit Einträgen für die Phase.",
                ),
                muted = true,
            ),
        ),
    )
}

private fun medicationsPart(medicationLabel: String, logs: List<DayLog>, period: String): ReportPart? {
    val taken = logs.count { it.pillStatus == PillStatus.TAKEN || it.medicationTaken }
    val missed = logs.count { it.pillStatus == PillStatus.MISSED }
    val meds = logs.flatMap { it.medications }.groupingBy { it }.eachCount()
    if (taken == 0 && missed == 0 && meds.isEmpty()) return null
    val label = medicationLabel.trim().ifBlank { ReportText.defaultMedicationLabel() }
    val rows = buildList {
        if (taken > 0) add(label to ReportText.pillTaken(taken))
        if (missed > 0) add(label to ReportText.pillMissed(missed))
        meds.forEach { (kind, count) -> add(kind.label to ReportText.loggedDays(count)) }
    }
    return ReportPart(
        ReportSection.MEDICATIONS,
        tr(
            fr = "Contraception et traitements notés ($period)",
            en = "Contraception and medications ($period)",
            pl = "Antykoncepcja i leki, zapisy ($period)",
            uk = "Контрацепція та ліки, записи ($period)",
            es = "Anticoncepción y tratamientos registrados ($period)",
            de = "Verhütung und erfasste Medikamente ($period)",
        ),
        listOf(ReportBlock.Rows(rows)),
    )
}

private fun weightPart(logs: List<DayLog>, period: String): ReportPart? {
    val weights = logs.mapNotNull { log -> log.weight?.let { log.date to it.toDouble() } }.sortedByDescending { it.first }
    if (weights.isEmpty()) return null
    val (latestDate, latest) = weights.first()
    val rows = buildList {
        add(ReportText.lastWeight() to "${ReportText.decimal(latest)} ${ReportText.kg()} (${Format.dayMonth(latestDate)})")
        if (weights.size > 1) {
            val min = weights.minOf { it.second }
            val max = weights.maxOf { it.second }
            add(ReportText.weightRange() to ReportText.weightRangeValue(min, max, weights.size))
        }
    }
    return ReportPart(
        ReportSection.WEIGHT,
        tr(
            fr = "Suivi du poids ($period)",
            en = "Weight tracking ($period)",
            pl = "Kontrola wagi ($period)",
            uk = "Контроль ваги ($period)",
            es = "Seguimiento del peso ($period)",
            de = "Gewichtsverlauf ($period)",
        ),
        listOf(ReportBlock.Rows(rows)),
    )
}

private fun ovulationPart(logs: List<DayLog>, period: String): ReportPart? {
    val mucusDays = logs.count { it.mucus != null }
    val temps = logs.mapNotNull { it.basalTemperature?.toDouble() }
    if (mucusDays == 0 && temps.isEmpty()) return null
    val rows = buildList {
        if (mucusDays > 0) add(ReportText.mucusObservations() to Format.days(mucusDays))
        if (temps.isNotEmpty()) add(ReportText.basalTemperature() to ReportText.basalTemperatureValue(temps.size, temps.average()))
    }
    return ReportPart(
        ReportSection.OVULATION_SIGNS,
        tr(
            fr = "Signes d'ovulation, glaire et température ($period)",
            en = "Ovulation signs, mucus and temperature ($period)",
            pl = "Objawy owulacji, śluz szyjkowy i temperatura ($period)",
            uk = "Ознаки овуляції, цервікальний слиз і температура ($period)",
            es = "Signos de ovulación, moco cervical y temperatura ($period)",
            de = "Ovulationszeichen, Zervixschleim und Temperatur ($period)",
        ),
        listOf(ReportBlock.Rows(rows)),
    )
}

private fun intimacyPart(logs: List<DayLog>, period: String): ReportPart? {
    val protectedCount = logs.count { IntimacyKind.PROTECTED in it.intimacy }
    val unprotectedCount = logs.count { IntimacyKind.UNPROTECTED in it.intimacy }
    val nonPenetrativeCount = logs.count { IntimacyKind.NON_PENETRATIVE in it.intimacy }
    if (protectedCount + unprotectedCount + nonPenetrativeCount == 0) return null
    val rows = buildList {
        if (protectedCount > 0) add(ReportText.intimacyProtected() to Format.days(protectedCount))
        if (unprotectedCount > 0) add(ReportText.intimacyUnprotected() to Format.days(unprotectedCount))
        if (nonPenetrativeCount > 0) add(ReportText.intimacyNonPenetrative() to Format.days(nonPenetrativeCount))
    }
    return ReportPart(
        ReportSection.INTIMACY,
        tr(
            fr = "Vie intime notée ($period)",
            en = "Logged intimacy ($period)",
            pl = "Życie intymne, zapisy ($period)",
            uk = "Інтимне життя, записи ($period)",
            es = "Vida íntima registrada ($period)",
            de = "Erfasste Intimität ($period)",
        ),
        listOf(ReportBlock.Rows(rows)),
    )
}

private fun notesPart(logs: List<DayLog>, period: String): ReportPart? {
    val notes = logs.filter { it.note.isNotBlank() }.sortedByDescending { it.date }.take(5)
    if (notes.isEmpty()) return null
    val rows = notes.map { log ->
        val text = log.note.trim()
        Format.dayMonth(log.date) to if (text.length > MAX_NOTE_LENGTH) text.take(MAX_NOTE_LENGTH).trimEnd() + "..." else text
    }
    return ReportPart(
        ReportSection.NOTES,
        tr(
            fr = "Notes personnelles récentes ($period)",
            en = "Recent personal notes ($period)",
            pl = "Ostatnie notatki osobiste ($period)",
            uk = "Останні особисті нотатки ($period)",
            es = "Notas personales recientes ($period)",
            de = "Aktuelle persönliche Notizen ($period)",
        ),
        listOf(ReportBlock.Rows(rows)),
    )
}

private fun alertsPart(report: HealthReport): ReportPart? {
    if (report.alerts.isEmpty()) return null
    return ReportPart(
        ReportSection.ALERTS,
        ReportText.alertsTitle(),
        listOf(ReportBlock.Alerts(report.alerts.map { ReportAlert(it.title, it.message) })),
    )
}
