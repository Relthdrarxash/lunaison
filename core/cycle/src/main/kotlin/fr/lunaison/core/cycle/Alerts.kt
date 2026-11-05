package fr.lunaison.core.cycle

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.tr

/**
 * Signaux qui méritent d'en parler à un professionnel de santé. Jamais un diagnostic :
 * l'appli dit ce qu'elle a remarqué et pourquoi ça vaut un avis.
 */
enum class AlertKind(
    private val frTitle: String,
    private val enTitle: String,
    private val frMessage: String,
    private val enMessage: String,
    val articleId: String,
) {
    SEVERE_PAIN(
        "Des douleurs fortes à plusieurs reprises",
        "Severe pain more than once",
        "Vous avez noté des douleurs qui vous empêchent de faire vos activités pendant plusieurs règles. " +
            "Ce n'est pas quelque chose qu'il faut supporter : un médecin, une sage-femme ou un gynécologue peut en chercher la cause, " +
            "par exemple une endométriose, et vous proposer un traitement.",
        "You have logged pain that stops you from doing your activities during several periods. " +
            "This is not something you have to put up with: a doctor, a midwife or a gynecologist can look for the cause, " +
            "such as endometriosis, and offer you treatment.",
        "consulter",
    ),
    LONG_PERIOD(
        "Des règles de plus de 8 jours",
        "Periods lasting more than 8 days",
        "Des règles qui durent plus de 8 jours sortent des repères habituels. Ça vaut la peine d'en parler à un professionnel de santé.",
        "Periods that last more than 8 days are outside the usual range. It is worth talking to a health professional about it.",
        "consulter",
    ),
    HEAVY_FLOW(
        "Des règles abondantes à plusieurs reprises",
        "Heavy periods more than once",
        "Des règles abondantes répétées peuvent fatiguer et faire baisser le fer. Si vous changez de protection en moins de trois heures, " +
            "ou si vous voyez des caillots de plus de 2,5 cm, parlez-en à un professionnel de santé.",
        "Repeated heavy periods can make you tired and lower your iron levels. If you need to change your pad, tampon or cup in less than three hours, " +
            "or if you see clots larger than 2.5 cm (1 inch), talk to a health professional.",
        "consulter",
    ),
    CYCLE_LENGTH(
        "Des cycles hors des repères habituels",
        "Cycles outside the usual range",
        "Vos deux derniers cycles étaient plus courts ou plus longs que la plupart des cycles. Ce n'est pas forcément inquiétant, " +
            "mais si ça continue, un avis médical permet d'en comprendre la raison.",
        "Your last two cycles were shorter or longer than most cycles. This is not necessarily a concern, " +
            "but if it continues, a medical opinion can help understand why.",
        "consulter",
    ),
    IRREGULAR(
        "Des cycles très variables",
        "Very variable cycles",
        "L'écart entre votre cycle le plus court et le plus long dépasse 9 jours. Le stress, un voyage ou une maladie peuvent l'expliquer ; " +
            "si ça dure, parlez-en à un professionnel de santé.",
        "The difference between your shortest and longest cycle is more than 9 days. Stress, travel or illness can explain it; " +
            "if it lasts, talk to a health professional.",
        "consulter",
    ),
    NO_PERIOD(
        "Pas de règles depuis plus de trois mois",
        "No period for more than three months",
        "Aucune règle n'est notée depuis plus de 90 jours. Si vous les avez simplement oubliées, ajoutez-les dans le calendrier. " +
            "Sinon, un avis médical est conseillé.",
        "No period has been logged for more than 90 days. If you simply forgot to log them, add them in the calendar. " +
            "Otherwise, seeing a doctor is advised.",
        "consulter",
    ),
    SPOTTING(
        "Des saignements entre les règles",
        "Bleeding between periods",
        "Vous avez noté plusieurs fois des traces de sang en dehors des règles. C'est souvent bénin, mais ça mérite d'en parler " +
            "à un professionnel de santé, surtout si ça se répète.",
        "You have logged spotting outside your periods several times. It is often harmless, but it is worth talking " +
            "to a health professional, especially if it keeps happening.",
        "consulter",
    ),
    ;

    val title: String get() = tr(frTitle, enTitle)
    val message: String get() = tr(frMessage, enMessage)
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
