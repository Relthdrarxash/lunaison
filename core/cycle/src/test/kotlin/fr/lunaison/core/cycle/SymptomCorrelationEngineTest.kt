package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymptomCorrelationEngineTest {

    private val baseDate = LocalDate.of(2026, 1, 1)

    @Test
    fun returnsEmptyWhenInsufficientDataOrNoRepetition() {
        // Seulement 2 jours sans répétition
        val logs = listOf(
            DayLog(date = baseDate, symptoms = setOf(Symptom.DIGESTIVE)),
            DayLog(date = baseDate.plusDays(1), symptoms = setOf(Symptom.PELVIC_PAIN)),
        )

        val correlations = SymptomCorrelationEngine.analyze(logs)
        assertTrue(correlations.isEmpty(), "Une seule occurrence ne doit pas créer de fausse corrélation")
    }

    @Test
    fun detectsRepeatedLagOneCorrelationEmpathetically() {
        // Cycle 1 : Troubles digestifs le 1er janvier, Douleurs pelviennes le 2 janvier (lag 1)
        // Cycle 2 : Troubles digestifs le 29 janvier, Douleurs pelviennes le 30 janvier (lag 1)
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

        // Vérification de la tonalité bienveillante et protectrice
        assertTrue(correlation.caringNote.contains("la veille de vos épisodes"))
        assertTrue(correlation.caringNote.contains("temps calme") || correlation.caringNote.contains("chaleur"))
        assertTrue(!correlation.caringNote.contains("Alerte"), "Ne doit jamais contenir de mot anxiogène comme 'Alerte'")
    }

    @Test
    fun detectsSameDayCoOccurrence() {
        // Ventre gonflé le même jour que des crampes (lag 0) répété sur deux épisodes
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

        assertTrue(bloatingCramps != null, "Doit détecter la co-occurrence le même jour")
        assertEquals(2, bloatingCramps.occurrences)
        assertTrue(bloatingCramps.caringNote.contains("le même jour"))
    }
}
