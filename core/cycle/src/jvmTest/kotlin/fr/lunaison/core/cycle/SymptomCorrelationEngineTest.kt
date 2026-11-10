package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import fr.lunaison.core.model.of
import fr.lunaison.core.model.plusDays

class SymptomCorrelationEngineTest {

    private val baseDate = LocalDate.of(2026, 1, 1)

    @Test
    fun returnsEmptyWhenInsufficientDataOrNoRepetition() {
        val logs = listOf(
            DayLog(date = baseDate, symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(1), symptoms = setOf(Symptom.PELVIC_PAIN)),
            DayLog(date = baseDate.plusDays(5), pain = Pain.NONE),
            DayLog(date = baseDate.plusDays(10), pain = Pain.NONE),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        assertTrue(correlations.isEmpty(), "Une seule occurrence isolée ne doit pas créer de fausse corrélation")
    }

    @Test
    fun rejectsConsecutiveDaysInSameCycleAsSeparateOccurrences() {
        // Dans un seul et même cycle : ballonnement le 1er janvier, douleur le 2 janvier (lag 1)
        // et douleur le 3 janvier (lag 2 par rapport au 1er janvier)
        // Ceci ne doit compter que pour 1 seul épisode, pas 2 !
        val logs = listOf(
            DayLog(date = baseDate, symptoms = setOf(Symptom.BLOATING)),
            DayLog(date = baseDate.plusDays(1), symptoms = setOf(Symptom.PELVIC_PAIN)),
            DayLog(date = baseDate.plusDays(2), symptoms = setOf(Symptom.PELVIC_PAIN)),
            DayLog(date = baseDate.plusDays(10), pain = Pain.NONE),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        assertTrue(correlations.isEmpty(), "Deux jours consécutifs d'un même cycle ne doivent pas être comptés comme 2 cycles distincts")
    }

    @Test
    fun doesNotAttributeHeadacheToPelvicPainEvenIfSevere() {
        // Migraine sévère isolée répétée sans composante pelvienne
        val logs = listOf(
            DayLog(date = baseDate, symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(1), symptoms = setOf(Symptom.HEADACHE), pain = Pain.SEVERE),
            DayLog(date = baseDate.plusDays(28), symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(29), symptoms = setOf(Symptom.HEADACHE), pain = Pain.SEVERE),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        assertTrue(correlations.isEmpty(), "Une céphalée sévère ne doit pas être assimilée arbitrairement à de la douleur pelvienne")
    }

    @Test
    fun detectsRepeatedLagOneCorrelationAcrossDistinctCycles() {
        // Cycle 1 : Troubles digestifs le 1er janvier, Douleurs pelviennes le 2 janvier (lag 1)
        // Cycle 2 : Troubles digestifs le 29 janvier, Douleurs pelviennes le 30 janvier (lag 1, >15j d'écart)
        val logs = listOf(
            // Cycle 1
            DayLog(date = baseDate, symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(1), symptoms = setOf(Symptom.PELVIC_PAIN), pain = Pain.MODERATE),

            // Cycle 2
            DayLog(date = baseDate.plusDays(28), symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(29), symptoms = setOf(Symptom.PELVIC_PAIN), pain = Pain.MODERATE),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        assertEquals(1, correlations.size)

        val correlation = correlations.first()
        assertEquals(Symptom.DIGESTIVE, correlation.precursor)
        assertEquals(Symptom.PELVIC_PAIN, correlation.target)
        assertEquals(1, correlation.lagDays)
        assertEquals(2, correlation.occurrences)

        // Vérification de la tonalité protectrice et bienveillante
        assertTrue(correlation.caringNote.contains("la veille de vos épisodes"))
        assertTrue(correlation.caringNote.contains("temps calme") || correlation.caringNote.contains("chaleur"))
        assertFalse(correlation.caringNote.contains("Alerte"), "Ne doit jamais contenir de mot anxiogène comme 'Alerte'")
    }

    @Test
    fun detectsSameDayCoOccurrenceAcrossDistinctCycles() {
        // Ventre gonflé le même jour que des crampes (lag 0) répété sur deux cycles distincts (> 15 jours)
        val logs = listOf(
            DayLog(date = baseDate, symptoms = setOf(Symptom.BLOATING, Symptom.CRAMPS)),
            DayLog(date = baseDate.plusDays(1), pain = Pain.MILD),
            DayLog(date = baseDate.plusDays(28), symptoms = setOf(Symptom.BLOATING, Symptom.CRAMPS)),
            DayLog(date = baseDate.plusDays(29), pain = Pain.NONE),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        val bloatingCramps = correlations.firstOrNull {
            it.precursor == Symptom.BLOATING && it.target == Symptom.CRAMPS && it.lagDays == 0
        }

        assertTrue(bloatingCramps != null, "Doit détecter la co-occurrence le même jour sur cycles distincts")
        assertEquals(2, bloatingCramps.occurrences)
        assertTrue(bloatingCramps.caringNote.contains("le même jour"))
    }
}
