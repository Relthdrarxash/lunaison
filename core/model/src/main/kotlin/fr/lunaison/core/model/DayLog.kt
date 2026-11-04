package fr.lunaison.core.model

import java.time.LocalDate

/** Flux du jour. Les traces seules ne marquent pas le début des règles. */
enum class Flow(val label: String) {
    SPOTTING("Traces"),
    LIGHT("Léger"),
    MEDIUM("Moyen"),
    HEAVY("Abondant"),
    ;

    /** Un vrai saignement, par opposition à de simples traces. */
    val isBleeding: Boolean get() = this != SPOTTING
}

/** Douleur ressentie, décrite par son effet plutôt que par un chiffre. */
enum class Pain(val label: String, val description: String) {
    NONE("Aucune", "Pas de douleur"),
    MILD("Légère", "Je la sens, sans que ça me gêne"),
    MODERATE("Gênante", "Elle me gêne, mais je fais ce que j'avais prévu"),
    SEVERE("Forte", "Elle m'empêche de faire mes activités"),
}

enum class Symptom(val label: String) {
    HEADACHE("Maux de tête"),
    BACK_PAIN("Mal de dos"),
    BLOATING("Ventre gonflé"),
    BREAST_TENDERNESS("Seins sensibles"),
    DIGESTIVE("Troubles digestifs"),
    NAUSEA("Nausées"),
    FATIGUE("Fatigue"),
    SLEEP("Sommeil perturbé"),
    CRAVINGS("Fringales"),
    ACNE("Boutons"),
}

/** Humeurs du jour, plusieurs possibles, notées à part des symptômes physiques. Libellés non genrés. */
enum class Mood(val label: String, val positive: Boolean) {
    CALM("Calme", true),
    HAPPY("De bonne humeur", true),
    ENERGETIC("Énergie haute", true),
    MOTIVATED("Motivation", true),
    SENSITIVE("À fleur de peau", false),
    IRRITABLE("Irritable", false),
    ANXIOUS("Anxiété", false),
    STRESSED("Stress", false),
    SAD("Triste", false),
    LOW_ENERGY("Énergie basse", false),
}

/** Ce que la personne a noté pour une journée. Tout est facultatif. */
data class DayLog(
    val date: LocalDate,
    val flow: Flow? = null,
    val pain: Pain? = null,
    val symptoms: Set<Symptom> = emptySet(),
    val moods: Set<Mood> = emptySet(),
    val note: String = "",
) {
    val isEmpty: Boolean
        get() = flow == null && pain == null && symptoms.isEmpty() && moods.isEmpty() && note.isBlank()
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
