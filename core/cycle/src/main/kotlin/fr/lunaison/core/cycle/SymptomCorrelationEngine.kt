package fr.lunaison.core.cycle

import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import java.time.LocalDate

/** Clé d'identification typée pour une corrélation. */
internal data class CorrelationKey(
    val precursor: Symptom,
    val target: Symptom,
    val lagDays: Int,
)

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
    /** Nombre d'épisodes distincts (séparés par au moins un cycle) où cette séquence s'est produite. */
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

    /** Seuil minimal de cycles ou d'épisodes distincts requis. */
    const val MIN_OCCURRENCES = CycleConfig.MIN_CYCLES_FOR_STATS

    /** Écart minimal en jours entre deux cibles pour être considérées comme deux épisodes distincts (repère cycle). */
    const val MIN_EPISODE_GAP_DAYS = CycleConfig.MIN_PLAUSIBLE_CYCLE.toLong()

    /**
     * Analyse les journées renseignées pour détecter les correspondances répétées sur des épisodes distincts.
     * @param logs Historique des journées notées
     * @param maxLagDays Fenêtre maximale d'anticipation (par défaut 2 jours)
     * @return Liste ordonnée des correspondances les plus stables
     */
    fun analyze(logs: List<DayLog>, maxLagDays: Int = 2): List<SymptomCorrelation> {
        val sortedLogs = logs.filter { it.isTracked }.sortedBy { it.date }
        if (sortedLogs.size < 4) return emptyList()

        val byDate = sortedLogs.associateBy { it.date }
        val matchTracker = mutableMapOf<CorrelationKey, MutableList<LocalDate>>()

        for (targetLog in sortedLogs) {
            val activeTargets = targetLog.symptoms.filter { it in PELVIC_TARGETS }.toMutableSet()

            // N'associer à une douleur pelvienne générale que si aucun symptôme spécifique n'a été coché
            // et que la douleur déclarée est formellement forte (SEVERE).
            if (activeTargets.isEmpty() && targetLog.symptoms.isEmpty() && targetLog.pain == Pain.SEVERE) {
                activeTargets.add(Symptom.PELVIC_PAIN)
            }

            if (activeTargets.isEmpty()) continue

            for (lag in 0..maxLagDays) {
                val precursorDate = targetLog.date.minusDays(lag.toLong())
                val precursorLog = byDate[precursorDate] ?: continue

                for (precursor in precursorLog.symptoms) {
                    if (precursor !in PRODROMAL_CANDIDATES) continue
                    if (lag == 0 && precursor in activeTargets) continue

                    for (target in activeTargets) {
                        val key = CorrelationKey(precursor, target, lag)
                        val occurrencesList = matchTracker.getOrPut(key) { mutableListOf() }

                        // Règle éthique : l'occurrence doit appartenir à un épisode/cycle distinct
                        val isDistinctEpisode = occurrencesList.none { existingDate ->
                            kotlin.math.abs(targetLog.date.toEpochDay() - existingDate.toEpochDay()) < MIN_EPISODE_GAP_DAYS
                        }
                        if (isDistinctEpisode) {
                            occurrencesList.add(targetLog.date)
                        }
                    }
                }
            }
        }

        return matchTracker
            .filter { (_, targetDates) -> targetDates.size >= MIN_OCCURRENCES }
            .map { (key, targetDates) ->
                SymptomCorrelation(
                    precursor = key.precursor,
                    target = key.target,
                    lagDays = key.lagDays,
                    occurrences = targetDates.size,
                    caringNote = generateCaringNote(key.precursor, key.target, key.lagDays)
                )
            }
            .sortedByDescending { it.occurrences }
    }

    private fun generateCaringNote(precursor: Symptom, target: Symptom, lagDays: Int): String {
        val frTiming = when (lagDays) {
            0 -> "le même jour que"
            1 -> "la veille de"
            else -> "$lagDays jours avant"
        }
        val enTiming = when (lagDays) {
            0 -> "on the same day as"
            1 -> "the day before"
            else -> "$lagDays days before"
        }

        val frNote = "Lors de plusieurs cycles précédents, ${precursor.label.lowercase()} a été noté $frTiming vos épisodes de ${target.label.lowercase()}. " +
            "Prendre un temps calme, une source de chaleur ou adapter vos activités quand ce signe apparaît peut vous aider à vivre cette période plus sereinement."

        val enNote = "Across several previous cycles, ${precursor.label.lowercase()} was noted $enTiming your episodes of ${target.label.lowercase()}. " +
            "Taking a quiet moment, applying gentle warmth or pacing your day when this sign appears may help you feel more comfortable."

        return tr(frNote, enNote)
    }
}
