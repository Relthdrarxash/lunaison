package fr.lunaison.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import fr.lunaison.core.content.Sources
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.designsystem.LunaColors
import fr.lunaison.core.designsystem.R as DesignR
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// ---------------------------------------------------------------------------------------------
// Contenu du bilan : aucune dépendance Android, testable sur la JVM.
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

/** Le bilan prêt à dessiner. */
data class ReportDocument(
    val title: String,
    val subtitle: String,
    val generatedLabel: String,
    val situationTitle: String,
    val tiles: List<ReportTile>,
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

private fun fullDate(date: LocalDate): String =
    date.toJava().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(ReportText.locale()))

private fun shortDate(date: LocalDate): String =
    date.toJava().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(ReportText.locale()))

private const val MAX_NOTE_LENGTH = 600

/**
 * Le polonais et l'ukrainien ont des lettres (ą ć ę ł ń ś ź ż, cyrillique) absentes d'Atkinson Hyperlegible
 * telle qu'embarquée dans l'appli (219 glyphes : latin de base et Latin-1, sans Latin étendu A ni cyrillique).
 * Pour ces langues le PDF utilise la police sans empattement du système, qui les couvre.
 */
internal fun reportNeedsSystemFont(language: Language): Boolean = language == Language.PL || language == Language.UK

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
            fr = "Généré le ${fullDate(report.generatedOn)}",
            en = "Generated on ${fullDate(report.generatedOn)}",
            pl = "Wygenerowano: ${fullDate(report.generatedOn)}",
            uk = "Створено: ${fullDate(report.generatedOn)}",
            es = "Generado el ${fullDate(report.generatedOn)}",
            de = "Erstellt am ${fullDate(report.generatedOn)}",
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
            shortDate(rec.start),
            Format.days(rec.length),
            rec.periodLength?.let { Format.days(it) } ?: "-",
            if (rec.painDays > 0) Format.days(rec.painDays) else "-",
        )
    }
    // Le nombre de cycles passe en « libellé : nombre » hors français et anglais (pas d'accord à faire).
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

// ---------------------------------------------------------------------------------------------
// Dessin : PdfDocument, Canvas, StaticLayout. Aucune bibliothèque tierce, aucun accès au réseau.
// ---------------------------------------------------------------------------------------------

/** Écrit le bilan dans un PDF A4, paginé automatiquement. */
object HealthReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
    private const val HEADER_HEIGHT = 90f
    private const val FIRST_PAGE_TOP = HEADER_HEIGHT + 20f
    private const val PAGE_TOP = MARGIN
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - 66f
    private const val FOOTER_RULE_Y = PAGE_HEIGHT - 50f
    private const val LINE_SPACING = 1.22f

    /** Dossier du cache où le PDF est écrit puis partagé : voir res/xml/file_paths.xml. */
    const val CACHE_DIRECTORY = "rapports"
    const val FILE_NAME = "bilan-lunaison.pdf"

    /** Écrit le PDF dans le cache de l'appli (en écrasant le précédent) et renvoie le fichier. */
    fun export(context: Context, document: ReportDocument): File {
        val directory = File(context.cacheDir, CACHE_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) throw java.io.IOException("Dossier $CACHE_DIRECTORY inaccessible")
        val file = File(directory, FILE_NAME)
        write(context, document, file)
        return file
    }

    fun write(context: Context, document: ReportDocument, file: File) {
        val fonts = Fonts.load(context, Language.current())
        val chunks = ReportLayout(fonts).chunks(document)
        val pages = paginate(chunks)
        val pdf = PdfDocument()
        try {
            pages.forEachIndexed { index, placed ->
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                val page = pdf.startPage(info)
                val canvas = page.canvas
                if (index == 0) drawHeader(canvas, document, fonts)
                placed.forEach { it.chunk.draw(canvas, MARGIN, it.y) }
                drawFooter(canvas, fonts, index + 1, pages.size)
                pdf.finishPage(page)
            }
            FileOutputStream(file).use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
    }

    // --- Pagination ---------------------------------------------------------------------------

    private class Placed(val chunk: Chunk, val y: Float)

    /**
     * Répartit les morceaux sur les pages : on ne coupe qu'entre deux morceaux, un morceau marqué
     * « garder avec le suivant » (titre de rubrique, en-tête de tableau) n'est jamais seul en bas de page,
     * et l'en-tête d'un tableau est répété si le tableau continue sur la page suivante.
     */
    private fun paginate(chunks: List<Chunk>): List<List<Placed>> {
        val pages = ArrayList<MutableList<Placed>>()
        var current = ArrayList<Placed>()
        var y = FIRST_PAGE_TOP
        var top = FIRST_PAGE_TOP
        var lastTableHeader: Chunk? = null

        fun newPage() {
            pages.add(current)
            current = ArrayList()
            top = PAGE_TOP
            y = PAGE_TOP
        }

        var index = 0
        while (index < chunks.size) {
            val chunk = chunks[index]
            if (chunk.isTableHeader) lastTableHeader = chunk
            var need = chunk.before + chunk.height
            var last = index
            while (chunks[last].keepWithNext && last + 1 < chunks.size) {
                last++
                need += chunks[last].before + chunks[last].height
            }
            val atTop = y == top
            if (!atTop && y + need > CONTENT_BOTTOM) {
                newPage()
                val header = lastTableHeader
                if (chunk.repeatsTableHeader && header != null) {
                    current.add(Placed(header, y))
                    y += header.height
                }
            }
            val before = if (y == top) 0f else chunk.before
            current.add(Placed(chunk, y + before))
            y += before + chunk.height
            index++
        }
        pages.add(current)
        return pages
    }

    // --- En-tête et pied de page --------------------------------------------------------------

    private fun drawHeader(canvas: Canvas, document: ReportDocument, fonts: Fonts) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.color = LunaColors.Plum.toArgb()
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), HEADER_HEIGHT, fill)

        val logoSize = 56f
        val logoLeft = MARGIN
        val logoTop = (HEADER_HEIGHT - logoSize) / 2f
        drawLogo(canvas, logoLeft, logoTop, logoSize)

        val textLeft = logoLeft + logoSize + 16f
        val title = fonts.paint(LunaColors.Cream, 22f, bold = true)
        canvas.drawText(document.title, textLeft, HEADER_HEIGHT / 2f - 1f, title)
        val subtitle = fonts.paint(LunaColors.Cream, 11f)
        canvas.drawText(document.subtitle, textLeft, HEADER_HEIGHT / 2f + 18f, subtitle)

        val dateText = fonts.paint(LunaColors.Cream, 9f)
        dateText.textAlign = Paint.Align.RIGHT
        canvas.drawText(document.generatedLabel, PAGE_WIDTH - MARGIN, HEADER_HEIGHT / 2f + 3f, dateText)
    }

    /**
     * Croissant de l'icône de l'appli (app/src/main/res/drawable/ic_launcher_foreground.xml, viewport 108,
     * groupe agrandi de 1,25 autour de 54,54), redessiné ici car le module de l'interface n'a pas accès
     * aux ressources de l'application. L'icône a un fond prune : on pose le logo sur une pastille
     * prune un peu plus claire pour qu'il se détache du bandeau.
     */
    private fun drawLogo(canvas: Canvas, left: Float, top: Float, size: Float) {
        val tile = Paint(Paint.ANTI_ALIAS_FLAG)
        tile.color = 0xFF5E3A64.toInt()
        canvas.drawRoundRect(RectF(left, top, left + size, top + size), size * 0.26f, size * 0.26f, tile)
        try {
            val scale = size / 108f
            val matrix = Matrix()
            matrix.setScale(1.25f, 1.25f, 54f, 54f)
            matrix.postScale(scale, scale)
            matrix.postTranslate(left, top)

            val moon = PathParser.createPathFromPathData("M60,32 A23,23 0 1,0 76,64 A18,18 0 1,1 60,32 Z")
            moon.transform(matrix)
            val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            moonPaint.color = LunaColors.Cream.toArgb()
            canvas.drawPath(moon, moonPaint)

            val dot: Path = PathParser.createPathFromPathData("M71,38 m-3,0 a3,3 0 1,0 6,0 a3,3 0 1,0 -6,0")
            dot.transform(matrix)
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            dotPaint.color = LunaColors.ClayNight.toArgb()
            canvas.drawPath(dot, dotPaint)
        } catch (_: RuntimeException) {
            // Tracé illisible : le bandeau garde sa pastille, le reste du bilan n'est pas touché.
        }
    }

    private fun drawFooter(canvas: Canvas, fonts: Fonts, page: Int, total: Int) {
        val rule = Paint(Paint.ANTI_ALIAS_FLAG)
        rule.color = LunaColors.Line.toArgb()
        rule.strokeWidth = 0.75f
        canvas.drawLine(MARGIN, FOOTER_RULE_Y, PAGE_WIDTH - MARGIN, FOOTER_RULE_Y, rule)

        val left = tr(
            fr = "Lunaison · page $page / $total",
            en = "Lunaison · page $page / $total",
            pl = "Lunaison · strona $page / $total",
            uk = "Lunaison · стор. $page / $total",
            es = "Lunaison · página $page / $total",
            de = "Lunaison · Seite $page / $total",
        )
        val leftPaint = fonts.textPaint(LunaColors.InkMuted, 8f, bold = true)
        val leftWidth = 125
        canvas.save()
        canvas.translate(MARGIN, FOOTER_RULE_Y + 8f)
        textLayout(left, leftPaint, leftWidth).draw(canvas)
        canvas.restore()

        val note = tr(
            fr = "Généré sur le téléphone de la patiente. Ce document n'est pas un diagnostic médical.",
            en = "Generated on the patient's phone. This document is not a medical diagnosis.",
            pl = "Wygenerowano na telefonie pacjentki. Ten dokument nie jest diagnozą medyczną.",
            uk = "Створено на телефоні пацієнтки. Цей документ не є медичним діагнозом.",
            es = "Generado en el teléfono de la paciente. Este documento no es un diagnóstico médico.",
            de = "Auf dem Smartphone der Patientin erstellt. Dieses Dokument ist keine medizinische Diagnose.",
        )
        val notePaint = fonts.textPaint(LunaColors.InkMuted, 8f)
        val noteWidth = (CONTENT_WIDTH - leftWidth - 12f).toInt()
        canvas.save()
        canvas.translate(MARGIN + leftWidth + 12f, FOOTER_RULE_Y + 8f)
        textLayout(note, notePaint, noteWidth, Layout.Alignment.ALIGN_OPPOSITE).draw(canvas)
        canvas.restore()
    }

    // --- Morceaux dessinables -----------------------------------------------------------------

    private fun textLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
        .setAlignment(alignment)
        .setLineSpacing(0f, LINE_SPACING)
        .setIncludePad(false)
        .build()

    private fun Canvas.drawLayout(layout: StaticLayout, x: Float, y: Float) {
        save()
        translate(x, y)
        layout.draw(this)
        restore()
    }

    /** Un morceau de page : jamais coupé, de hauteur connue avant d'être dessiné. */
    private abstract class Chunk(
        /** Espace laissé au-dessus, ignoré en haut de page. */
        val before: Float = 0f,
        val keepWithNext: Boolean = false,
    ) {
        abstract val height: Float
        open val isTableHeader: Boolean get() = false
        open val repeatsTableHeader: Boolean get() = false
        abstract fun draw(canvas: Canvas, x: Float, y: Float)
    }

    private class Fonts(private val regular: Typeface, private val bold: Typeface) {
        fun textPaint(color: androidx.compose.ui.graphics.Color, size: Float, bold: Boolean = false): TextPaint {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            paint.color = color.toArgb()
            paint.textSize = size
            paint.typeface = if (bold) this.bold else regular
            return paint
        }

        fun paint(color: androidx.compose.ui.graphics.Color, size: Float, bold: Boolean = false): Paint =
            textPaint(color, size, bold)

        companion object {
            /**
             * Atkinson Hyperlegible comme dans l'appli, ou la police sans empattement du système quand elle
             * ne couvre pas la langue (polonais, ukrainien : voir [reportNeedsSystemFont]).
             */
            fun load(context: Context, language: Language): Fonts {
                if (reportNeedsSystemFont(language)) {
                    return Fonts(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
                }
                fun font(id: Int, style: Int): Typeface = try {
                    ResourcesCompat.getFont(context, id) ?: Typeface.create(Typeface.SANS_SERIF, style)
                } catch (_: Exception) {
                    Typeface.create(Typeface.SANS_SERIF, style)
                }
                return Fonts(font(DesignR.font.atkinson_regular, Typeface.NORMAL), font(DesignR.font.atkinson_bold, Typeface.BOLD))
            }
        }
    }

    private class ReportLayout(private val fonts: Fonts) {
        private val width = CONTENT_WIDTH.toInt()
        private val body = fonts.textPaint(LunaColors.Ink, 10.5f)
        private val bodyBold = fonts.textPaint(LunaColors.Ink, 10.5f, bold = true)
        private val label = fonts.textPaint(LunaColors.InkMuted, 10.5f)
        private val small = fonts.textPaint(LunaColors.InkMuted, 8.5f)
        private val heading = fonts.textPaint(LunaColors.Plum, 13f, bold = true)

        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LunaColors.Line.toArgb()
            strokeWidth = 0.75f
        }

        fun chunks(document: ReportDocument): List<Chunk> {
            val list = ArrayList<Chunk>()
            list.add(SituationChunk(document))
            document.parts.forEach { part ->
                list.add(HeadingChunk(part.title))
                part.blocks.forEach { block -> list.addAll(blockChunks(block)) }
            }
            if (document.references.isNotEmpty()) {
                list.add(HeadingChunk(document.referencesTitle, small = true))
                document.references.forEach { list.add(ParagraphChunk(it, small, 2f)) }
            }
            return list
        }

        private fun blockChunks(block: ReportBlock): List<Chunk> = when (block) {
            is ReportBlock.Rows -> block.rows.map { RowChunk(it.first, it.second) }
            is ReportBlock.Bullets -> block.items.map { BulletChunk(it) }
            is ReportBlock.Paragraph -> listOf(ParagraphChunk(block.text, if (block.muted) small else body, 4f))
            is ReportBlock.CycleTable -> tableChunks(block)
            is ReportBlock.Bars -> block.bars.map { BarChunk(it) }
            is ReportBlock.Alerts -> block.items.mapIndexed { i, item -> AlertChunk(item, i == 0, i == block.items.lastIndex) }
        }

        private fun tableChunks(table: ReportBlock.CycleTable): List<Chunk> {
            val columns = floatArrayOf(0.27f, 0.22f, 0.22f, 0.29f).map { it * CONTENT_WIDTH }
            val header = TableRowChunk(table.headers, columns, header = true, striped = false)
            val rows = table.rows.mapIndexed { i, row -> TableRowChunk(row, columns, header = false, striped = i % 2 == 1, repeatHeader = true) }
            val all = ArrayList<Chunk>()
            all.add(header)
            all.addAll(rows)
            table.summary?.let { all.add(ParagraphChunk(it, small, 6f)) }
            return all
        }

        // Titre de rubrique : filet Clay à gauche, texte Plum gras.
        private inner class HeadingChunk(text: String, small: Boolean = false) :
            Chunk(before = 16f, keepWithNext = true) {
            private val paint = if (small) fonts.textPaint(LunaColors.Plum, 10f, bold = true) else heading
            private val layout = textLayout(text, paint, width - 12)
            private val extra = 6f
            override val height = layout.height + extra
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val bar = Paint(Paint.ANTI_ALIAS_FLAG)
                bar.color = LunaColors.Clay.toArgb()
                canvas.drawRoundRect(RectF(x, y + 1f, x + 3f, y + layout.height - 1f), 1.5f, 1.5f, bar)
                canvas.drawLayout(layout, x + 11f, y)
            }
        }

        private inner class ParagraphChunk(text: String, paint: TextPaint, private val gap: Float) :
            Chunk() {
            private val layout = textLayout(text, paint, width)
            override val height = layout.height + gap
            override fun draw(canvas: Canvas, x: Float, y: Float) = canvas.drawLayout(layout, x, y)
        }

        private inner class BulletChunk(text: String) : Chunk() {
            private val layout = textLayout(text, body, width - 16)
            override val height = layout.height + 4f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val dot = Paint(Paint.ANTI_ALIAS_FLAG)
                dot.color = LunaColors.Clay.toArgb()
                canvas.drawCircle(x + 3f, y + body.textSize * 0.62f, 1.8f, dot)
                canvas.drawLayout(layout, x + 14f, y)
            }
        }

        // Ligne libellé à gauche, valeur à droite, filet fin dessous.
        private inner class RowChunk(labelText: String, valueText: String) : Chunk() {
            private val labelWidth = 170
            private val labelLayout = textLayout(labelText, label, labelWidth)
            private val valueLayout = textLayout(valueText, bodyBold, width - labelWidth - 14)
            override val height = maxOf(labelLayout.height, valueLayout.height) + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                canvas.drawLayout(labelLayout, x, y + 3f)
                canvas.drawLayout(valueLayout, x + labelWidth + 14f, y + 3f)
                canvas.drawLine(x, y + height - 1f, x + width, y + height - 1f, line)
            }
        }

        // Libellé, barre proportionnelle sur 90 jours en Clay pâle, nombre de jours aligné à droite.
        private inner class BarChunk(private val bar: ReportBar) : Chunk() {
            private val labelWidth = 235
            private val trackLeft = 245f
            private val trackWidth = 120f
            private val valueWidth = 95
            private val labelLayout = textLayout(bar.label, body, labelWidth)
            private val valueLayout = textLayout(bar.valueText, bodyBold, valueWidth, Layout.Alignment.ALIGN_OPPOSITE)
            override val height = maxOf(labelLayout.height, valueLayout.height) + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                canvas.drawLayout(labelLayout, x, y + 3f)
                val centerY = y + 3f + body.textSize * 0.6f
                val track = Paint(Paint.ANTI_ALIAS_FLAG)
                track.color = LunaColors.Line.toArgb()
                canvas.drawRoundRect(RectF(x + trackLeft, centerY - 3.5f, x + trackLeft + trackWidth, centerY + 3.5f), 3.5f, 3.5f, track)
                val ratio = (bar.days.toFloat() / ReportDocument.PERIOD_DAYS).coerceIn(0f, 1f)
                if (ratio > 0f) {
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG)
                    fill.color = 0xFFD9958C.toInt()
                    val right = x + trackLeft + maxOf(trackWidth * ratio, 7f)
                    canvas.drawRoundRect(RectF(x + trackLeft, centerY - 3.5f, right, centerY + 3.5f), 3.5f, 3.5f, fill)
                }
                canvas.drawLayout(valueLayout, x + width - valueWidth, y + 3f)
                canvas.drawLine(x, y + height - 1f, x + width, y + height - 1f, line)
            }
        }

        // Encadré d'alerte : fond ClaySoft, bordure Clay, un morceau par alerte pour pouvoir couper entre deux.
        private inner class AlertChunk(item: ReportAlert, private val first: Boolean, private val last: Boolean) :
            Chunk(before = if (first) 2f else 0f) {
            private val titleLayout = textLayout(item.title, bodyBold, width - 28)
            private val messageLayout = textLayout(item.message, body, width - 28)
            override val height = 9f + titleLayout.height + 2f + messageLayout.height + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val fill = Paint(Paint.ANTI_ALIAS_FLAG)
                fill.color = LunaColors.ClaySoft.toArgb()
                canvas.drawRect(x, y, x + width, y + height, fill)
                val border = Paint(Paint.ANTI_ALIAS_FLAG)
                border.color = LunaColors.Clay.toArgb()
                border.strokeWidth = 1f
                canvas.drawLine(x, y, x, y + height, border)
                canvas.drawLine(x + width, y, x + width, y + height, border)
                if (first) canvas.drawLine(x, y, x + width, y, border)
                if (last) canvas.drawLine(x, y + height, x + width, y + height, border)
                canvas.drawRect(x, y, x + 4f, y + height, border)
                canvas.drawLayout(titleLayout, x + 16f, y + 9f)
                canvas.drawLayout(messageLayout, x + 16f, y + 9f + titleLayout.height + 2f)
            }
        }

        // Tableau des cycles : en-tête PlumSoft, lignes alternées très légères.
        private inner class TableRowChunk(
            cells: List<String>,
            private val columns: List<Float>,
            private val header: Boolean,
            private val striped: Boolean,
            private val repeatHeader: Boolean = false,
        ) : Chunk(keepWithNext = header) {
            private val paint = if (header) fonts.textPaint(LunaColors.Plum, 9f, bold = true) else body
            private val layouts = cells.mapIndexed { i, text -> textLayout(text, paint, (columns[i] - 16f).toInt()) }
            override val height = layouts.maxOf { it.height } + if (header) 12f else 10f
            override val isTableHeader: Boolean get() = header
            override val repeatsTableHeader: Boolean get() = repeatHeader

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                if (header || striped) {
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG)
                    fill.color = if (header) LunaColors.PlumSoft.toArgb() else 0xFFFAF6F8.toInt()
                    canvas.drawRect(x, y, x + width, y + height, fill)
                }
                var left = x
                layouts.forEachIndexed { i, layout ->
                    canvas.drawLayout(layout, left + 8f, y + (if (header) 6f else 5f))
                    left += columns[i]
                }
                if (!header) canvas.drawLine(x, y + height, x + width, y + height, line)
            }
        }

        // Situation actuelle : encadré PlumSoft avec les chiffres clés en tuiles.
        private inner class SituationChunk(document: ReportDocument) : Chunk() {
            private val padding = 14f
            private val gap = 10f
            private val titleLayout = textLayout(document.situationTitle, fonts.textPaint(LunaColors.Plum, 12f, bold = true), width - 2 * padding.toInt())
            private val tileWidth = (width - 2 * padding - gap * (document.tiles.size - 1)) / document.tiles.size
            private val valuePaints = document.tiles.map { tile ->
                val paint = fonts.textPaint(LunaColors.Plum, 18f, bold = true)
                while (paint.measureText(tile.value) > tileWidth - 16f && paint.textSize > 9f) paint.textSize -= 0.5f
                paint
            }
            private val labelLayouts = document.tiles.map { tile ->
                val text = if (tile.detail != null) tile.label + "\n" + tile.detail else tile.label
                textLayout(text, small, (tileWidth - 16f).toInt())
            }
            private val tileHeight = 10f + 24f + 3f + (labelLayouts.maxOfOrNull { it.height } ?: 0) + 10f
            private val tiles = document.tiles
            override val height = padding + titleLayout.height + 8f + tileHeight + padding + 6f

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val boxHeight = height - 6f
                val frame = Paint(Paint.ANTI_ALIAS_FLAG)
                frame.color = LunaColors.PlumSoft.toArgb()
                canvas.drawRoundRect(RectF(x, y, x + width, y + boxHeight), 8f, 8f, frame)
                canvas.drawLayout(titleLayout, x + padding, y + padding)
                val tileTop = y + padding + titleLayout.height + 8f
                val tileFill = Paint(Paint.ANTI_ALIAS_FLAG)
                tileFill.color = 0xFFFFFFFF.toInt()
                tiles.forEachIndexed { i, tile ->
                    val left = x + padding + i * (tileWidth + gap)
                    canvas.drawRoundRect(RectF(left, tileTop, left + tileWidth, tileTop + tileHeight), 6f, 6f, tileFill)
                    canvas.drawText(tile.value, left + 8f, tileTop + 10f + 18f, valuePaints[i])
                    canvas.drawLayout(labelLayouts[i], left + 8f, tileTop + 10f + 24f + 3f)
                }
            }
        }
    }
}
