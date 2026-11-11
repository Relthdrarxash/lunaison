package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import fr.lunaison.core.model.minusDays
import fr.lunaison.core.model.of
import fr.lunaison.core.model.plusDays
import fr.lunaison.core.model.daysTo

private val START: LocalDate = LocalDate.of(2026, 1, 5)

/** Cycles de durées [lengths], règles de 5 jours, avec [extra] ajouté à chaque cycle selon son début. */
private fun cycles(vararg lengths: Int, pain: Pain? = null, extra: (cycleStart: LocalDate, nextStart: LocalDate) -> List<DayLog> = { _, _ -> emptyList() }): List<DayLog> {
    val logs = mutableListOf<DayLog>()
    var start = START
    for (length in lengths) {
        val next = start.plusDays(length.toLong())
        logs += (0 until 5).map { DayLog(start.plusDays(it.toLong()), flow = Flow.MEDIUM, pain = if (it == 0) pain else null) }
        logs += extra(start, next)
        start = next
    }
    logs += (0 until 5).map { DayLog(start.plusDays(it.toLong()), flow = Flow.MEDIUM) }
    return logs
}

private fun lastStart(vararg lengths: Int): LocalDate = START.plusDays(lengths.sum().toLong())

private fun state(logs: List<DayLog>, today: LocalDate): CycleState.Known =
    assertIs<CycleState.Known>(CycleAnalyzer.analyze(logs, null, today))

class PhasePatternGuardsTest {

    @Test
    fun `un symptome present tout le cycle n'est pas lie a une phase`() {
        // Fatigue notée un jour sur deux, partout ; maux de tête seulement juste avant les règles.
        val logs = cycles(28, 28, 28) { start, next ->
            (6 until 28 step 2).map { DayLog(start.plusDays(it.toLong()), symptoms = setOf(Symptom.FATIGUE)) } +
                listOf(DayLog(next.minusDays(2), symptoms = setOf(Symptom.HEADACHE, Symptom.FATIGUE)))
        }
        val pattern = PhasePatterns.compute(logs, state(logs, lastStart(28, 28, 28).plusDays(10)), Phase.LUTEAL)
        assertEquals(listOf(Symptom.HEADACHE), pattern.symptoms.map { it.first })
    }

    @Test
    fun `il faut au moins la moitie des cycles renseignes`() {
        // Seins sensibles deux cycles sur six : pas une tendance.
        val logs = cycles(28, 28, 28, 28, 28, 28) { start, next ->
            val index = START.daysTo(start) / 28
            listOf(DayLog(next.minusDays(3), symptoms = if (index < 2) setOf(Symptom.BREAST_TENDERNESS) else setOf(Symptom.ACNE)))
        }
        val pattern = PhasePatterns.compute(logs, state(logs, lastStart(28, 28, 28, 28, 28, 28).plusDays(10)), Phase.LUTEAL)
        assertEquals(6, pattern.cycles)
        assertFalse(pattern.symptoms.any { it.first == Symptom.BREAST_TENDERNESS })
        assertTrue(pattern.symptoms.any { it.first == Symptom.ACNE })
    }

    @Test
    fun `les jours sans saisie ne comptent pas comme des jours sans symptome`() {
        // Deux cycles renseignés en fin de cycle seulement : la phase compte 2 cycles, pas 3.
        val logs = cycles(28, 28, 28) { start, next ->
            if (start == START) emptyList() else listOf(DayLog(next.minusDays(2), moods = setOf(Mood.IRRITABLE)))
        }
        val pattern = PhasePatterns.compute(logs, state(logs, lastStart(28, 28, 28).plusDays(10)), Phase.LUTEAL)
        assertEquals(2, pattern.cycles)
        assertEquals(listOf(Mood.IRRITABLE to 2), pattern.moods)
    }
}

class CycleInsightsTest {

    @Test
    fun `rien avant deux cycles complets`() {
        val logs = cycles(28)
        assertNull(CycleInsights.overview(logs, state(logs, lastStart(28).plusDays(3)), AgeGroup.ADULT))
    }

    @Test
    fun `vue d'ensemble des cycles et ecart des previsions`() {
        val lengths = intArrayOf(28, 30, 27, 29, 31)
        val logs = cycles(*lengths)
        val overview = assertNotNull(CycleInsights.overview(logs, state(logs, lastStart(*lengths).plusDays(3)), AgeGroup.ADULT))
        assertEquals(5, overview.records.size)
        assertEquals(29, overview.usualLength)
        assertEquals(27, overview.shortest)
        assertEquals(31, overview.longest)
        assertEquals(5, overview.usualPeriodLength)
        assertTrue(overview.withinReferences)
        // Prévisions vérifiables à partir du troisième cycle : 27 contre 29, 29 contre 28, 31 contre 29.
        assertEquals(3, overview.predictionsChecked)
        assertEquals(2, overview.typicalPredictionError)
    }

    @Test
    fun `des cycles tres variables sortent des reperes chez l'adulte seulement`() {
        val lengths = intArrayOf(25, 37, 26)
        val logs = cycles(*lengths)
        val s = state(logs, lastStart(*lengths).plusDays(3))
        assertFalse(assertNotNull(CycleInsights.overview(logs, s, AgeGroup.ADULT)).withinReferences)
        assertTrue(assertNotNull(CycleInsights.overview(logs, s, AgeGroup.UNDER_18)).withinReferences)
    }

    @Test
    fun `bilan du cycle termine pendant les premiers jours seulement`() {
        val lengths = intArrayOf(28, 28, 32)
        val logs = cycles(*lengths, pain = Pain.SEVERE)
        val recap = assertNotNull(CycleInsights.recap(logs, state(logs, lastStart(*lengths).plusDays(2)), AgeGroup.ADULT))
        assertEquals(32, recap.record.length)
        assertEquals(28, recap.usualLength)
        assertEquals(4, recap.difference)
        assertTrue(recap.withinReferences)
        assertEquals(1, recap.record.painDays)
        assertEquals(1, recap.usualPainDays)
        assertNull(CycleInsights.recap(logs, state(logs, lastStart(*lengths).plusDays(CycleConfig.RECAP_DAYS.toLong())), AgeGroup.ADULT))
    }

    @Test
    fun `annonce de la phase suivante quand un motif y revient`() {
        val lengths = intArrayOf(28, 28, 28)
        val logs = cycles(*lengths) { _, next ->
            listOf(DayLog(next.minusDays(3), symptoms = setOf(Symptom.HEADACHE)), DayLog(next.minusDays(2), symptoms = setOf(Symptom.HEADACHE)))
        }
        // Prochaines règles prévues 28 jours après le dernier début ; phase lutéale à partir de J-10.
        val today = lastStart(*lengths).plusDays(16)
        val anticipation = assertNotNull(CycleInsights.anticipation(logs, state(logs, today)))
        assertEquals(Phase.LUTEAL, anticipation.phase)
        assertEquals(2, anticipation.inDays)
        assertEquals(listOf("Maux de tête"), anticipation.pattern.labels)
        // Sans motif dans la phase suivante, rien.
        assertNull(CycleInsights.anticipation(cycles(*lengths), state(cycles(*lengths), today)))
    }

    @Test
    fun `apparition d'un symptome comptee a rebours depuis les regles`() {
        val lengths = intArrayOf(26, 30, 28)
        val logs = cycles(*lengths) { _, next ->
            listOf(DayLog(next.minusDays(3), symptoms = setOf(Symptom.BREAST_TENDERNESS)), DayLog(next.minusDays(1), symptoms = setOf(Symptom.BREAST_TENDERNESS)))
        }
        val onsets = CycleInsights.onsets(logs, state(logs, lastStart(*lengths).plusDays(5)))
        assertEquals(listOf(Onset("Seins sensibles", 3, 3)), onsets)
    }

    @Test
    fun `bilan de sante complet et motifs sur toutes les phases`() {
        val lengths = intArrayOf(28, 28, 28)
        val logs = cycles(*lengths) { start, next ->
            listOf(
                DayLog(start.plusDays(8), moods = setOf(Mood.ENERGETIC), medicationTaken = true),
                DayLog(next.minusDays(2), symptoms = setOf(Symptom.BREAST_TENDERNESS)),
            )
        }
        val s = state(logs, lastStart(*lengths).plusDays(5))
        val allPatterns = CycleInsights.allPhasePatterns(logs, s)
        assertTrue(allPatterns.any { it.phase == Phase.FOLLICULAR })
        assertTrue(allPatterns.any { it.phase == Phase.LUTEAL })
        val report = CycleInsights.healthReport(logs, s, AgeGroup.ADULT)
        assertNotNull(report.overview)
        assertEquals(6, report.dayOfCycle)
        assertEquals(1, report.medicationDaysLast30Days)
    }
}
