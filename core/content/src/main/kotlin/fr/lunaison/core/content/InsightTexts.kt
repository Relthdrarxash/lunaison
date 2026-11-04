package fr.lunaison.core.content

import fr.lunaison.core.cycle.Anticipation
import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.Onset
import fr.lunaison.core.cycle.Phase
import kotlin.math.abs

/**
 * Phrases des statistiques personnelles. Elles décrivent ce qui a été noté, en comptes simples,
 * sans jugement ni diagnostic. Regroupées ici pour être testées avec les autres textes.
 */
object InsightTexts {

    fun days(count: Int): String = if (abs(count) <= 1) "${abs(count)} jour" else "${abs(count)} jours"

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> "les règles"
        Phase.FOLLICULAR -> "la phase folliculaire"
        Phase.OVULATION -> "la période autour de l'ovulation"
        Phase.LUTEAL -> "la phase lutéale"
        Phase.LATE -> "le retard des règles"
    }

    fun recapLines(recap: CycleRecap): List<String> = buildList {
        val length = recap.record.length
        val difference = recap.difference
        add(
            when {
                difference == null -> "Il a duré ${days(length)}."
                abs(difference) <= CycleConfig.USUAL_TOLERANCE -> "Il a duré ${days(length)}, comme d'habitude."
                difference > 0 -> "Il a duré ${days(length)}, ${days(difference)} de plus que d'habitude (${recap.usualLength})."
                else -> "Il a duré ${days(length)}, ${days(difference)} de moins que d'habitude (${recap.usualLength})."
            },
        )
        recap.record.periodLength?.let { add("Ses règles ont duré ${days(it)}.") }
        recap.record.predictionError?.let { error ->
            add(
                when {
                    error == 0 -> "Vos règles sont arrivées à la date estimée."
                    error > 0 -> "Vos règles sont arrivées ${days(error)} après la date estimée."
                    else -> "Vos règles sont arrivées ${days(error)} avant la date estimée."
                },
            )
        }
        val usualPain = recap.usualPainDays
        if (usualPain != null && (recap.record.painDays > 0 || usualPain > 0)) {
            add("Jours de douleur gênante ou forte : ${recap.record.painDays}, contre $usualPain d'habitude.")
        }
        if (!recap.withinReferences) add("Cette durée sort des repères habituels. Si cela se répète, la fiche « Quand consulter » dit quoi faire.")
    }

    fun anticipationLine(anticipation: Anticipation): String {
        val moment = if (anticipation.inDays == 1) "Demain" else "Dans ${days(anticipation.inDays)}"
        return "$moment commence ${phaseName(anticipation.phase)}, où vous notez souvent : " +
            anticipation.pattern.labels.joinToString(", ") { it.lowercase() } + "."
    }

    fun overviewLines(overview: CycleOverview): List<String> = buildList {
        add(
            if (overview.shortest == overview.longest) {
                "Vos ${overview.records.size} derniers cycles ont tous duré ${days(overview.usualLength)}."
            } else {
                "Vos cycles durent en général ${days(overview.usualLength)}, de ${overview.shortest} à ${days(overview.longest)} sur les ${overview.records.size} derniers."
            },
        )
        overview.usualPeriodLength?.let { add("Vos règles durent en général ${days(it)}.") }
        add(
            if (overview.withinReferences) {
                "Ces durées sont dans les repères habituels."
            } else {
                "Certaines durées sortent des repères habituels. La fiche « Quand consulter » dit quand en parler."
            },
        )
    }

    fun predictionLine(overview: CycleOverview): String {
        val error = overview.typicalPredictionError
        return when {
            error == null -> "Il faut encore quelques cycles pour mesurer la justesse des prévisions."
            error == 0 -> "Vos règles sont en général arrivées à la date estimée, sur ${overview.predictionsChecked} cycles vérifiés."
            else -> "Vos règles sont en général arrivées à ${days(error)} de la date estimée, sur ${overview.predictionsChecked} cycles vérifiés. C'est pour cela que Lunaison affiche une plage de dates."
        }
    }

    fun onsetLine(onset: Onset): String = when (onset.daysBefore) {
        1 -> "${onset.label} : en général la veille des règles (${onset.cycles} cycles)."
        else -> "${onset.label} : en général ${days(onset.daysBefore)} avant les règles (${onset.cycles} cycles)."
    }

    const val NOTHING_YET = "Vos statistiques apparaîtront quand deux cycles complets auront été notés : il faut deux débuts de règles après le premier."

    const val HOW_IT_WORKS = "Calculé sur votre téléphone, à partir de ce que vous avez noté. Seuls les jours remplis comptent, et rien n'est affiché avant deux cycles. Ces chiffres décrivent vos cycles, ils ne posent pas de diagnostic."
}
