package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.time.LocalDate

/**
 * Un conseil de la banque d'une phase.
 *
 * Il passe en premier les jours où la personne a noté l'un des éléments de [symptoms], [moods], [pain] ou [flow].
 * Chaque conseil cite au moins une source, sauf ceux marqués [withoutStudy] : ce sont des conseils de bon sens
 * ou d'usage de l'appli, listés comme tels dans documentation/sources.md.
 */
data class Tip(
    val text: String,
    val sources: List<Source> = emptyList(),
    val symptoms: Set<Symptom> = emptySet(),
    val moods: Set<Mood> = emptySet(),
    val pain: Set<Pain> = emptySet(),
    val flow: Set<Flow> = emptySet(),
    val withoutStudy: Boolean = false,
) {
    fun matches(log: DayLog): Boolean =
        log.symptoms.any { it in symptoms } ||
            log.moods.any { it in moods } ||
            log.pain in pain ||
            log.flow in flow
}

/**
 * Conseils affichés sur l'accueil selon la phase estimée.
 * On dit ce qui se passe et ce qui aide, sans promettre qu'une phase « doit » se vivre d'une certaine façon :
 * adapter tout son quotidien à chaque phase ne repose pas sur des études solides.
 */
data class PhaseGuide(
    val phase: Phase,
    val name: String,
    val whatHappens: String,
    val tips: List<Tip>,
    val articleId: String,
)

private val PAINFUL = setOf(Pain.MODERATE, Pain.SEVERE)
private val PMS_SYMPTOMS = setOf(Symptom.BLOATING, Symptom.BREAST_TENDERNESS, Symptom.CRAVINGS)
private val LOW_MOODS = setOf(Mood.IRRITABLE, Mood.SENSITIVE, Mood.SAD, Mood.ANXIOUS)

object PhaseGuides {

    val menstrual = PhaseGuide(
        phase = Phase.MENSTRUAL,
        name = "Règles",
        whatHappens = "La paroi de l'utérus s'élimine et les hormones sont au plus bas. Fatigue et douleurs sont fréquentes les premiers jours.",
        tips = listOf(
            Tip(
                "La chaleur sur le bas du ventre soulage autant qu'un anti-inflammatoire dans les études. Attention aux brûlures : jamais de chaleur trop forte directement sur la peau.",
                listOf(Sources.HEAT_2018),
                pain = PAINFUL,
            ),
            Tip(
                "Un anti-inflammatoire comme l'ibuprofène soulage mieux que le paracétamol, surtout pris dès le début des douleurs. Respectez la notice.",
                listOf(Sources.COCHRANE_NSAIDS_2015),
                pain = PAINFUL,
            ),
            Tip(
                "Une douleur qui vous empêche de faire vos activités n'est pas à supporter : elle mérite un avis médical.",
                listOf(Sources.ACOG_DYSMENORRHEA_2018),
                pain = setOf(Pain.SEVERE),
            ),
            Tip(
                "Les règles font perdre du fer. Légumineuses, viande ou poisson, avec une source de vitamine C comme un agrume ou un kiwi, aident à le compenser.",
                listOf(Sources.EFSA_IRON_2015),
                symptoms = setOf(Symptom.FATIGUE),
                moods = setOf(Mood.LOW_ENERGY),
                flow = setOf(Flow.HEAVY),
            ),
            Tip(
                "Le thé et le café gênent l'absorption du fer : mieux vaut les prendre à distance des repas.",
                listOf(Sources.EFSA_IRON_2015),
            ),
            Tip(
                "Si vous changez de protection en moins de trois heures, ou si vous voyez des caillots de plus de 2,5 cm, parlez-en à un professionnel de santé.",
                listOf(Sources.MSD_PARAMETERS),
                flow = setOf(Flow.HEAVY),
            ),
            Tip(
                "Tampon ou coupe : respectez la durée de port indiquée sur la notice. Pour la nuit, une serviette ou une culotte menstruelle évite de la dépasser.",
                listOf(Sources.ANSES_TSS_2025),
            ),
            Tip(
                "Quelques petits essais suggèrent que le gingembre en poudre réduit la douleur les premiers jours. Les preuves sont faibles.",
                listOf(Sources.GINGER_2015),
                pain = setOf(Pain.MILD),
            ),
        ),
        articleId = "douleurs",
    )

    val follicular = PhaseGuide(
        phase = Phase.FOLLICULAR,
        name = "Phase folliculaire",
        whatHappens = "Les œstrogènes remontent pendant qu'un follicule mûrit dans un ovaire. Beaucoup de personnes se sentent plus en forme, d'autres ne remarquent aucune différence.",
        tips = listOf(
            Tip(
                "Une activité physique régulière, environ trois séances par semaine, peut réduire les douleurs des règles suivantes. Les études sont modestes, mais le risque est faible.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Marche, natation, yoga : l'effet sur les douleurs de règles a été observé avec des activités variées. Choisissez celle qui vous convient.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Les poissons gras, les noix et l'huile de colza apportent des oméga-3, qui ont un peu réduit les douleurs de règles dans plusieurs essais.",
                listOf(Sources.OMEGA3_2022),
            ),
            Tip(
                "Après des règles abondantes, des repas riches en fer aident à refaire ses réserves. Un complément ne se prend que sur avis médical.",
                listOf(Sources.EFSA_IRON_2015),
                symptoms = setOf(Symptom.FATIGUE),
                moods = setOf(Mood.LOW_ENERGY),
            ),
            Tip(
                "Le cycle de 28 jours est une moyenne : dans une étude de plus de 600 000 cycles, seuls 13 % duraient exactement 28 jours.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "Notez votre humeur et votre énergie quelques jours : Lunaison vous montrera ce qui revient vraiment d'un cycle à l'autre.",
                withoutStudy = true,
            ),
        ),
        articleId = "cycle",
    )

    val ovulation = PhaseGuide(
        phase = Phase.OVULATION,
        name = "Autour de l'ovulation",
        whatHappens = "L'ovaire libère un ovule. Certaines personnes ressentent une petite douleur d'un côté du ventre ou remarquent des pertes plus fluides et filantes.",
        tips = listOf(
            Tip(
                "Cette période est estimée à partir de vos règles, avec une marge d'erreur : elle ne permet pas d'éviter une grossesse.",
                listOf(Sources.TRUSSELL_2011),
            ),
            Tip(
                "L'ovulation n'a pas lieu au 14e jour pour tout le monde : elle arrive en général 12 à 13 jours avant les règles suivantes.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "La durée du cycle varie en moyenne de 2 à 3 jours d'un mois à l'autre, même chez une personne régulière. L'estimation de l'ovulation varie d'autant.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "Les méthodes fondées sur le calendrier du cycle échouent souvent : jusqu'à environ une personne sur quatre tombe enceinte en un an. Un centre de santé sexuelle peut vous aider à choisir une contraception.",
                listOf(Sources.TRUSSELL_2011),
            ),
            Tip(
                "Une activité physique régulière peut réduire les douleurs des règles à venir. Il n'est jamais trop tard dans le cycle pour s'y mettre.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
        ),
        articleId = "previsions",
    )

    val luteal = PhaseGuide(
        phase = Phase.LUTEAL,
        name = "Phase lutéale",
        whatHappens = "La progestérone domine. Dans les derniers jours, seins sensibles, ventre gonflé, fatigue ou humeur changeante sont fréquents.",
        tips = listOf(
            Tip(
                "Produits laitiers, eaux riches en calcium ou amandes : un apport suffisant en calcium a réduit les symptômes prémenstruels dans un essai.",
                listOf(Sources.CALCIUM_PMS_1998),
                symptoms = PMS_SYMPTOMS,
                moods = LOW_MOODS,
            ),
            Tip(
                "Si vos règles sont douloureuses, un anti-inflammatoire marche mieux pris dès les premières douleurs : prévoyez-en un à portée de main.",
                listOf(Sources.COCHRANE_NSAIDS_2015),
            ),
            Tip(
                "Une activité physique régulière peut aussi atténuer les douleurs des règles qui arrivent.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Les oméga-3 des poissons gras, des noix ou de l'huile de colza ont un peu réduit les douleurs de règles dans plusieurs essais.",
                listOf(Sources.OMEGA3_2022),
            ),
            Tip(
                "Si les jours qui viennent s'annoncent chargés, allégez ce qui peut l'être, ou prévenez votre entourage.",
                withoutStudy = true,
                moods = LOW_MOODS,
            ),
            Tip(
                "Si ces jours-là votre moral s'effondre ou si l'anxiété abîme vos relations, parlez-en à un médecin : une forme sévère du syndrome prémenstruel se soigne.",
                withoutStudy = true,
                moods = setOf(Mood.SAD, Mood.ANXIOUS),
            ),
            Tip(
                "Vos règles sont attendues dans une plage de dates : gardez une protection sur vous les derniers jours.",
                withoutStudy = true,
            ),
        ),
        articleId = "spm",
    )

    val late = PhaseGuide(
        phase = Phase.LATE,
        name = "Règles en retard",
        whatHappens = "Vos règles n'ont pas commencé dans la plage estimée. Stress, voyage, maladie, changement de poids, sport intense ou grossesse peuvent l'expliquer.",
        tips = listOf(
            Tip("Si elles ont commencé et que vous l'avez oublié, ajoutez-les dans le calendrier.", withoutStudy = true),
            Tip("Si une grossesse est possible, un test urinaire est en général fiable dès maintenant.", withoutStudy = true),
            Tip("Sans règles depuis plus de trois mois, demandez un avis médical.", listOf(Sources.ACOG_ADOLESCENTS_2015)),
        ),
        articleId = "previsions",
    )

    val all: List<PhaseGuide> = listOf(menstrual, follicular, ovulation, luteal, late)

    fun forPhase(phase: Phase): PhaseGuide = all.first { it.phase == phase }
}

/** Choisit le conseil du jour : un seul, pour que l'accueil change sans s'alourdir. */
object DailyTip {

    /**
     * D'abord un conseil lié à ce que la personne a noté aujourd'hui. Sinon, la banque tourne d'un cran
     * chaque jour : le même conseil toute la journée, un autre le lendemain, et aucun ne revient avant
     * que tous soient passés.
     */
    fun pick(guide: PhaseGuide, date: LocalDate, today: DayLog?): Tip? {
        if (guide.tips.isEmpty()) return null
        val day = date.toEpochDay()
        val relevant = today?.let { log -> guide.tips.filter { it.matches(log) } }.orEmpty()
        val pool = relevant.ifEmpty { guide.tips }
        return pool[Math.floorMod(day, pool.size.toLong()).toInt()]
    }
}
