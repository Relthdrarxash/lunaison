package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PartnerPeriod
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.Symptom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val D0: LocalDate = LocalDate.of(2026, 1, 5)

/** Règles de [length] jours commençant [offset] jours après D0. */
private fun period(offset: Int, length: Int = 5, flow: Flow = Flow.MEDIUM, pain: Pain? = null): List<DayLog> =
    (0 until length).map { DayLog(D0.plusDays((offset + it).toLong()), flow = flow, pain = pain) }

/** Règles régulières : chaque élément de [lengths] est la durée d'un cycle. */
private fun history(vararg lengths: Int): List<DayLog> {
    var offset = 0
    val logs = mutableListOf<DayLog>()
    for (length in lengths) {
        logs += period(offset)
        offset += length
    }
    logs += period(offset)
    return logs
}

private fun known(state: CycleState): CycleState.Known = assertIs<CycleState.Known>(state)

class PeriodDetectorTest {

    @Test
    fun `des jours proches forment les memes regles malgre un oubli`() {
        val logs = period(0, 2) + period(3, 2)
        val detection = PeriodDetector.detect(logs)
        assertEquals(1, detection.periods.size)
        assertEquals(5, detection.periods.single().length)
    }

    @Test
    fun `les traces avant les regles ne decalent pas le premier jour`() {
        val logs = listOf(DayLog(D0, flow = Flow.SPOTTING)) + period(1, 4)
        val period = PeriodDetector.detect(logs).periods.single()
        assertEquals(D0.plusDays(1), period.start)
    }

    @Test
    fun `des traces isolees ne sont pas des regles`() {
        val logs = period(0) + DayLog(D0.plusDays(14), flow = Flow.SPOTTING)
        val detection = PeriodDetector.detect(logs)
        assertEquals(1, detection.periods.size)
        assertEquals(listOf(D0.plusDays(14)), detection.spotting)
    }
}

class CycleAnalyzerTest {

    @Test
    fun `sans rien de note, l'etat est inconnu`() {
        assertEquals(CycleState.Unknown, CycleAnalyzer.analyze(emptyList(), null, D0))
    }

    @Test
    fun `la date declaree au demarrage suffit pour une premiere prevision`() {
        val baseline = Baseline(AgeGroup.ADULT, lastPeriodStart = D0, usualCycleLength = 30)
        val state = known(CycleAnalyzer.analyze(emptyList(), baseline, D0.plusDays(10)))
        assertEquals(D0.plusDays(30), state.next.start)
        assertEquals(Confidence.PROVISIONAL, state.stats.confidence)
        assertEquals(CycleConfig.PROVISIONAL_SPREAD, state.stats.spread)
        assertEquals(11, state.dayOfCycle)
    }

    @Test
    fun `la prevision suit la mediane des derniers cycles`() {
        val logs = history(27, 29, 28, 35, 28)
        val lastStart = D0.plusDays(27L + 29 + 28 + 35 + 28)
        val state = known(CycleAnalyzer.analyze(logs, null, lastStart.plusDays(10)))
        assertEquals(28, state.stats.cycleLength)
        assertEquals(lastStart.plusDays(28), state.next.start)
        assertEquals(Confidence.GOOD, state.stats.confidence)
    }

    @Test
    fun `la marge reste dans les bornes`() {
        val regular = known(CycleAnalyzer.analyze(history(28, 28, 28, 28), null, D0.plusDays(120)))
        assertEquals(CycleConfig.MIN_SPREAD, regular.stats.spread)
        val chaotic = known(CycleAnalyzer.analyze(history(22, 45, 25, 60, 24), null, D0.plusDays(190)))
        assertEquals(CycleConfig.MAX_SPREAD, chaotic.stats.spread)
    }

    @Test
    fun `un cycle invraisemblable est ignore pour la prevision`() {
        val state = known(CycleAnalyzer.analyze(history(28, 28, 140), null, D0.plusDays(200)))
        assertEquals(28, state.stats.cycleLength)
    }

    @Test
    fun `phases d'un cycle de 28 jours`() {
        val logs = history(28, 28, 28)
        val start = D0.plusDays(84)
        fun phaseOn(day: Int) = known(CycleAnalyzer.analyze(logs, null, start.plusDays(day - 1L))).phase
        assertEquals(Phase.MENSTRUAL, phaseOn(2))
        assertEquals(Phase.FOLLICULAR, phaseOn(8))
        // Ovulation estimée 13 jours avant les règles suivantes, soit le 16e jour.
        assertEquals(Phase.OVULATION, phaseOn(16))
        assertEquals(Phase.LUTEAL, phaseOn(24))
    }

    @Test
    fun `retard au-dela de la plage estimee`() {
        val logs = history(28, 28, 28)
        val start = D0.plusDays(84)
        val state = known(CycleAnalyzer.analyze(logs, null, start.plusDays(33)))
        assertEquals(Phase.LATE, state.phase)
        assertEquals(5, state.daysLate)
        assertTrue(state.possiblePregnancyHint)
    }

    @Test
    fun `fenetre fertile et date d'ovulation estimees`() {
        val logs = history(28, 28, 28)
        val start = D0.plusDays(84)
        val state = known(CycleAnalyzer.analyze(logs, null, start.plusDays(15)))
        assertEquals(start.plusDays(15), state.ovulationDate)
        assertTrue(state.inFertileWindow)
    }

    @Test
    fun `les regles en cours sont reconnues meme si aujourd'hui n'est pas encore note`() {
        val logs = history(28) + period(56, 2)
        val state = known(CycleAnalyzer.analyze(logs, null, D0.plusDays(58)))
        assertTrue(state.inPeriod)
        assertEquals(Phase.MENSTRUAL, state.phase)
    }
}

class CycleAlertsTest {

    private fun alerts(logs: List<DayLog>, today: LocalDate, ageGroup: AgeGroup = AgeGroup.ADULT) =
        CycleAlerts.evaluate(known(CycleAnalyzer.analyze(logs, null, today)), ageGroup)

    @Test
    fun `un historique regulier ne declenche rien`() {
        assertEquals(emptyList(), alerts(history(28, 29, 27, 28), D0.plusDays(120)))
    }

    @Test
    fun `douleurs fortes sur deux regles`() {
        val logs = period(0, pain = Pain.SEVERE) + period(28, pain = Pain.SEVERE) + period(56)
        assertTrue(AlertKind.SEVERE_PAIN in alerts(logs, D0.plusDays(70)))
    }

    @Test
    fun `regles de plus de huit jours`() {
        val logs = period(0, 9) + period(28)
        assertTrue(AlertKind.LONG_PERIOD in alerts(logs, D0.plusDays(40)))
    }

    @Test
    fun `cycles courts repetes chez l'adulte mais pas a l'adolescence`() {
        val logs = history(22, 22, 22)
        assertTrue(AlertKind.CYCLE_LENGTH in alerts(logs, D0.plusDays(70)))
        assertFalse(AlertKind.CYCLE_LENGTH in alerts(logs, D0.plusDays(70), AgeGroup.UNDER_18))
    }

    @Test
    fun `cycles tres variables chez l'adulte seulement`() {
        val logs = history(24, 36, 26, 37)
        assertTrue(AlertKind.IRREGULAR in alerts(logs, D0.plusDays(130)))
        assertFalse(AlertKind.IRREGULAR in alerts(logs, D0.plusDays(130), AgeGroup.UNDER_18))
    }

    @Test
    fun `plus de quatre-vingt-dix jours sans regles`() {
        assertTrue(AlertKind.NO_PERIOD in alerts(period(0), D0.plusDays(95)))
    }

    @Test
    fun `traces repetees entre les regles`() {
        val logs = history(28, 28) + DayLog(D0.plusDays(14), flow = Flow.SPOTTING) + DayLog(D0.plusDays(42), flow = Flow.SPOTTING)
        assertTrue(AlertKind.SPOTTING in alerts(logs, D0.plusDays(60)))
    }
}

class PhasePatternsTest {

    @Test
    fun `un symptome present deux cycles de suite est retenu`() {
        val logs = history(28, 28, 28).toMutableList()
        // Seins sensibles en fin de cycle, deux cycles sur trois.
        logs += DayLog(D0.plusDays(25), symptoms = setOf(Symptom.BREAST_TENDERNESS), moods = setOf(Mood.IRRITABLE))
        logs += DayLog(D0.plusDays(53), symptoms = setOf(Symptom.BREAST_TENDERNESS))
        val state = known(CycleAnalyzer.analyze(logs, null, D0.plusDays(90)))
        val pattern = PhasePatterns.compute(logs, state, Phase.LUTEAL)
        assertEquals(listOf(Symptom.BREAST_TENDERNESS to 2), pattern.symptoms)
        // Une humeur notée une seule fois ne fait pas une tendance.
        assertTrue(pattern.moods.isEmpty())
    }
}

class ReminderPlannerTest {

    private val logs = history(28, 28, 28)
    private val start: LocalDate = D0.plusDays(84)

    @Test
    fun `rappel deux jours avant la date estimee`() {
        val state = CycleAnalyzer.analyze(logs, null, start.plusDays(10))
        val at = ReminderPlanner.nextPeriodReminder(state, ReminderSettings(), LocalDateTime.of(start.plusDays(10), LocalTime.NOON))
        assertEquals(LocalDateTime.of(start.plusDays(26), ReminderPlanner.PERIOD_REMINDER_TIME), at)
    }

    @Test
    fun `pas de rappel de saisie si la journee est remplie`() {
        val state = CycleAnalyzer.analyze(logs, null, start.plusDays(1))
        assertTrue(ReminderPlanner.shouldRemindToLog(state, ReminderSettings(), start.plusDays(1), alreadyLogged = false))
        assertFalse(ReminderPlanner.shouldRemindToLog(state, ReminderSettings(), start.plusDays(1), alreadyLogged = true))
    }

    @Test
    fun `rappel de saisie autour des regles seulement par defaut`() {
        val state = CycleAnalyzer.analyze(logs, null, start.plusDays(12))
        assertFalse(ReminderPlanner.shouldRemindToLog(state, ReminderSettings(), start.plusDays(12), alreadyLogged = false))
    }

    @Test
    fun `rappel quotidien de contraception ou traitement`() {
        val settings = ReminderSettings(medicationReminder = true, medicationReminderTime = LocalTime.of(9, 0))
        val at = ReminderPlanner.nextMedicationReminder(settings, LocalDateTime.of(start, LocalTime.of(8, 0)))
        assertEquals(LocalDateTime.of(start, LocalTime.of(9, 0)), at)
        assertTrue(ReminderPlanner.shouldRemindMedication(settings, alreadyTaken = false))
        assertFalse(ReminderPlanner.shouldRemindMedication(settings, alreadyTaken = true))
    }

    @Test
    fun `encodage et decodage du partage partenaire par QR Code ou SMS`() {
        val snapshot = PartnerSnapshot(
            partnerName = "Camille",
            sharedAt = start,
            periods = listOf(
                PartnerPeriod(D0, 5),
                PartnerPeriod(D0.plusDays(28), 5),
                PartnerPeriod(D0.plusDays(56), 5),
                PartnerPeriod(start, 5),
            ),
            usualCycleLength = 28,
            showFertility = true,
        )
        val encoded = snapshot.encode()
        assertTrue(encoded.startsWith("LUNA1:"))
        val decodedDirect = assertNotNull(PartnerSnapshot.decode(encoded))
        assertEquals(snapshot, decodedDirect)

        val sms = "Voici mon suivi Lunaison : $encoded à coller dans l'application."
        val decodedFromSms = assertNotNull(PartnerSnapshot.decode(sms))
        assertEquals(snapshot, decodedFromSms)

        val partnerState = known(CycleAnalyzer.analyze(decodedFromSms.toDayLogs(), decodedFromSms.toBaseline(), start.plusDays(10)))
        assertEquals(11, partnerState.dayOfCycle)
        assertEquals(start.plusDays(28), partnerState.next.start)

        // Mise à jour par QR Code au cycle suivant : même si le QR contient les derniers cycles déjà connus + le nouveau,
        // mergeWith dédoublonne proprement sans créer de doublon.
        val nextStart = start.plusDays(29)
        val updateQr = PartnerSnapshot(
            partnerName = "Camille",
            sharedAt = nextStart,
            periods = snapshot.periods + PartnerPeriod(nextStart, 5),
            usualCycleLength = 28,
            showFertility = true,
        ).encode()
        val merged = snapshot.mergeWith(assertNotNull(PartnerSnapshot.decode(updateQr)))
        assertEquals(5, merged.periods.size)
        assertEquals(nextStart, merged.periods.last().start)
    }

    @Test
    fun `les probabilites de grossesse et les previsions a 7 jours suivent la fenetre fertile`() {
        val lastStart = D0.plusDays(28L * 4)
        val logs = history(28, 28, 28, 28)
        // Ovulation estimée = 28 - 13 = 15 jours après le début des règles
        val stateAtOvulation = known(CycleAnalyzer.analyze(logs, null, lastStart.plusDays(15)))
        assertEquals(PregnancyOdds.PEAK, stateAtOvulation.todayPregnancyOdds)

        val stateAtFollicular = known(CycleAnalyzer.analyze(logs, null, lastStart.plusDays(7)))
        assertEquals(PregnancyOdds.LOW, stateAtFollicular.todayPregnancyOdds)

        val stateInPeriod = known(CycleAnalyzer.analyze(logs, null, lastStart.plusDays(2)))
        assertEquals(PregnancyOdds.VERY_LOW, stateInPeriod.todayPregnancyOdds)

        val forecasts = stateAtFollicular.upcomingDayForecasts(7)
        assertEquals(7, forecasts.size)
        assertTrue(forecasts.any { it.odds in setOf(PregnancyOdds.MEDIUM, PregnancyOdds.HIGH, PregnancyOdds.PEAK) })
    }
}
