package fr.lunaison.ui

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecord
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.model.CervicalMucus
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.MedicationKind
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import kotlinx.datetime.LocalDate
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import fr.lunaison.core.model.minusDays
import fr.lunaison.core.model.of

class HealthReportDocumentTest {

    private val original = Locale.getDefault()

    @BeforeTest
    fun useFrench() = Locale.setDefault(Locale.FRENCH)

    @AfterTest
    fun restore() = Locale.setDefault(original)

    private val today = LocalDate.of(2026, 11, 9)

    private fun record(start: LocalDate, length: Int, period: Int?, pain: Int) =
        CycleRecord(start = start, length = length, periodLength = period, painDays = pain, expectedLength = null)

    private val overview = CycleOverview(
        records = listOf(
            record(LocalDate.of(2026, 8, 1), 28, 5, 0),
            record(LocalDate.of(2026, 8, 29), 30, 4, 2),
            record(LocalDate.of(2026, 9, 28), 29, null, 0),
        ),
        usualLength = 29,
        shortest = 28,
        longest = 30,
        usualPeriodLength = 5,
        withinReferences = true,
        typicalPredictionError = null,
        predictionsChecked = 0,
    )

    private fun report(alerts: List<AlertKind> = emptyList(), spotting: Int = 0, withOverview: Boolean = true) = HealthReport(
        generatedOn = today,
        lastPeriodStart = LocalDate.of(2026, 10, 27),
        dayOfCycle = 14,
        overview = if (withOverview) overview else null,
        recentPeriods = emptyList(),
        phasePatterns = emptyList(),
        alerts = alerts,
        spottingCountLast90Days = spotting,
        medicationDaysLast30Days = 0,
    )

    private val logs = listOf(
        DayLog(date = today.minusDays(3), pain = Pain.SEVERE, weight = 61.5f, note = "Migraine"),
        DayLog(date = today.minusDays(5), pain = Pain.MODERATE, basalTemperature = 36.4f),
    )

    private fun build(sections: Set<ReportSection>, alerts: List<AlertKind> = emptyList(), logs: List<DayLog> = this.logs) =
        buildReportDocument(report(alerts), "", logs, sections)

    @Test
    fun onlyTickedSectionsAppearInEnumOrder() {
        val doc = build(setOf(ReportSection.WEIGHT, ReportSection.CYCLES_HISTORY, ReportSection.PAIN_AND_ENDOMETRIOSIS))
        assertEquals(
            listOf(ReportSection.CYCLES_HISTORY, ReportSection.PAIN_AND_ENDOMETRIOSIS, ReportSection.WEIGHT),
            doc.parts.map { it.section },
        )
    }

    @Test
    fun alertsComeFirstInThePdf() {
        val doc = build(setOf(ReportSection.OVERVIEW, ReportSection.ALERTS), alerts = listOf(AlertKind.SEVERE_PAIN))
        assertEquals(listOf(ReportSection.ALERTS, ReportSection.OVERVIEW), doc.parts.map { it.section })
    }

    @Test
    fun personalDataStaysOutWhenTheSectionIsNotTicked() {
        val doc = build(setOf(ReportSection.OVERVIEW))
        assertTrue(doc.parts.none { it.section == ReportSection.WEIGHT || it.section == ReportSection.NOTES })
        val text = doc.parts.flatMap { it.blocks }.toString()
        assertTrue("Migraine" !in text && "61,5" !in text)
    }

    @Test
    fun emptySectionsAreSkipped() {
        val doc = build(setOf(ReportSection.INTIMACY, ReportSection.MEDICATIONS, ReportSection.ALERTS))
        assertTrue(doc.parts.isEmpty())
    }

    @Test
    fun cyclesTableListsTheMostRecentCycleFirst() {
        val table = build(setOf(ReportSection.CYCLES_HISTORY)).parts.single().blocks.single() as ReportBlock.CycleTable
        assertEquals(listOf("29 jours", "30 jours", "28 jours"), table.rows.map { it[1] })
        assertEquals("-", table.rows.first()[2])
        assertEquals("2 jours", table.rows[1][3])
        assertTrue(table.summary!!.contains("de 28 à 30 jours"))
    }

    @Test
    fun painCountsMatchTheLogs() {
        val bars = (build(setOf(ReportSection.PAIN_AND_ENDOMETRIOSIS)).parts.single().blocks.first() as ReportBlock.Bars).bars
        assertEquals(listOf(2, 1), bars.map { it.days })
        assertEquals("1 jour", bars[1].valueText)
    }

    @Test
    fun currentSituationTilesFollowTheData() {
        val withAll = buildReportDocument(report(spotting = 3), "", logs, setOf(ReportSection.OVERVIEW))
        assertEquals(listOf("14", "27 octobre", "29 jours", "3 jours"), withAll.tiles.map { it.value })
        assertEquals("règles : 5 jours", withAll.tiles[2].detail)
        val minimal = buildReportDocument(report(withOverview = false), "", logs, emptySet())
        assertEquals(2, minimal.tiles.size)
        assertNull(minimal.tiles.last().detail)
    }

    @Test
    fun referencesOnlyListWhatIsUsed() {
        assertTrue(build(setOf(ReportSection.WEIGHT)).references.isEmpty())
        assertEquals(2, build(setOf(ReportSection.OVERVIEW)).references.size)
    }

    // --- Langues : le PDF ne retombe plus sur l'anglais en polonais, ukrainien, espagnol et allemand ---

    private val richLogs = listOf(
        DayLog(
            date = today.minusDays(3),
            pain = Pain.SEVERE,
            symptoms = setOf(Symptom.CRAMPS, Symptom.PELVIC_PAIN),
            weight = 61.5f,
            note = "Migraine",
            pillStatus = PillStatus.TAKEN,
            medications = setOf(MedicationKind.PAINKILLER),
            intimacy = setOf(IntimacyKind.PROTECTED),
            mucus = CervicalMucus.DRY,
            basalTemperature = 36.4f,
        ),
        DayLog(date = today.minusDays(5), pain = Pain.MODERATE, weight = 62.0f, basalTemperature = 36.6f),
    )

    private val allTranslatedSections = ReportSection.entries.toSet() - ReportSection.SYMPTOMS_AND_MOODS - ReportSection.ALERTS

    private fun fullDocument() = buildReportDocument(report(spotting = 3), "", richLogs, allTranslatedSections)

    /** Tous les libellés fixes du bilan, sans les valeurs (nombres, dates, texte libre). */
    private fun labels(doc: ReportDocument): List<String> = buildList {
        add(doc.title)
        add(doc.subtitle)
        add(doc.situationTitle)
        addAll(doc.tiles.map { it.label })
        doc.parts.forEach { part ->
            add(part.title)
            part.blocks.forEach { block ->
                when (block) {
                    // Les notes ont une date pour libellé : ce n'est pas un texte fixe.
                    is ReportBlock.Rows -> if (part.section != ReportSection.NOTES) addAll(block.rows.map { it.first })
                    is ReportBlock.Bars -> addAll(block.bars.map { it.label })
                    is ReportBlock.CycleTable -> addAll(block.headers)
                    is ReportBlock.Paragraph -> add(block.text)
                    else -> Unit
                }
            }
        }
    }

    @Test
    fun everyLanguageGetsItsOwnWordingInsteadOfEnglish() {
        try {
            Locale.setDefault(Locale.ENGLISH)
            val english = fullDocument()
            val englishLabels = labels(english)
            assertTrue(englishLabels.size > 20)
            listOf("pl", "uk", "es", "de").forEach { tag ->
                Locale.setDefault(Locale.forLanguageTag(tag))
                val doc = fullDocument()
                assertEquals(english.parts.map { it.section }, doc.parts.map { it.section }, tag)
                val translated = labels(doc)
                assertEquals(englishLabels.size, translated.size, tag)
                englishLabels.zip(translated).forEach { (en, loc) ->
                    assertNotEquals(en, loc, "[$tag] encore en anglais : $en")
                }
                // Les titres de rubrique et le titre du document sont les premiers repères d'un médecin.
                assertNotEquals(english.title, doc.title, tag)
                english.parts.zip(doc.parts).forEach { (en, loc) -> assertNotEquals(en.title, loc.title, tag) }
            }
        } finally {
            Locale.setDefault(Locale.FRENCH)
        }
    }

    @Test
    fun datesAndDecimalsFollowTheLanguage() {
        val months = mapOf("pl" to "listopada", "uk" to "листопада", "es" to "noviembre", "de" to "9. November")
        try {
            Locale.setDefault(Locale.ENGLISH)
            val english = fullDocument()
            assertTrue("61.5" in english.parts.single { it.section == ReportSection.WEIGHT }.blocks.toString())
            months.forEach { (tag, month) ->
                Locale.setDefault(Locale.forLanguageTag(tag))
                val doc = fullDocument()
                assertTrue(month in doc.generatedLabel, "[$tag] date : ${doc.generatedLabel}")
                val weight = doc.parts.single { it.section == ReportSection.WEIGHT }.blocks.toString()
                assertTrue("61,5" in weight, "[$tag] poids : $weight")
                assertFalse("61.5" in weight, "[$tag] poids : $weight")
            }
        } finally {
            Locale.setDefault(Locale.FRENCH)
        }
    }

    @Test
    fun polishAndUkrainianFallBackToTheSystemFont() {
        // Atkinson Hyperlegible embarquée n'a ni le Latin étendu A (ł, ś, ż...) ni le cyrillique.
        assertTrue(reportNeedsSystemFont(Language.PL))
        assertTrue(reportNeedsSystemFont(Language.UK))
        listOf(Language.FR, Language.EN, Language.ES, Language.DE).forEach { assertFalse(reportNeedsSystemFont(it), it.name) }
    }

    @Test
    fun germanKeepsItsCapitalisedNounsInPhaseSentences() {
        try {
            Locale.setDefault(Locale.GERMAN)
            assertEquals("Krämpfe", ReportText.inline("Krämpfe"))
            Locale.setDefault(Locale.ENGLISH)
            assertEquals("cramps", ReportText.inline("Cramps"))
        } finally {
            Locale.setDefault(Locale.FRENCH)
        }
    }
}
