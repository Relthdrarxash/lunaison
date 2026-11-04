package fr.lunaison.core.content

import fr.lunaison.core.cycle.CycleState

/**
 * Messages proposés pour le partage avec un ou une partenaire. La personne choisit, relit et modifie le texte,
 * puis l'envoie elle-même depuis sa messagerie : rien ne part automatiquement.
 */
enum class ShareKind(val label: String) {
    PERIOD_STARTED("Mes règles ont commencé"),
    PAIN("J'ai mal aujourd'hui"),
    REST("J'ai besoin de calme"),
    HELP("Un coup de main ?"),
    PERIOD_SOON("Mes règles arrivent bientôt"),
    FEELING_BETTER("Ça va mieux"),
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
        ShareKind.PERIOD_STARTED -> "Mes règles ont commencé aujourd'hui. Je risque d'avoir moins d'énergie et d'avoir mal les prochains jours."
        ShareKind.PAIN -> "J'ai mal aujourd'hui (règles). Une bouillotte et un peu de calme me feraient du bien."
        ShareKind.REST -> "J'ai besoin d'une soirée calme aujourd'hui. Rien de grave, c'est le cycle."
        ShareKind.HELP -> "Je suis à plat aujourd'hui. Tu pourrais prendre le relais pour le repas ou les courses ?"
        ShareKind.PERIOD_SOON -> {
            val days = (state as? CycleState.Known)?.daysUntilNext
            when {
                days == null || days <= 0 -> "Mes règles devraient arriver d'un jour à l'autre."
                days == 1 -> "Mes règles devraient arriver demain."
                else -> "Mes règles devraient arriver dans $days jours environ."
            }
        }
        ShareKind.FEELING_BETTER -> "Ça va mieux aujourd'hui, merci d'avoir été là."
    }
}
