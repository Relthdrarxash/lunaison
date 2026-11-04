package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Language

/**
 * Conseils affichés sur l'accueil selon la phase estimée.
 * On dit ce qui se passe et ce qui aide, sans promettre qu'une phase « doit » se vivre d'une certaine façon :
 * adapter tout son quotidien à chaque phase ne repose pas sur des études solides.
 */
data class PhaseGuide(
    val phase: Phase,
    val name: String,
    val whatHappens: String,
    val tips: List<String>,
    val articleId: String,
)

object PhaseGuides {
    val all: List<PhaseGuide> get() = forLanguage(Language.current())

    fun forLanguage(language: Language): List<PhaseGuide> = when (language) {
        Language.FR -> PhaseGuidesFr.all
        Language.EN -> PhaseGuidesEn.all
    }

    fun forPhase(phase: Phase): PhaseGuide = all.first { it.phase == phase }
}

internal object PhaseGuidesFr {

    val menstrual = PhaseGuide(
        phase = Phase.MENSTRUAL,
        name = "Règles",
        whatHappens = "La paroi de l'utérus s'élimine et les hormones sont au plus bas. Fatigue et douleurs sont fréquentes les premiers jours.",
        tips = listOf(
            "La chaleur sur le bas du ventre soulage autant qu'un anti-inflammatoire dans les études.",
            "Un anti-inflammatoire marche mieux pris dès le début des douleurs, en respectant la notice.",
            "Pensez au fer : légumineuses, viande, poisson, avec une source de vitamine C.",
            "Tampon ou coupe : respectez la durée de port indiquée sur la notice.",
        ),
        articleId = "douleurs",
    )

    val follicular = PhaseGuide(
        phase = Phase.FOLLICULAR,
        name = "Phase folliculaire",
        whatHappens = "Les œstrogènes remontent pendant qu'un follicule mûrit dans un ovaire. Beaucoup de personnes se sentent plus en forme, d'autres ne remarquent aucune différence.",
        tips = listOf(
            "C'est souvent un bon moment pour reprendre une activité physique régulière : elle réduit aussi les douleurs des règles suivantes.",
            "Notez votre humeur et votre énergie quelques jours : vous verrez ce qui change vraiment au fil de votre cycle.",
        ),
        articleId = "cycle",
    )

    val ovulation = PhaseGuide(
        phase = Phase.OVULATION,
        name = "Autour de l'ovulation",
        whatHappens = "L'ovaire libère un ovule. Certaines personnes ressentent une petite douleur d'un côté du ventre ou remarquent des pertes plus fluides et filantes.",
        tips = listOf(
            "Cette période est estimée à partir de vos règles, avec une marge d'erreur : elle ne permet pas d'éviter une grossesse.",
            "Une petite douleur passagère d'un côté est fréquente. Une douleur forte ou qui dure mérite un avis médical.",
        ),
        articleId = "previsions",
    )

    val luteal = PhaseGuide(
        phase = Phase.LUTEAL,
        name = "Phase lutéale",
        whatHappens = "La progestérone domine. Dans les derniers jours, seins sensibles, ventre gonflé, fatigue ou humeur changeante sont fréquents.",
        tips = listOf(
            "Une activité physique régulière et un bon sommeil atténuent souvent les symptômes prémenstruels.",
            "Les produits laitiers, les eaux riches en calcium ou les amandes apportent du calcium, qui a réduit ces symptômes dans un essai.",
            "Si les jours qui viennent s'annoncent chargés, allégez ce qui peut l'être.",
        ),
        articleId = "spm",
    )

    val late = PhaseGuide(
        phase = Phase.LATE,
        name = "Règles en retard",
        whatHappens = "Vos règles n'ont pas commencé dans la plage estimée. Stress, voyage, maladie, changement de poids, sport intense ou grossesse peuvent l'expliquer.",
        tips = listOf(
            "Si elles ont commencé et que vous l'avez oublié, ajoutez-les dans le calendrier.",
            "Si une grossesse est possible, un test urinaire est en général fiable dès maintenant.",
            "Sans règles depuis plus de trois mois, demandez un avis médical.",
        ),
        articleId = "previsions",
    )

    val all: List<PhaseGuide> = listOf(menstrual, follicular, ovulation, luteal, late)
}
