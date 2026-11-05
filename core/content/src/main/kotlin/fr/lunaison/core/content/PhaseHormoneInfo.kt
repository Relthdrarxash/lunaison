package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.tr

/** Niveau relatif d'une hormone à un stade du cycle. */
enum class HormoneLevel(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    LOW("Bas", "Low", "Niski", "Низький", "Bajo", "Niedrig"),
    RISING("En hausse", "Rising", "Wzrastający", "Зростає", "En ascenso", "Steigend"),
    PEAK("Pic maximal", "Peak", "Szczyt", "Пік", "Pico máximo", "Spitze"),
    DOMINANT("Dominant", "Dominant", "Dominujący", "Домінує", "Dominante", "Dominant"),
    FALLING("En baisse", "Falling", "Opadający", "Спадає", "En descenso", "Fallend"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/** Vue d'ensemble des 4 hormones clés du cycle ovarien. */
data class HormoneSnapshot(
    val estrogen: HormoneLevel,
    val progesterone: HormoneLevel,
    val lh: HormoneLevel,
    val fsh: HormoneLevel,
    val summary: String,
)

/** Profil physiologique, atouts et défis d'une phase du cycle. */
data class PhaseProfile(
    val phase: Phase,
    val title: String,
    val hormones: HormoneSnapshot,
    val strengths: List<String>,
    val challenges: List<String>,
    val supportAdvice: List<String>,
)

object PhaseHormoneInfo {

    fun profile(phase: Phase): PhaseProfile = when (phase) {
        Phase.MENSTRUAL -> PhaseProfile(
            phase = Phase.MENSTRUAL,
            title = tr("Règles : réinitialisation et repos", "Period: reset and rest"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.LOW,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.RISING,
                summary = tr(
                    "Les hormones sont à leur niveau plancher, déclenchant le détachement de la muqueuse utérine.",
                    "Hormones are at baseline levels, triggering the shedding of the uterine lining.",
                ),
            ),
            strengths = listOf(
                tr("Soulagement immédiat du syndrome prémenstruel et des tensions nerveuses.", "Immediate relief from PMS symptoms and nervous tension."),
                tr("Tendance naturelle au calme, au recentrage et au lâcher-prise.", "Natural tendency towards calm, introspection, and slowing down."),
                tr("Nouveau départ biologique : le corps commence déjà à préparer le cycle suivant.", "Fresh biological start: the body is already gearing up for the next cycle."),
            ),
            challenges = listOf(
                tr("Crampes utérines déclenchées par les prostaglandines lors des contractions.", "Uterine cramps caused by prostaglandins during contractions."),
                tr("Baisse d'énergie physique et perte modérée de fer via les saignements.", "Lower physical energy and moderate iron loss through bleeding."),
                tr("Sensibilité digestive ou petits maux de tête possibles les premiers jours.", "Digestive sensitivity or mild headaches possible in early days."),
            ),
            supportAdvice = listOf(
                tr("Chaleur locale (bouillotte) pour relâcher les fibres musculaires de l'utérus.", "Local warmth (heating pad) to relax uterine muscle fibers."),
                tr("Privilégier les aliments riches en fer associés à la vitamine C.", "Favor iron-rich foods paired with vitamin C for better absorption."),
                tr("Ne pas hésiter à alléger ses journées sans culpabilité.", "Lighten your schedule when possible without guilt."),
            ),
        )

        Phase.FOLLICULAR -> PhaseProfile(
            phase = Phase.FOLLICULAR,
            title = tr("Phase folliculaire : regain d'élan et clarté", "Follicular phase: renewed drive and clarity"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.RISING,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.DOMINANT,
                summary = tr(
                    "La FSH stimule les follicules ovariens ; l'un d'eux mûrit et produit des œstrogènes en hausse.",
                    "FSH stimulates ovarian follicles; one matures and produces rising estrogen.",
                ),
            ),
            strengths = listOf(
                tr("Regain net d'énergie physique, de vitalité et d'endurance musculaire.", "Marked boost in physical energy, stamina, and muscle recovery."),
                tr("Hausse de la motivation, curiosité et facilité pour démarrer de nouveaux projets.", "Higher motivation, curiosity, and ease starting new projects."),
                tr("Peau souvent plus lumineuse grâce à la stimulation du collagène par les œstrogènes.", "Clearer, more hydrated skin thanks to estrogen stimulating collagen."),
            ),
            challenges = listOf(
                tr("Transition parfois progressive les premiers jours après la fin des règles.", "Sometimes a slow transition in the first days right after bleeding stops."),
                tr("Risque d'en faire trop d'un coup par enthousiasme débordant.", "Risk of overcommitting or burnout from sudden surges of energy."),
            ),
            supportAdvice = listOf(
                tr("Idéal pour planifier des entraînements sportifs plus intenses ou progressifs.", "Ideal time for higher-intensity workouts and progressive exercise."),
                tr("Profiter de la clarté cognitive pour les tâches d'apprentissage ou de synthèse.", "Leverage cognitive clarity for complex learning or strategic planning."),
            ),
        )

        Phase.OVULATION -> PhaseProfile(
            phase = Phase.OVULATION,
            title = tr("Autour de l'ovulation : fertilité et rayonnement", "Around ovulation: fertility and peak vitality"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.PEAK,
                progesterone = HormoneLevel.RISING,
                lh = HormoneLevel.PEAK,
                fsh = HormoneLevel.PEAK,
                summary = tr(
                    "Le pic massif de LH provoque la rupture du follicule et libère l'ovocyte dans la trompe.",
                    "A surge in LH triggers follicle rupture, releasing the egg into the fallopian tube.",
                ),
            ),
            strengths = listOf(
                tr("Pic de sociabilité, de confiance en soi et d'aisance relationnelle.", "Peak confidence, sociability, and verbal ease in social settings."),
                tr("Libido et désir généralement au plus haut du cycle.", "Libido and sexual desire typically at their highest of the cycle."),
                tr("Force musculaire et coordination motrice au maximum.", "Peak muscular strength and neuromuscular coordination."),
            ),
            challenges = listOf(
                tr("Tiraillements pelviens d'un côté (douleur ovarienne d'ovulation).", "Mild pelvic twinges on one side (mittelschmerz ovulation pain)."),
                tr("Glaire filante et abondante, sensation d'humidité intime marquée.", "Abundant, stretchy cervical mucus and heightened intimate sensation."),
                tr("Fenêtre fertile maximale : attention contraceptive stricte si pas de désir d'enfant.", "Peak fertility window: strict contraception needed if pregnancy is not desired."),
            ),
            supportAdvice = listOf(
                tr("Noter la glaire cervicale pour repérer précisément votre propre schéma corporel.", "Log cervical mucus observations to map your personal body patterns."),
                tr("Garder en tête que les spermatozoïdes peuvent attendre l'ovocyte jusqu'à 5 jours.", "Remember that sperm can wait for the egg for up to 5 days."),
            ),
        )

        Phase.LUTEAL -> PhaseProfile(
            phase = Phase.LUTEAL,
            title = tr("Phase lutéale : cocooning et vigilance", "Luteal phase: calm focus and self-care"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.FALLING,
                progesterone = HormoneLevel.DOMINANT,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.LOW,
                summary = tr(
                    "Le corps jaune sécrète la progestérone qui épaissit la muqueuse utérine, avant de chuter.",
                    "The corpus luteum secretes progesterone to nourish the uterine lining, then drops.",
                ),
            ),
            strengths = listOf(
                tr("Progestérone calmante en première partie : sommeil plus profond et détente.", "Calming progesterone in the first half: deeper sleep and physical relaxation."),
                tr("Sens du détail aiguisé, propice au tri, à l'organisation et aux finitions.", "Keen eye for detail, well suited for organizing, editing, and closure."),
                tr("Métabolisme basal légèrement accru (dépense énergétique un peu plus élevée).", "Slightly elevated basal metabolic rate (burning slightly more energy)."),
            ),
            challenges = listOf(
                tr("Baisse de sérotonine en fin de phase : hypersensibilité émotionnelle ou irritabilité.", "Drop in serotonin later in the phase: mood swings, irritability, or tearfulness."),
                tr("Rétention d'eau, sensation de gonflement abdominal et seins tendus.", "Water retention, bloating sensation, and tender or swollen breasts."),
                tr("Fringales glucidiques (réflexe naturel du cerveau face au déficit de sérotonine).", "Carb cravings (the brain's natural response to lower serotonin)."),
            ),
            supportAdvice = listOf(
                tr("Apport suffisant en magnésium et calcium pour calmer les tensions neuromusculaires.", "Ensure adequate magnesium and calcium intake to ease neuromuscular tension."),
                tr("Modérer les excitants (caféine, alcool) qui accentuent l'anxiété prémenstruelle.", "Moderate caffeine and alcohol, which can exacerbate premenstrual anxiety."),
                tr("Privilégier les activités douces (marche, étirements, respiration 4-6).", "Favor soothing activities such as walking, gentle stretching, or 4-6 breathing."),
            ),
        )

        Phase.LATE -> PhaseProfile(
            phase = Phase.LATE,
            title = tr("Retard de règles : écoute et bienveillance", "Late period: gentle observation"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.LOW,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.LOW,
                summary = tr(
                    "La phase lutéale s'étire ou l'ovulation a été décalée par un facteur de vie ou de santé.",
                    "The cycle is prolonged or ovulation was delayed by stress, travel, or pregnancy.",
                ),
            ),
            strengths = listOf(
                tr("Occasion d'écouter les signaux récents de fatigue, de stress ou de voyage.", "Opportunity to reflect on recent stress, travel, or lifestyle shifts."),
            ),
            challenges = listOf(
                tr("Inquiétude ou incertitude liée à l'attente des règles.", "Uncertainty or concern while waiting for bleeding to start."),
            ),
            supportAdvice = listOf(
                tr("Un test urinaire est fiable dès le premier jour de retard présumé.", "A home urine test is reliable from the first day of an expected missed period."),
                tr("Si une pause est souhaitée (grossesse ou contraception), le mode pause est disponible.", "If you need a break from forecasts, the pause mode is available in settings."),
            ),
        )
    }
}
