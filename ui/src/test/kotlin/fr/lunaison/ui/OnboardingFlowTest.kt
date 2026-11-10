package fr.lunaison.ui

import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import fr.lunaison.core.model.minusDays
import fr.lunaison.core.model.plusDays
import fr.lunaison.core.model.of

class OnboardingFlowTest {

    @Test
    fun hasAtMostFiveScreens() {
        assertEquals(5, OnboardingStep.entries.size)
    }

    @Test
    fun showsOnlyBeforeTheFirstFinish() {
        assertTrue(OnboardingFlow.shouldShow(onboarded = false))
        // Mise à jour de l'appli : le réglage est conservé, l'accueil ne revient pas.
        assertFalse(OnboardingFlow.shouldShow(onboarded = true))
    }

    @Test
    fun walksThroughEveryScreenInOrder() {
        val path = generateSequence(OnboardingStep.WELCOME) { OnboardingFlow.next(it, hasHistory = false) }.toList()
        assertEquals(OnboardingStep.entries.toList(), path)
        assertNull(OnboardingFlow.next(OnboardingStep.REMINDERS, hasHistory = false))
    }

    @Test
    fun skipsTheCycleScreenWhenAHistoryIsAlreadyThere() {
        assertEquals(OnboardingStep.LOCK, OnboardingFlow.next(OnboardingStep.AGE, hasHistory = true))
        assertEquals(OnboardingStep.AGE, OnboardingFlow.previous(OnboardingStep.LOCK, hasHistory = true))
        assertEquals(OnboardingStep.CYCLE, OnboardingFlow.next(OnboardingStep.AGE, hasHistory = false))
        assertEquals(OnboardingStep.CYCLE, OnboardingFlow.previous(OnboardingStep.LOCK, hasHistory = false))
    }

    @Test
    fun backStopsAtTheFirstScreen() {
        assertNull(OnboardingFlow.previous(OnboardingStep.WELCOME, hasHistory = false))
        assertEquals(OnboardingStep.WELCOME, OnboardingFlow.previous(OnboardingStep.AGE, hasHistory = false))
    }

    @Test
    fun skipIsOfferedOnlyOnceTheAgeIsKnown() {
        assertFalse(OnboardingFlow.canSkip(OnboardingStep.WELCOME, ageAnswered = false))
        assertFalse(OnboardingFlow.canSkip(OnboardingStep.AGE, ageAnswered = false))
        assertTrue(OnboardingFlow.canSkip(OnboardingStep.AGE, ageAnswered = true))
        listOf(OnboardingStep.CYCLE, OnboardingStep.LOCK, OnboardingStep.REMINDERS).forEach {
            assertTrue(OnboardingFlow.canSkip(it, ageAnswered = true), it.name)
        }
    }

    @Test
    fun defaultsMatchTheEngine() {
        assertEquals(CycleConfig.DEFAULT_CYCLE_LENGTH, OnboardingFlow.DEFAULT_CYCLE_LENGTH)
        assertEquals(CycleConfig.DEFAULT_PERIOD_LENGTH, OnboardingFlow.DEFAULT_PERIOD_LENGTH)
        assertEquals(28, OnboardingFlow.DEFAULT_CYCLE_LENGTH)
        assertEquals(5, OnboardingFlow.DEFAULT_PERIOD_LENGTH)
    }

    @Test
    fun steppersStayInRange() {
        assertEquals(OnboardingFlow.MIN_CYCLE_LENGTH, OnboardingFlow.clampCycleLength(3))
        assertEquals(OnboardingFlow.MAX_CYCLE_LENGTH, OnboardingFlow.clampCycleLength(500))
        assertEquals(30, OnboardingFlow.clampCycleLength(30))
        assertEquals(OnboardingFlow.MIN_PERIOD_LENGTH, OnboardingFlow.clampPeriodLength(0))
        assertEquals(OnboardingFlow.MAX_PERIOD_LENGTH, OnboardingFlow.clampPeriodLength(40))
        assertEquals(5, OnboardingFlow.clampPeriodLength(5))
        // Les bornes de l'écran restent dans ce que le moteur accepte.
        assertTrue(OnboardingFlow.MIN_CYCLE_LENGTH >= CycleConfig.MIN_PLAUSIBLE_CYCLE)
        assertTrue(OnboardingFlow.MAX_CYCLE_LENGTH <= CycleConfig.MAX_PLAUSIBLE_CYCLE)
    }

    @Test
    fun lastPeriodIsNeitherInTheFutureNorTooOld() {
        val today = LocalDate.of(2026, 11, 10)
        assertTrue(OnboardingFlow.isSelectableLastPeriod(today, today))
        assertTrue(OnboardingFlow.isSelectableLastPeriod(today.minusDays(119), today))
        assertFalse(OnboardingFlow.isSelectableLastPeriod(today.minusDays(120), today))
        assertFalse(OnboardingFlow.isSelectableLastPeriod(today.plusDays(1), today))
    }

    @Test
    fun skippedAnswersLeaveTheDefaultsUntouched() {
        val result = OnboardingFlow.finalBaseline(Baseline(AgeGroup.ADULT), null, null, null, null)
        assertEquals(Baseline(AgeGroup.ADULT), result)
        assertNull(result.lastPeriodStart)
        assertNull(result.usualCycleLength)
        assertNull(result.usualPeriodLength)
    }

    @Test
    fun answersAreRecorded() {
        val start = LocalDate.of(2026, 11, 3)
        val result = OnboardingFlow.finalBaseline(Baseline(AgeGroup.ADULT), AgeGroup.UNDER_18, start, 30, 6)
        assertEquals(Baseline(AgeGroup.UNDER_18, start, 30, 6), result)
    }

    @Test
    fun existingValuesSurviveAReplayOrAnImport() {
        val known = Baseline(AgeGroup.UNDER_18, LocalDate.of(2026, 10, 6), 29, 4)
        // Rouvrir l'accueil sans rien répondre ne remplace rien.
        assertEquals(known, OnboardingFlow.finalBaseline(known, null, null, null, null))
        // Une réponse remplace seulement ce qu'elle concerne.
        val updated = OnboardingFlow.finalBaseline(known, AgeGroup.ADULT, null, 31, null)
        assertEquals(Baseline(AgeGroup.ADULT, LocalDate.of(2026, 10, 6), 31, 4), updated)
    }
}
