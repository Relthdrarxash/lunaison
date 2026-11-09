package fr.lunaison.ui

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecord
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import java.time.LocalDate
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
}
