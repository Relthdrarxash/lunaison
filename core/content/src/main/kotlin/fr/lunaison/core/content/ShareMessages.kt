package fr.lunaison.core.content

import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
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
    PHASE_UPDATE("Expliquer ma phase actuelle", "Explain my current phase"),
    FEELING_BETTER("Ça va mieux", "Feeling better"),
    ;

    val label: String get() = tr(fr, en)
}

/** Repères pour le ou la partenaire : comprendre la phase en cours et savoir comment soutenir concrètement. */
data class PartnerSupportGuide(
    val phaseTitle: String,
    val whatPartnerShouldKnow: String,
    val supportTips: List<String>,
)

object ShareMessages {

    /** Les modèles proposés aujourd'hui, du plus probable au moins probable. */
    fun suggestions(state: CycleState): List<ShareKind> {
        val known = state as? CycleState.Known
        return when {
            known == null -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED)
            known.inPeriod -> listOf(ShareKind.PAIN, ShareKind.REST, ShareKind.HELP, ShareKind.PERIOD_STARTED, ShareKind.PHASE_UPDATE, ShareKind.FEELING_BETTER)
            known.daysUntilNext in 0..5 || known.daysLate > 0 ->
                listOf(ShareKind.PERIOD_SOON, ShareKind.PHASE_UPDATE, ShareKind.PERIOD_STARTED, ShareKind.REST, ShareKind.PAIN, ShareKind.HELP)
            else -> listOf(ShareKind.PHASE_UPDATE, ShareKind.REST, ShareKind.PAIN, ShareKind.HELP, ShareKind.FEELING_BETTER)
        }
    }

    fun partnerGuide(phase: Phase): PartnerSupportGuide = when (phase) {
        Phase.MENSTRUAL -> PartnerSupportGuide(
            phaseTitle = tr("Pendant les règles", "During the period"),
            whatPartnerShouldKnow = tr(
                "Les hormones sont au plus bas et l'utérus se contracte. La fatigue et les crampes peuvent être intenses les premiers jours.",
                "Hormones are at their lowest and the uterus contracts. Fatigue and cramps can be intense in the first days.",
            ),
            supportTips = listOf(
                tr("Préparer une bouillotte chaude ou vérifier qu'il reste un antalgique adapté.", "Prepare a hot water bottle or check that suitable pain relief is on hand."),
                tr("Prendre spontanément le relais sur les repas, les courses ou les tâches lourdes.", "Proactively take over meals, grocery shopping or heavy chores."),
                tr("Proposer une soirée calme sans pression sociale si l'énergie est basse.", "Offer a quiet evening without social pressure if energy is low."),
            ),
        )
        Phase.FOLLICULAR -> PartnerSupportGuide(
            phaseTitle = tr("Phase folliculaire", "Follicular phase"),
            whatPartnerShouldKnow = tr(
                "Après les règles, les œstrogènes remontent progressivement. L'énergie et l'élan reviennent souvent pendant cette période.",
                "After the period, estrogen gradually rises. Energy and drive often return during this time.",
            ),
            supportTips = listOf(
                tr("Bon moment pour planifier des sorties, du sport ou des projets à deux.", "A good time to plan outings, exercise or projects together."),
                tr("Après des règles abondantes, privilégier des repas riches en fer et vitamine C.", "After heavy periods, favor meals rich in iron and vitamin C."),
                tr("Rester à l'écoute : chaque cycle est différent et la fatigue peut parfois persister.", "Stay attentive: every cycle is different and tiredness can sometimes linger."),
            ),
        )
        Phase.OVULATION -> PartnerSupportGuide(
            phaseTitle = tr("Autour de l'ovulation", "Around ovulation"),
            whatPartnerShouldKnow = tr(
                "Les œstrogènes atteignent leur pic au milieu du cycle. Une gêne passagère d'un côté du bas-ventre est possible.",
                "Estrogen reaches its peak mid-cycle. Mild temporary discomfort on one side of the lower belly is possible.",
            ),
            supportTips = listOf(
                tr("C'est la fenêtre fertile du cycle : utile à savoir selon vos projets communs.", "This is the fertile window of the cycle: useful to know depending on your shared plans."),
                tr("Si une douleur pelvienne ou un mal de tête survient, un peu de repos aide.", "If pelvic discomfort or a headache occurs, a bit of rest helps."),
                tr("Rappel : l'estimation par calendrier ne protège pas d'une grossesse.", "Reminder: calendar estimates do not protect against pregnancy."),
            ),
        )
        Phase.LUTEAL -> PartnerSupportGuide(
            phaseTitle = tr("Phase lutéale (avant les règles)", "Luteal phase (before the period)"),
            whatPartnerShouldKnow = tr(
                "La progestérone domine puis chute avant les règles. Fatigue, seins sensibles, sommeil perturbé ou irritabilité (SPM) sont fréquents en fin de cycle.",
                "Progesterone dominates and then drops before the period. Fatigue, tender breasts, poor sleep or irritability (PMS) are common late in the cycle.",
            ),
            supportTips = listOf(
                tr("Alléger la charge mentale du quotidien sans attendre qu'on vous le demande.", "Lighten the daily mental load without waiting to be asked."),
                tr("Faire preuve de patience et de douceur si la sensibilité ou la fatigue augmente.", "Show patience and gentleness if sensitivity or tiredness increases."),
                tr("Éviter de minimiser ce qui est ressenti (« c'est juste tes règles »).", "Avoid dismissing feelings (\"it's just your period\")."),
            ),
        )
        Phase.LATE -> PartnerSupportGuide(
            phaseTitle = tr("Règles en retard", "Period late"),
            whatPartnerShouldKnow = tr(
                "Les règles dépassent la plage attendue. Cela peut venir du stress, d'un changement de rythme ou d'une grossesse.",
                "The period is past the expected range. This can come from stress, a change in routine or pregnancy.",
            ),
            supportTips = listOf(
                tr("Être présent et rassurant, sans ajouter de stress ni de questions insistantes.", "Be present and reassuring, without adding stress or insistent questions."),
                tr("Proposer d'aller chercher un test en pharmacie si vous le souhaitez tous les deux.", "Offer to pick up a test at the pharmacy if you both want to."),
                tr("Laisser de l'espace et du calme en attendant la suite.", "Allow space and calm while waiting to see what happens next."),
            ),
        )
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
        ShareKind.PHASE_UPDATE -> {
            val phase = (state as? CycleState.Known)?.phase ?: Phase.MENSTRUAL
            val guide = partnerGuide(phase)
            tr(
                "Petit point cycle (${guide.phaseTitle.lowercase()}) : ${guide.whatPartnerShouldKnow} Ce qui m'aiderait en ce moment : ${guide.supportTips.first().replaceFirstChar { it.lowercase() }}",
                "Quick cycle update (${guide.phaseTitle.lowercase()}): ${guide.whatPartnerShouldKnow} What would help me right now: ${guide.supportTips.first().replaceFirstChar { it.lowercase() }}",
            )
        }
        ShareKind.FEELING_BETTER -> tr("Ça va mieux aujourd'hui, merci d'avoir été là.", "I'm feeling better today, thanks for being there.")
    }
}
