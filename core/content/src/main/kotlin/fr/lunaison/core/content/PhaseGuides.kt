package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import java.time.LocalDate

/**
 * Un conseil de la banque d'une phase, en français et en anglais.
 *
 * Il passe en premier les jours où la personne a noté l'un des éléments de [symptoms], [moods], [pain] ou [flow].
 * Chaque conseil cite au moins une source, sauf ceux marqués [withoutStudy] : ce sont des conseils de bon sens
 * ou d'usage de l'appli, listés comme tels dans documentation/sources.md.
 */
data class Tip(
    val fr: String,
    val en: String,
    val sources: List<Source> = emptyList(),
    val symptoms: Set<Symptom> = emptySet(),
    val moods: Set<Mood> = emptySet(),
    val pain: Set<Pain> = emptySet(),
    val flow: Set<Flow> = emptySet(),
    val withoutStudy: Boolean = false,
) {
    /** Le conseil dans la langue de l'appli. */
    val text: String get() = tr(fr, en)

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
    private val nameFr: String,
    private val nameEn: String,
    private val whatHappensFr: String,
    private val whatHappensEn: String,
    val tips: List<Tip>,
    val articleId: String,
) {
    val name: String get() = tr(nameFr, nameEn)
    val whatHappens: String get() = tr(whatHappensFr, whatHappensEn)

    /** Les textes dans les deux langues, pour les tests. */
    val allTexts: List<String> get() = listOf(nameFr, nameEn, whatHappensFr, whatHappensEn) + tips.flatMap { listOf(it.fr, it.en) }
}

private val PAINFUL = setOf(Pain.MODERATE, Pain.SEVERE)
private val PMS_SYMPTOMS = setOf(Symptom.BLOATING, Symptom.BREAST_TENDERNESS, Symptom.CRAVINGS)
private val LOW_MOODS = setOf(Mood.IRRITABLE, Mood.SENSITIVE, Mood.SAD, Mood.ANXIOUS)

object PhaseGuides {

    val menstrual = PhaseGuide(
        phase = Phase.MENSTRUAL,
        nameFr = "Règles",
        nameEn = "Period",
        whatHappensFr = "La paroi de l'utérus s'élimine et les hormones sont au plus bas. Fatigue et douleurs sont fréquentes les premiers jours.",
        whatHappensEn = "The lining of the uterus is shed and hormone levels are at their lowest. Tiredness and pain are common in the first days.",
        tips = listOf(
            Tip(
                "La chaleur sur le bas du ventre soulage autant qu'un anti-inflammatoire dans les études. Attention aux brûlures : jamais de chaleur trop forte directement sur la peau.",
                "In studies, heat on the lower belly relieves pain as well as an anti-inflammatory. Watch out for burns: never put very strong heat directly on the skin.",
                listOf(Sources.HEAT_2018),
                pain = PAINFUL,
            ),
            Tip(
                "Un anti-inflammatoire comme l'ibuprofène soulage mieux que le paracétamol, surtout pris dès le début des douleurs. Respectez la notice.",
                "An anti-inflammatory such as ibuprofen relieves pain better than paracetamol (acetaminophen), especially when taken as soon as the pain starts. Follow the leaflet.",
                listOf(Sources.COCHRANE_NSAIDS_2015),
                pain = PAINFUL,
            ),
            Tip(
                "Une douleur qui vous empêche de faire vos activités n'est pas à supporter : elle mérite un avis médical.",
                "Pain that stops you from doing your activities is not something to put up with: it deserves medical advice.",
                listOf(Sources.ACOG_DYSMENORRHEA_2018),
                pain = setOf(Pain.SEVERE),
            ),
            Tip(
                "Les règles font perdre du fer. Légumineuses, viande ou poisson, avec une source de vitamine C comme un agrume ou un kiwi, aident à le compenser.",
                "Periods make you lose iron. Legumes, meat or fish, with a source of vitamin C such as citrus fruit or a kiwi, help make up for it.",
                listOf(Sources.EFSA_IRON_2015),
                symptoms = setOf(Symptom.FATIGUE),
                moods = setOf(Mood.LOW_ENERGY),
                flow = setOf(Flow.HEAVY),
            ),
            Tip(
                "Le thé et le café gênent l'absorption du fer : mieux vaut les prendre à distance des repas.",
                "Tea and coffee reduce iron absorption: it is better to have them away from meals.",
                listOf(Sources.EFSA_IRON_2015),
            ),
            Tip(
                "Si vous devez changer de protection toutes les 1 à 2 heures, ou vider votre coupe plus souvent que prévu, ou si vous perdez des caillots de plus de 2,5 cm environ, parlez-en à un professionnel de santé.",
                "If you need to change your pad or tampon every 1 to 2 hours, or empty your cup more often than recommended, or if you pass clots larger than about 2.5 cm (1 inch), talk to a health professional.",
                listOf(Sources.NHS_HEAVY_PERIODS),
                flow = setOf(Flow.HEAVY),
            ),
            Tip(
                "Tampon ou coupe : respectez la durée de port indiquée sur la notice. Pour la nuit, une serviette ou une culotte menstruelle évite de la dépasser.",
                "Tampon or cup: follow the wearing time given in the leaflet. At night, a pad or period underwear means you don't go over it.",
                listOf(Sources.ANSES_TSS_2025),
            ),
            Tip(
                "Quelques petits essais suggèrent que le gingembre en poudre réduit la douleur les premiers jours. Les preuves sont faibles.",
                "A few small trials suggest that ginger powder reduces pain in the first days. The evidence is weak.",
                listOf(Sources.GINGER_2015),
                pain = setOf(Pain.MILD),
            ),
        ),
        articleId = "douleurs",
    )

    val follicular = PhaseGuide(
        phase = Phase.FOLLICULAR,
        nameFr = "Phase folliculaire",
        nameEn = "Follicular phase",
        whatHappensFr = "Les œstrogènes remontent pendant qu'un follicule mûrit dans un ovaire. Beaucoup de personnes se sentent plus en forme, d'autres ne remarquent aucune différence.",
        whatHappensEn = "Estrogen rises again while a follicle matures in an ovary. Many people feel more energetic, others notice no difference at all.",
        tips = listOf(
            Tip(
                "Une activité physique régulière, environ trois séances par semaine, peut réduire les douleurs des règles suivantes. Les études sont modestes, mais le risque est faible.",
                "Regular physical activity, about three sessions a week, may reduce the pain of your next periods. The studies are modest, but the risk is low.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Marche, natation, yoga : l'effet sur les douleurs de règles a été observé avec des activités variées. Choisissez celle qui vous convient.",
                "Walking, swimming, yoga: the effect on period pain has been seen with a variety of activities. Choose the one that suits you.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Les poissons gras, les noix et l'huile de colza apportent des oméga-3, qui ont un peu réduit les douleurs de règles dans plusieurs essais.",
                "Oily fish, walnuts and canola oil provide omega-3 fatty acids, which slightly reduced period pain in several trials.",
                listOf(Sources.OMEGA3_2022),
            ),
            Tip(
                "Après des règles abondantes, des repas riches en fer aident à refaire ses réserves. Un complément ne se prend que sur avis médical.",
                "After heavy periods, iron-rich meals help rebuild your stores. Only take a supplement on medical advice.",
                listOf(Sources.EFSA_IRON_2015),
                symptoms = setOf(Symptom.FATIGUE),
                moods = setOf(Mood.LOW_ENERGY),
            ),
            Tip(
                "Le cycle de 28 jours est une moyenne : dans une étude de plus de 600 000 cycles, seuls 13 % duraient exactement 28 jours.",
                "The 28-day cycle is an average: in a study of more than 600,000 cycles, only 13% lasted exactly 28 days.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "Notez votre humeur et votre énergie quelques jours : Lunaison vous montrera ce qui revient vraiment d'un cycle à l'autre.",
                "Note your mood and energy for a few days: Lunaison will show you what really comes back from one cycle to the next.",
                withoutStudy = true,
            ),
        ),
        articleId = "cycle",
    )

    val ovulation = PhaseGuide(
        phase = Phase.OVULATION,
        nameFr = "Autour de l'ovulation",
        nameEn = "Around ovulation",
        whatHappensFr = "L'ovaire libère un ovule. Certaines personnes ressentent une petite douleur d'un côté du ventre ou remarquent des pertes plus fluides et filantes.",
        whatHappensEn = "The ovary releases an egg. Some people feel a slight pain on one side of the belly or notice clearer, stretchier discharge.",
        tips = listOf(
            Tip(
                "Cette période est estimée à partir de vos règles, avec une marge d'erreur : elle ne permet pas d'éviter une grossesse.",
                "This window is estimated from your periods, with a margin of error: it cannot be used to avoid pregnancy.",
                listOf(Sources.TRUSSELL_2011),
            ),
            Tip(
                "L'ovulation n'a pas lieu au 14e jour pour tout le monde : elle arrive en général 12 à 13 jours avant les règles suivantes.",
                "Ovulation does not happen on day 14 for everyone: it usually comes 12 to 13 days before the next period.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "La durée du cycle varie en moyenne de 2 à 3 jours d'un mois à l'autre, même chez une personne régulière. L'estimation de l'ovulation varie d'autant.",
                "Cycle length varies by 2 to 3 days on average from one month to the next, even for someone with regular cycles. The ovulation estimate varies just as much.",
                listOf(Sources.BULL_2019),
            ),
            Tip(
                "Les méthodes fondées sur le calendrier du cycle échouent souvent : jusqu'à environ une personne sur quatre tombe enceinte en un an. Un centre de santé sexuelle peut vous aider à choisir une contraception.",
                "Methods based on the cycle calendar often fail: up to about one in four people become pregnant within a year. A sexual health clinic can help you choose a method of contraception.",
                listOf(Sources.TRUSSELL_2011),
            ),
            Tip(
                "Une activité physique régulière peut réduire les douleurs des règles à venir. Il n'est jamais trop tard dans le cycle pour s'y mettre.",
                "Regular physical activity may reduce the pain of your coming periods. It is never too late in the cycle to start.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
        ),
        articleId = "previsions",
    )

    val luteal = PhaseGuide(
        phase = Phase.LUTEAL,
        nameFr = "Phase lutéale",
        nameEn = "Luteal phase",
        whatHappensFr = "La progestérone domine. Dans les derniers jours, seins sensibles, ventre gonflé, fatigue ou humeur changeante sont fréquents.",
        whatHappensEn = "Progesterone dominates. In the last days, tender breasts, bloating, tiredness or changing moods are common.",
        tips = listOf(
            Tip(
                "Produits laitiers, eaux riches en calcium ou amandes : un apport suffisant en calcium a réduit les symptômes prémenstruels dans un essai.",
                "Dairy products, calcium-rich mineral water or almonds: getting enough calcium reduced premenstrual symptoms in one trial.",
                listOf(Sources.CALCIUM_PMS_1998),
                symptoms = PMS_SYMPTOMS,
                moods = LOW_MOODS,
            ),
            Tip(
                "Si vos règles sont douloureuses, un anti-inflammatoire marche mieux pris dès les premières douleurs : prévoyez-en un à portée de main.",
                "If your periods are painful, an anti-inflammatory works better taken at the first sign of pain: keep one within reach.",
                listOf(Sources.COCHRANE_NSAIDS_2015),
            ),
            Tip(
                "Une activité physique régulière peut aussi atténuer les douleurs des règles qui arrivent.",
                "Regular physical activity may also ease the pain of the period that is coming.",
                listOf(Sources.COCHRANE_EXERCISE_2019),
            ),
            Tip(
                "Les oméga-3 des poissons gras, des noix ou de l'huile de colza ont un peu réduit les douleurs de règles dans plusieurs essais.",
                "Omega-3 fatty acids from oily fish, walnuts or canola oil slightly reduced period pain in several trials.",
                listOf(Sources.OMEGA3_2022),
            ),
            Tip(
                "Si les jours qui viennent s'annoncent chargés, allégez ce qui peut l'être, ou prévenez votre entourage.",
                "If the coming days look busy, lighten what you can, or let the people around you know.",
                withoutStudy = true,
                moods = LOW_MOODS,
            ),
            Tip(
                "Si ces jours-là votre moral s'effondre ou si l'anxiété abîme vos relations, parlez-en à un médecin : une forme sévère du syndrome prémenstruel se soigne.",
                "If your mood collapses on those days or anxiety damages your relationships, talk to a doctor: a severe form of premenstrual syndrome can be treated.",
                withoutStudy = true,
                moods = setOf(Mood.SAD, Mood.ANXIOUS),
            ),
            Tip(
                "Vos règles sont attendues dans une plage de dates : gardez une protection sur vous les derniers jours.",
                "Your period is expected within a range of dates: keep a pad, tampon or cup with you in the days before.",
                withoutStudy = true,
            ),
        ),
        articleId = "spm",
    )

    val late = PhaseGuide(
        phase = Phase.LATE,
        nameFr = "Règles en retard",
        nameEn = "Period late",
        whatHappensFr = "Vos règles n'ont pas commencé dans la plage estimée. Stress, voyage, maladie, changement de poids, sport intense ou grossesse peuvent l'expliquer.",
        whatHappensEn = "Your period has not started within the estimated range. Stress, travel, illness, weight change, intense exercise or pregnancy can explain it.",
        tips = listOf(
            Tip(
                "Si elles ont commencé et que vous l'avez oublié, ajoutez-les dans le calendrier.",
                "If it has started and you forgot to log it, add it in the calendar.",
                withoutStudy = true,
            ),
            Tip(
                "Si une grossesse est possible, un test urinaire est en général fiable dès maintenant.",
                "If a pregnancy is possible, a urine test is usually reliable from now on.",
                withoutStudy = true,
            ),
            Tip(
                "Sans règles depuis plus de trois mois, demandez un avis médical.",
                "With no period for more than three months, ask for medical advice.",
                listOf(Sources.ACOG_ADOLESCENTS_2015),
            ),
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
