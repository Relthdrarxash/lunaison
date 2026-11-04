package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup

/**
 * Toutes les valeurs de référence du moteur, au même endroit, avec leur source.
 * La liste complète des références est dans documentation/sources.md.
 */
object CycleConfig {

    /** Jusqu'à deux jours sans flux noté restent dans les mêmes règles (oubli de saisie, flux irrégulier). */
    const val MAX_GAP_IN_PERIOD = 3

    /** En dessous, un « cycle » est plutôt un saignement intermédiaire ; au-delà, un oubli de saisie probable. */
    const val MIN_PLAUSIBLE_CYCLE = 15
    const val MAX_PLAUSIBLE_CYCLE = 90

    /** Nombre de cycles récents utilisés pour la prévision. */
    const val CYCLES_FOR_PREDICTION = 6

    /** Valeurs par défaut sans historique ni réponse de la personne. */
    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5

    /**
     * Marge de la prévision, en jours de part et d'autre de la date estimée.
     * Variation moyenne d'un cycle à l'autre chez une même personne : 2,6 jours (Bull et al., 2019).
     */
    const val MIN_SPREAD = 2
    const val MAX_SPREAD = 7
    const val PROVISIONAL_SPREAD = 4

    /**
     * Phase lutéale moyenne de 12,4 jours, assez stable, alors que la phase folliculaire varie beaucoup
     * (Bull et al., 2019). L'ovulation est donc estimée à partir des règles suivantes, pas au 14e jour.
     */
    const val LUTEAL_DAYS = 13
    const val OVULATION_WINDOW = 2

    /** Règles de plus de 8 jours : au-delà de la norme FIGO (Munro et al., 2018). */
    const val MAX_NORMAL_PERIOD = 8

    /** Écart entre le cycle le plus court et le plus long au-delà duquel le cycle est dit irrégulier (FIGO, 2018). */
    const val MAX_NORMAL_VARIATION = 9

    /** Absence de règles à partir de laquelle un avis médical est conseillé (ACOG, 2015). */
    const val LONG_ABSENCE_DAYS = 90

    /** Durée normale d'un cycle : 24 à 38 jours chez l'adulte (FIGO, 2018), 21 à 45 jours à l'adolescence (ACOG, 2015). */
    // ---- Statistiques personnelles (choix de conception, voir documentation/sources.md) ----

    /**
     * Cycles nécessaires avant d'affirmer qu'un symptôme revient ou de résumer ses cycles.
     * Le même seuil que les critères cliniques du syndrome prémenstruel, qui demandent un suivi
     * au jour le jour sur au moins deux cycles.
     */
    const val MIN_CYCLES_FOR_STATS = 2

    /** Un symptôme n'est lié à une phase que s'il y est au moins deux fois plus fréquent qu'en dehors. */
    const val PATTERN_ENRICHMENT = 2.0

    /** En dessous de ce nombre de jours renseignés hors de la phase, la comparaison n'est pas possible. */
    const val PATTERN_MIN_DAYS_OUTSIDE = 5

    /** Prévisions passées nécessaires avant d'annoncer l'écart habituel entre prévision et règles. */
    const val MIN_CHECKED_PREDICTIONS = 3

    /** Écart, en jours, en dessous duquel un cycle est « dans vos habitudes ». */
    const val USUAL_TOLERANCE = 2

    /** Le bilan du cycle terminé reste visible pendant les premiers jours du cycle suivant. */
    const val RECAP_DAYS = 7

    /** Horizon, en jours, de la ligne qui annonce la phase suivante. */
    const val ANTICIPATION_DAYS = 4

    fun normalCycleRange(ageGroup: AgeGroup): IntRange = when (ageGroup) {
        AgeGroup.ADULT -> 24..38
        AgeGroup.UNDER_18 -> 21..45
    }
}
