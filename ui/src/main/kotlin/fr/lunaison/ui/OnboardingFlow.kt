package fr.lunaison.ui

import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import kotlinx.datetime.LocalDate
import fr.lunaison.core.model.isAfter
import fr.lunaison.core.model.minusDays

/** Les cinq écrans de l'accueil, dans l'ordre. */
enum class OnboardingStep { WELCOME, AGE, CYCLE, LOCK, REMINDERS }

/**
 * Logique pure de l'accueil du premier lancement : enchaînement des écrans, valeurs proposées, réglage final.
 * Séparée de l'écran pour être testée sans téléphone.
 */
object OnboardingFlow {

    /** Valeurs affichées au départ des réglages, et employées par le moteur quand la personne répond « je ne sais pas ». */
    const val DEFAULT_CYCLE_LENGTH = CycleConfig.DEFAULT_CYCLE_LENGTH
    const val DEFAULT_PERIOD_LENGTH = CycleConfig.DEFAULT_PERIOD_LENGTH

    const val MIN_CYCLE_LENGTH = 18
    const val MAX_CYCLE_LENGTH = 60
    const val MIN_PERIOD_LENGTH = 2
    const val MAX_PERIOD_LENGTH = 10

    /** Date des dernières règles proposée : pas plus de 120 jours en arrière. */
    const val MAX_LAST_PERIOD_AGE_DAYS = 120L

    /**
     * L'accueil ne s'affiche qu'une fois : tant que le réglage `onboarded` n'est pas posé.
     * Une mise à jour de l'appli conserve ce réglage, donc les personnes qui ont déjà des données ne le revoient pas.
     */
    fun shouldShow(onboarded: Boolean): Boolean = !onboarded

    /** Écran suivant, ou null après le dernier. L'écran du cycle est sauté quand un historique est déjà là (import). */
    fun next(step: OnboardingStep, hasHistory: Boolean): OnboardingStep? {
        val following = OnboardingStep.entries.getOrNull(step.ordinal + 1) ?: return null
        return if (following == OnboardingStep.CYCLE && hasHistory) next(following, hasHistory) else following
    }

    /** Écran précédent, ou null depuis le premier. */
    fun previous(step: OnboardingStep, hasHistory: Boolean): OnboardingStep? {
        val before = OnboardingStep.entries.getOrNull(step.ordinal - 1) ?: return null
        return if (before == OnboardingStep.CYCLE && hasHistory) previous(before, hasHistory) else before
    }

    /**
     * « Passer » n'est proposé qu'une fois l'âge indiqué : il règle les repères du cycle et l'accès au partage.
     * Le premier écran a son propre bouton « Commencer ».
     */
    fun canSkip(step: OnboardingStep, ageAnswered: Boolean): Boolean = when (step) {
        OnboardingStep.WELCOME -> false
        OnboardingStep.AGE -> ageAnswered
        else -> true
    }

    fun clampCycleLength(days: Int): Int = days.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
    fun clampPeriodLength(days: Int): Int = days.coerceIn(MIN_PERIOD_LENGTH, MAX_PERIOD_LENGTH)

    /** Dernières règles : ni dans le futur, ni plus de [MAX_LAST_PERIOD_AGE_DAYS] jours en arrière. */
    fun isSelectableLastPeriod(date: LocalDate, today: LocalDate): Boolean =
        !date.isAfter(today) && date.isAfter(today.minusDays(MAX_LAST_PERIOD_AGE_DAYS))

    /**
     * Réglage enregistré à la fin. Une réponse « je ne sais pas » ou un écran passé (null) ne remplace jamais
     * ce qui est déjà connu : historique importé, ou réglage d'une personne qui rouvre l'accueil depuis les réglages.
     */
    fun finalBaseline(
        existing: Baseline,
        ageGroup: AgeGroup?,
        lastPeriodStart: LocalDate?,
        usualCycleLength: Int?,
        usualPeriodLength: Int?,
    ): Baseline = Baseline(
        ageGroup = ageGroup ?: existing.ageGroup,
        lastPeriodStart = lastPeriodStart ?: existing.lastPeriodStart,
        usualCycleLength = usualCycleLength ?: existing.usualCycleLength,
        usualPeriodLength = usualPeriodLength ?: existing.usualPeriodLength,
    )
}
