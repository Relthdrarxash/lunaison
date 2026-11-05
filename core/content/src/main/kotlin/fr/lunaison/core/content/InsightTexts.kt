package fr.lunaison.core.content

import fr.lunaison.core.cycle.Anticipation
import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.Onset
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.tr
import kotlin.math.abs

/**
 * Phrases des statistiques personnelles, en français et en anglais. Elles décrivent ce qui a été noté,
 * en comptes simples, sans jugement ni diagnostic. Regroupées ici pour être testées avec les autres textes.
 */
object InsightTexts {

    fun days(count: Int): String {
        val n = abs(count)
        return if (n <= 1) tr("$n jour", "$n day") else tr("$n jours", "$n days")
    }

    fun phaseName(phase: Phase): String = when (phase) {
        Phase.MENSTRUAL -> tr("les règles", "your period")
        Phase.FOLLICULAR -> tr("la phase folliculaire", "the follicular phase")
        Phase.OVULATION -> tr("la période autour de l'ovulation", "the time around ovulation")
        Phase.LUTEAL -> tr("la phase lutéale", "the luteal phase")
        Phase.LATE -> tr("le retard des règles", "the late period")
    }

    fun recapLines(recap: CycleRecap): List<String> = buildList {
        val length = recap.record.length
        val difference = recap.difference
        add(
            when {
                difference == null -> tr("Il a duré ${days(length)}.", "It lasted ${days(length)}.")
                abs(difference) <= CycleConfig.USUAL_TOLERANCE -> tr(
                    "Il a duré ${days(length)}, comme d'habitude.",
                    "It lasted ${days(length)}, as usual.",
                )
                difference > 0 -> tr(
                    "Il a duré ${days(length)}, ${days(difference)} de plus que d'habitude (${recap.usualLength}).",
                    "It lasted ${days(length)}, ${days(difference)} longer than usual (${recap.usualLength}).",
                )
                else -> tr(
                    "Il a duré ${days(length)}, ${days(difference)} de moins que d'habitude (${recap.usualLength}).",
                    "It lasted ${days(length)}, ${days(difference)} shorter than usual (${recap.usualLength}).",
                )
            },
        )
        recap.record.periodLength?.let { add(tr("Ses règles ont duré ${days(it)}.", "The period lasted ${days(it)}.")) }
        recap.record.predictionError?.let { error ->
            add(
                when {
                    error == 0 -> tr("Vos règles sont arrivées à la date estimée.", "Your period arrived on the estimated date.")
                    error > 0 -> tr(
                        "Vos règles sont arrivées ${days(error)} après la date estimée.",
                        "Your period arrived ${days(error)} after the estimated date.",
                    )
                    else -> tr(
                        "Vos règles sont arrivées ${days(error)} avant la date estimée.",
                        "Your period arrived ${days(error)} before the estimated date.",
                    )
                },
            )
        }
        val usualPain = recap.usualPainDays
        if (usualPain != null && (recap.record.painDays > 0 || usualPain > 0)) {
            add(
                tr(
                    "Jours de douleur gênante ou forte : ${recap.record.painDays}, contre $usualPain d'habitude.",
                    "Days of bothersome or severe pain: ${recap.record.painDays}, compared with $usualPain usually.",
                ),
            )
        }
        if (!recap.withinReferences) {
            add(
                tr(
                    "Cette durée sort des repères habituels. Si cela se répète, la fiche « Quand consulter » dit quoi faire.",
                    "This length is outside the usual range. If it happens again, the \"When to see a doctor\" guide says what to do.",
                ),
            )
        }
    }

    fun anticipationLine(anticipation: Anticipation): String {
        val labels = anticipation.pattern.labels.joinToString(", ") { it.lowercase() }
        return if (anticipation.inDays == 1) {
            tr(
                "Demain commence ${phaseName(anticipation.phase)}, où vous notez souvent : $labels.",
                "Tomorrow ${phaseName(anticipation.phase)} begins, when you often log: $labels.",
            )
        } else {
            tr(
                "Dans ${days(anticipation.inDays)} commence ${phaseName(anticipation.phase)}, où vous notez souvent : $labels.",
                "In ${days(anticipation.inDays)}, ${phaseName(anticipation.phase)} begins, when you often log: $labels.",
            )
        }
    }

    fun overviewLines(overview: CycleOverview): List<String> = buildList {
        add(
            if (overview.shortest == overview.longest) {
                tr(
                    "Vos ${overview.records.size} derniers cycles ont tous duré ${days(overview.usualLength)}.",
                    "Your last ${overview.records.size} cycles all lasted ${days(overview.usualLength)}.",
                )
            } else {
                tr(
                    "Vos cycles durent en général ${days(overview.usualLength)}, de ${overview.shortest} à ${days(overview.longest)} sur les ${overview.records.size} derniers.",
                    "Your cycles usually last ${days(overview.usualLength)}, from ${overview.shortest} to ${days(overview.longest)} over the last ${overview.records.size}.",
                )
            },
        )
        overview.usualPeriodLength?.let { add(tr("Vos règles durent en général ${days(it)}.", "Your period usually lasts ${days(it)}.")) }
        add(
            if (overview.withinReferences) {
                tr("Ces durées sont dans les repères habituels.", "These lengths are within the usual range.")
            } else {
                tr(
                    "Certaines durées sortent des repères habituels. La fiche « Quand consulter » dit quand en parler.",
                    "Some lengths are outside the usual range. The \"When to see a doctor\" guide says when to bring it up.",
                )
            },
        )
    }

    fun predictionLine(overview: CycleOverview): String {
        val error = overview.typicalPredictionError
        return when {
            error == null -> tr(
                "Il faut encore quelques cycles pour mesurer la justesse des prévisions.",
                "A few more cycles are needed to measure how accurate the forecasts are.",
            )
            error == 0 -> tr(
                "Vos règles sont en général arrivées à la date estimée, sur ${overview.predictionsChecked} cycles vérifiés.",
                "Your period has usually arrived on the estimated date, over ${overview.predictionsChecked} cycles checked.",
            )
            else -> tr(
                "Vos règles sont en général arrivées à ${days(error)} de la date estimée, sur ${overview.predictionsChecked} cycles vérifiés. C'est pour cela que Lunaison affiche une plage de dates.",
                "Your period has usually arrived within ${days(error)} of the estimated date, over ${overview.predictionsChecked} cycles checked. That is why Lunaison shows a range of dates.",
            )
        }
    }

    fun onsetLine(onset: Onset): String = when (onset.daysBefore) {
        1 -> tr(
            "${onset.label} : en général la veille des règles (${onset.cycles} cycles).",
            "${onset.label}: usually the day before your period (${onset.cycles} cycles).",
        )
        else -> tr(
            "${onset.label} : en général ${days(onset.daysBefore)} avant les règles (${onset.cycles} cycles).",
            "${onset.label}: usually ${days(onset.daysBefore)} before your period (${onset.cycles} cycles).",
        )
    }

    val NOTHING_YET: String
        get() = tr(
            "Vos statistiques apparaîtront quand deux cycles complets auront été notés : il faut deux débuts de règles après le premier.",
            "Your statistics will appear once two complete cycles have been logged: two period starts are needed after the first one.",
        )

    val HOW_IT_WORKS: String
        get() = tr(
            "Calculé sur votre téléphone, à partir de ce que vous avez noté. Seuls les jours remplis comptent, et rien n'est affiché avant deux cycles. Ces chiffres décrivent vos cycles, ils ne posent pas de diagnostic.",
            "Calculated on your phone, from what you have logged. Only filled-in days count, and nothing is shown before two cycles. These figures describe your cycles; they do not make a diagnosis.",
        )
}
