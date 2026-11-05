package fr.lunaison.core.model

import java.time.LocalDate

/** Flux du jour. Les traces seules ne marquent pas le début des règles. */
enum class Flow(private val fr: String, private val en: String) {
    SPOTTING("Traces", "Spotting"),
    LIGHT("Léger", "Light"),
    MEDIUM("Moyen", "Medium"),
    HEAVY("Abondant", "Heavy"),
    ;

    val label: String get() = tr(fr, en)

    /** Un vrai saignement, par opposition à de simples traces. */
    val isBleeding: Boolean get() = this != SPOTTING
}

/** Douleur ressentie, décrite par son effet plutôt que par un chiffre. */
enum class Pain(private val fr: String, private val en: String, private val frDescription: String, private val enDescription: String) {
    NONE("Aucune", "None", "Pas de douleur", "No pain"),
    MILD("Légère", "Mild", "Je la sens, sans que ça me gêne", "I can feel it, but it doesn't bother me"),
    MODERATE("Gênante", "Bothersome", "Elle me gêne, mais je fais ce que j'avais prévu", "It bothers me, but I still do what I had planned"),
    SEVERE("Forte", "Severe", "Elle m'empêche de faire mes activités", "It stops me from doing my usual activities"),
    ;

    val label: String get() = tr(fr, en)
    val description: String get() = tr(frDescription, enDescription)
}

enum class Symptom(private val fr: String, private val en: String) {
    HEADACHE("Maux de tête", "Headache"),
    BACK_PAIN("Mal de dos", "Back pain"),
    BLOATING("Ventre gonflé", "Bloating"),
    BREAST_TENDERNESS("Seins sensibles", "Tender breasts"),
    DIGESTIVE("Troubles digestifs", "Digestive issues"),
    NAUSEA("Nausées", "Nausea"),
    FATIGUE("Fatigue", "Fatigue"),
    SLEEP("Sommeil perturbé", "Poor sleep"),
    CRAVINGS("Fringales", "Cravings"),
    ACNE("Boutons", "Acne"),
    PELVIC_PAIN("Douleur pelvienne", "Pelvic pain"),
    HOT_FLASHES("Bouffées de chaleur", "Hot flashes"),
    NIGHT_SWEATS("Sueurs nocturnes", "Night sweats"),
    BRAIN_FOG("Brouillard mental", "Brain fog"),
    ;

    val label: String get() = tr(fr, en)
}

/** Observation facultative de la glaire cervicale (suivi symptothermique / signes d'ovulation). */
enum class CervicalMucus(private val fr: String, private val en: String) {
    DRY("Sec ou absent", "Dry or none"),
    STICKY("Collant ou épais", "Sticky or thick"),
    CREAMY("Crémeux ou laiteux", "Creamy or lotion-like"),
    EGG_WHITE("Fluide, transparent (très fertile)", "Clear, stretchy (very fertile)"),
    ;

    val label: String get() = tr(fr, en)
}

/** Humeurs du jour, plusieurs possibles, notées à part des symptômes physiques. Libellés non genrés. */
enum class Mood(private val fr: String, private val en: String, val positive: Boolean) {
    CALM("Calme", "Calm", true),
    HAPPY("De bonne humeur", "In a good mood", true),
    ENERGETIC("Énergie haute", "High energy", true),
    MOTIVATED("Motivation", "Motivated", true),
    SENSITIVE("À fleur de peau", "Sensitive", false),
    IRRITABLE("Irritable", "Irritable", false),
    ANXIOUS("Anxiété", "Anxious", false),
    STRESSED("Stress", "Stressed", false),
    SAD("Triste", "Sad", false),
    LOW_ENERGY("Énergie basse", "Low energy", false),
    ;

    val label: String get() = tr(fr, en)
}

/** Ce que la personne a noté pour une journée. Tout est facultatif. */
data class DayLog(
    val date: LocalDate,
    val flow: Flow? = null,
    val pain: Pain? = null,
    val symptoms: Set<Symptom> = emptySet(),
    val moods: Set<Mood> = emptySet(),
    val note: String = "",
    /** Prise de contraception, traitement ou complément notée ce jour-là. */
    val medicationTaken: Boolean = false,
    /** Aspect de la glaire cervicale, si le suivi des signes d'ovulation est activé. */
    val mucus: CervicalMucus? = null,
    /** Température basale au réveil en °C (ex. 36.6f), si le suivi des signes d'ovulation est activé. */
    val basalTemperature: Float? = null,
) {
    val isEmpty: Boolean
        get() = flow == null && pain == null && symptoms.isEmpty() && moods.isEmpty() &&
            note.isBlank() && !medicationTaken && mucus == null && basalTemperature == null
}

/** Palette de couleurs de l'interface, toutes conformes au contraste WCAG AA. */
enum class ThemePalette(private val fr: String, private val en: String) {
    PLUM("Nuit de lune (Prune)", "Moonlight (Plum)"),
    SAGE("Forêt calme (Sauge)", "Calm forest (Sage)"),
    OCEAN("Marée (Océan)", "Tide (Ocean)"),
    TERRACOTTA("Terre chaude (Terracotta)", "Warm earth (Terracotta)"),
    ;

    val label: String get() = tr(fr, en)
}

/** Tranche d'âge déclarée : elle règle les repères de cycle et l'accès au partage. */
enum class AgeGroup {
    /** Moins de 18 ans : repères adaptés aux premières années de cycle, partage désactivé. */
    UNDER_18,
    ADULT,
}

/** Ce que la personne a indiqué au démarrage, avant d'avoir un historique. */
data class Baseline(
    val ageGroup: AgeGroup,
    val lastPeriodStart: LocalDate? = null,
    val usualCycleLength: Int? = null,
    val usualPeriodLength: Int? = null,
)
