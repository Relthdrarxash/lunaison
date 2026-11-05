package fr.lunaison.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.cycle.PhasePatterns
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

/** « Mes cycles » : ce que les données notées disent, calculé sur le téléphone. */
@Composable
fun InsightsScreen(
    state: AppState,
    onBack: () -> Unit,
    onArticle: (String) -> Unit,
    onHealthReport: () -> Unit = {},
) {
    val cycle = state.cycle as? CycleState.Known
    val ageGroup = state.settings.baseline.ageGroup
    val overview = remember(state.logs, cycle) { cycle?.let { CycleInsights.overview(state.logs, it, ageGroup) } }
    val onsets = remember(state.logs, cycle) { cycle?.let { CycleInsights.onsets(state.logs, it) }.orEmpty() }
    val duringPeriod = remember(state.logs, cycle) { cycle?.let { PhasePatterns.compute(state.logs, it, Phase.MENSTRUAL) } }
    val phasePatterns = remember(state.logs, cycle) { cycle?.let { CycleInsights.allPhasePatterns(state.logs, it) }.orEmpty() }
    val context = LocalContext.current

    Page {
        PageTitle(tr("Mes cycles", "My cycles"), onBack = onBack)

        if (cycle == null || overview == null) {
            LunaCard { Text(InsightTexts.NOTHING_YET, style = MaterialTheme.typography.bodyMedium) }
            Muted(InsightTexts.HOW_IT_WORKS)
            return@Page
        }

        LunaButton(
            tr("Bilan de santé pour un soignant", "Health report for a clinician"),
            onClick = onHealthReport,
            style = LunaButtonStyle.Outlined,
        )

        SectionTitle(tr("Durée de vos cycles", "Length of your cycles"))
        LunaCard {
            InsightTexts.overviewLines(overview).forEach { Bullet(it) }
            CycleBars(overview)
            if (!overview.withinReferences) {
                TextButton(onClick = { onArticle("consulter") }) { Text(tr("Quand consulter", "When to see a doctor")) }
            }
        }

        SectionTitle(tr("Vos prévisions", "Your forecasts"))
        LunaCard {
            Text(InsightTexts.predictionLine(overview), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onArticle("previsions") }) { Text(tr("Ce que les prévisions peuvent dire", "What predictions can tell you")) }
        }

        if (onsets.isNotEmpty()) {
            SectionTitle(tr("Avant vos règles", "Before your period"))
            LunaCard { onsets.forEach { Bullet(InsightTexts.onsetLine(it)) } }
        }

        if (duringPeriod != null && !duringPeriod.isEmpty) {
            SectionTitle(tr("Pendant vos règles", "During your period"))
            LunaCard {
                Text(tr("Lors de vos dernières règles, vous avez souvent noté :", "During your recent periods, you often logged:"), style = MaterialTheme.typography.bodyMedium)
                duringPeriod.symptoms.forEach { (s, n) -> Bullet(tr("${s.label} (${n} cycles sur ${duringPeriod.cycles})", "${s.label} (${n} of ${duringPeriod.cycles} cycles)")) }
                duringPeriod.moods.forEach { (m, n) -> Bullet(tr("${m.label} (${n} cycles sur ${duringPeriod.cycles})", "${m.label} (${n} of ${duringPeriod.cycles} cycles)")) }
            }
        }

        if (phasePatterns.isNotEmpty()) {
            SectionTitle(tr("Votre cycle, phase par phase", "Your cycle, phase by phase"))
            phasePatterns.forEach { pattern ->
                LunaCard {
                    Text(Format.phaseName(pattern.phase), style = MaterialTheme.typography.titleMedium)
                    Text(
                        tr("Revenu lors de vos derniers cycles pendant cette phase :", "Observed across your recent cycles during this phase:"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    pattern.symptoms.forEach { (s, n) ->
                        Bullet(tr("${s.label} ($n cycles sur ${pattern.cycles})", "${s.label} ($n of ${pattern.cycles} cycles)"))
                    }
                    pattern.moods.forEach { (m, n) ->
                        Bullet(tr("Humeur : ${m.label.lowercase()} ($n cycles sur ${pattern.cycles})", "Mood: ${m.label.lowercase()} ($n of ${pattern.cycles} cycles)"))
                    }
                }
            }
        }

        val painful = overview.records.filter { it.periodLength != null }
        if (painful.any { it.painDays > 0 }) {
            SectionTitle(tr("Douleurs pendant les règles", "Pain during your period"))
            LunaCard {
                Text(tr("Jours de douleur gênante ou forte, des règles les plus anciennes aux plus récentes :", "Days of bothersome or severe pain, from the oldest period to the most recent:"), style = MaterialTheme.typography.bodyMedium)
                Text(painful.joinToString("  ·  ") { tr("${Format.dayMonth(it.start)} : ${it.painDays}", "${Format.dayMonth(it.start)}: ${it.painDays}") }, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { onArticle("douleurs") }) { Text(tr("Soulager les douleurs", "Relieving pain")) }
            }
        }

        Muted(InsightTexts.HOW_IT_WORKS)
        TextButton(onClick = { openLink(context, Links.SOURCES) }) { Text(tr("Comment c'est calculé", "How it is calculated")) }
    }
}

/** Une barre par cycle, proportionnelle à sa durée. Chaque barre est lue par le lecteur d'écran. */
@Composable
private fun CycleBars(overview: CycleOverview) {
    val max = overview.longest.toFloat()
    overview.records.forEach { record ->
        Row(
            Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = tr("Cycle du ${Format.dayMonth(record.start)} : ${Format.days(record.length)}", "Cycle from ${Format.dayMonth(record.start)}: ${Format.days(record.length)}") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(Format.dayMonth(record.start), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(84.dp))
            Box(Modifier.weight(1f).height(14.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(record.length / max)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(7.dp)),
                )
            }
            Text(tr("${record.length} j", "${record.length} d"), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
        }
    }
}
