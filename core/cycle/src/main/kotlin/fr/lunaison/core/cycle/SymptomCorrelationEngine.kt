package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.time.LocalDate

/**
 * Une correspondance observée avec bienveillance entre deux symptômes ou ressentis.
 * Rétrospective pure : jamais une alerte ou une prédiction anxiogène.
 */
data class SymptomCorrelation(
    /** Le symptôme précurseur ou concomitant (ex: Troubles digestifs, Fatigue). */
    val precursor: Symptom,
    /** La manifestation pelvienne ou douloureuse associée (ex: Douleur pelvienne, Crampes). */
    val target: Symptom,
    /** Décalage en jours (0 = le même jour, 1 = la veille, 2 = deux jours avant). */
    val lagDays: Int,
    /** Nombre de cycles ou épisodes distincts où cette séquence s'est produite. */
    val occurrences: Int,
    /** Message d'accompagnement doux axé sur le confort personnel et l'écoute de son corps. */
    val caringNote: String,
)

object SymptomCorrelationEngine {

    /** Symptômes cibles typiques des douleurs pelviennes chroniques / endométriose (les 5 « D »). */
    val PELVIC_TARGETS = setOf(
        Symptom.PELVIC_PAIN,
        Symptom.CRAMPS,
        Symptom.DYSPAREUNIA,
        Symptom.DIGESTIVE_PAIN,
        Symptom.URINARY_PAIN,
        Symptom.RADIATING_PAIN,
    )

    /** Précurseurs fonctionnels fréquents (digestifs, neuro-végétatifs, fatigue). */
    val PRODROMAL_CANDIDATES = setOf(
        Symptom.BLOATING,
        Symptom.DIGESTIVE,
        Symptom.FATIGUE,
        Symptom.BACK_PAIN,
        Symptom.HEADACHE,
        Symptom.BRAIN_FOG,
    )

    /** Seuil minimal de répétition pour ne pas extrapoler sur une coïncidence isolée. */
    const val MIN_OCCURRENCES = 2

    /**
     * Analyse les journées renseignées pour détecter les correspondances répétées.
     * @param logs Historique des journées notées
     * @param maxLagDays Fenêtre maximale d'anticipation (par défaut 2 jours)
     * @return Liste ordonnée des correspondances les plus stables
     */
    fun analyze(logs: List<DayLog>, maxLagDays: Int = 2): List<SymptomCorrelation> {
        val sortedLogs = logs.filter { it.isTracked }.sortedBy { it.date }
        if (sortedLogs.size < 4) return emptyList()

        val byDate = sortedLogs.associateBy { it.date }
        val dates = sortedLogs.map { it.date }

        // Clé: (Precursor, Target, LagDays) -> Ensemble de dates où la cible s'est produite
        val matchTracker = mutableMapOf<Triple<Symptom, Symptom, Int>, MutableSet<LocalDate>>()

        for (targetDate in dates) {
            val targetLog = byDate[targetDate] ?: continue
            val activeTargets = targetLog.symptoms.filter { it in PELVIC_TARGETS }.toMutableSet()

            // Si forte douleur notée sans tag spécifique, on l'associe à la douleur pelvienne
            if (targetLog.pain == Pain.MODERATE || targetLog.pain == Pain.SEVERE) {
                activeTargets.add(Symptom.PELVIC_PAIN)
            }

            if (activeTargets.isEmpty()) continue

            for (lag in 0..maxLagDays) {
                val precursorDate = targetDate.minusDays(lag.toLong())
                val precursorLog = byDate[precursorDate] ?: continue

                val activePrecursors = precursorLog.symptoms.filter { it in PRODROMAL_CANDIDATES }

                for (precursor in activePrecursors) {
                    for (target in activeTargets) {
                        // Éviter de corréler un symptôme avec lui-même au même jour
                        if (lag == 0 && precursor == target) continue

                        val key = Triple(precursor, target, lag)
                        matchTracker.getOrPut(key) { mutableSetOf() }.add(targetDate)
                    }
                }
            }
        }

        return matchTracker
            .filter { (_, targetDates) -> targetDates.size >= MIN_OCCURRENCES }
            .map { (key, targetDates) ->
                val (precursor, target, lag) = key
                SymptomCorrelation(
                    precursor = precursor,
                    target = target,
                    lagDays = lag,
                    occurrences = targetDates.size,
                    caringNote = generateCaringNote(precursor, target, lag)
                )
            }
            .sortedByDescending { it.occurrences }
    }

    private fun generateCaringNote(precursor: Symptom, target: Symptom, lagDays: Int): String {
        val timing = when (lagDays) {
            0 -> "le même jour que"
            1 -> "la veille de"
            else -> "$lagDays jours avant"
        }
        return "Lors de plusieurs cycles précédents, ${precursor.label.lowercase()} a été noté $timing vos épisodes de ${target.label.lowercase()}. " +
            "Prendre un temps calme, une source de chaleur ou adapter vos activités quand ce signe apparaît peut vous aider à vivre cette période plus sereinement."
    }
}
