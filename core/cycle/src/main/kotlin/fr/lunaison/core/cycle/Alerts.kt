package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup

/**
 * Signaux qui méritent d'en parler à un professionnel de santé. Jamais un diagnostic :
 * l'appli dit ce qu'elle a remarqué et pourquoi ça vaut un avis.
 */
enum class AlertKind(val title: String, val message: String, val articleId: String) {
    SEVERE_PAIN(
        "Des douleurs fortes à plusieurs reprises",
        "Vous avez noté des douleurs qui vous empêchent de faire vos activités pendant plusieurs règles. " +
            "Ce n'est pas quelque chose qu'il faut supporter : un médecin, une sage-femme ou un gynécologue peut en chercher la cause, " +
            "par exemple une endométriose, et vous proposer un traitement.",
        "consulter",
    ),
    LONG_PERIOD(
        "Des règles de plus de 8 jours",
        "Des règles qui durent plus de 8 jours sortent des repères habituels. Ça vaut la peine d'en parler à un professionnel de santé.",
        "consulter",
    ),
    HEAVY_FLOW(
        "Des règles abondantes à plusieurs reprises",
        "Des règles abondantes répétées peuvent fatiguer et faire baisser le fer. Si vous changez de protection en moins de trois heures, " +
            "ou si vous voyez des caillots de plus de 2,5 cm, parlez-en à un professionnel de santé.",
        "consulter",
    ),
    CYCLE_LENGTH(
        "Des cycles hors des repères habituels",
        "Vos deux derniers cycles étaient plus courts ou plus longs que la plupart des cycles. Ce n'est pas forcément inquiétant, " +
            "mais si ça continue, un avis médical permet d'en comprendre la raison.",
        "consulter",
    ),
    IRREGULAR(
        "Des cycles très variables",
        "L'écart entre votre cycle le plus court et le plus long dépasse 9 jours. Le stress, un voyage ou une maladie peuvent l'expliquer ; " +
            "si ça dure, parlez-en à un professionnel de santé.",
        "consulter",
    ),
    NO_PERIOD(
        "Pas de règles depuis plus de trois mois",
        "Aucune règle n'est notée depuis plus de 90 jours. Si vous les avez simplement oubliées, ajoutez-les dans le calendrier. " +
            "Sinon, un avis médical est conseillé.",
        "consulter",
    ),
    SPOTTING(
        "Des saignements entre les règles",
        "Vous avez noté plusieurs fois des traces de sang en dehors des règles. C'est souvent bénin, mais ça mérite d'en parler " +
            "à un professionnel de santé, surtout si ça se répète.",
        "consulter",
    ),
}

object CycleAlerts {

    fun evaluate(state: CycleState.Known, ageGroup: AgeGroup): List<AlertKind> {
        val alerts = mutableListOf<AlertKind>()
        val recentPeriods = state.periods.takeLast(3)

        if (recentPeriods.count { it.hasSeverePain } >= 2) alerts += AlertKind.SEVERE_PAIN
        if (recentPeriods.any { it.length > CycleConfig.MAX_NORMAL_PERIOD }) alerts += AlertKind.LONG_PERIOD
        if (recentPeriods.count { it.heavyDays >= 3 } >= 2) alerts += AlertKind.HEAVY_FLOW

        val plausible = state.cycles.filter { it.isPlausible }
        val range = CycleConfig.normalCycleRange(ageGroup)
        if (plausible.size >= 2 && plausible.takeLast(2).all { it.length !in range }) alerts += AlertKind.CYCLE_LENGTH

        // À l'adolescence, des cycles irréguliers sont habituels les premières années (ACOG, 2015).
        val lastSix = plausible.takeLast(CycleConfig.CYCLES_FOR_PREDICTION).map { it.length }
        if (ageGroup == AgeGroup.ADULT && lastSix.size >= 4 && lastSix.max() - lastSix.min() > CycleConfig.MAX_NORMAL_VARIATION) {
            alerts += AlertKind.IRREGULAR
        }

        if (state.dayOfCycle - 1 > CycleConfig.LONG_ABSENCE_DAYS) alerts += AlertKind.NO_PERIOD

        // Traces isolées depuis le début de l'avant-dernier cycle.
        val since = state.periods.takeLast(3).firstOrNull()?.start
        if (since != null && state.spotting.count { !it.isBefore(since) } >= 2) alerts += AlertKind.SPOTTING

        return alerts
    }
}
