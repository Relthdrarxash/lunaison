package fr.lunaison.core.content

import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.model.tr

/**
 * Messages proposés pour le partage avec un ou une partenaire. La personne choisit, relit et modifie le texte,
 * puis l'envoie elle-même depuis sa messagerie : rien ne part automatiquement.
 */
enum class ShareKind(private val fr: String, private val en: String) {
    PERIOD_STARTED("Mes règles ont commencé", "My period has started"),
    PAIN("J'ai mal aujourd'hui", "I'm in pain today"),
    REST("J'ai besoin de calme", "I need some quiet"),
    HELP("Un coup de main ?", "Can you help?"),
    PERIOD_SOON("Mes règles arrivent bientôt", "My period is coming soon"),
    FEELING_BETTER("Ça va mieux", "Feeling better"),
    ;

    val label: String get() = tr(fr, en)
}

object ShareMessages {

    /** Les modèles proposés aujourd'hui, du plus probable au moins probable. */
    fun suggestions(state: CycleState): List<ShareKind> {
        val known = state as? CycleState.Known
        return when {
            known == null -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED)
            known.inPeriod -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED, ShareKind.FEELING_BETTER)
            known.daysUntilNext in 0..5 || known.daysLate > 0 ->
                listOf(ShareKind.PERIOD_SOON, ShareKind.PERIOD_STARTED, ShareKind.REST, ShareKind.PAIN, ShareKind.HELP)
            else -> listOf(ShareKind.REST, ShareKind.PAIN, ShareKind.HELP, ShareKind.FEELING_BETTER)
        }
    }

    fun text(kind: ShareKind, state: CycleState): String = when (kind) {
        ShareKind.PERIOD_STARTED -> tr(
            "Mes règles ont commencé aujourd'hui. Je risque d'avoir moins d'énergie et d'avoir mal les prochains jours.",
            "My period started today. I may have less energy and be in pain over the next few days.",
        )
        ShareKind.PAIN -> tr(
            "J'ai mal aujourd'hui (règles). Une bouillotte et un peu de calme me feraient du bien.",
            "I'm in pain today (period). A hot water bottle and a bit of quiet would do me good.",
        )
        ShareKind.REST -> tr(
            "J'ai besoin d'une soirée calme aujourd'hui. Rien de grave, c'est le cycle.",
            "I need a quiet evening today. Nothing serious, it's my cycle.",
        )
        ShareKind.HELP -> tr(
            "Je suis à plat aujourd'hui. Tu pourrais prendre le relais pour le repas ou les courses ?",
            "I'm worn out today. Could you take over dinner or the shopping?",
        )
        ShareKind.PERIOD_SOON -> {
            val days = (state as? CycleState.Known)?.daysUntilNext
            when {
                days == null || days <= 0 -> tr("Mes règles devraient arriver d'un jour à l'autre.", "My period should arrive any day now.")
                days == 1 -> tr("Mes règles devraient arriver demain.", "My period should arrive tomorrow.")
                else -> tr("Mes règles devraient arriver dans $days jours environ.", "My period should arrive in about $days days.")
            }
        }
        ShareKind.FEELING_BETTER -> tr("Ça va mieux aujourd'hui, merci d'avoir été là.", "I'm feeling better today, thanks for being there.")
    }
}
