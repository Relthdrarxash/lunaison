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
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle

/** « Mes cycles » : ce que les données notées disent, calculé sur le téléphone. */
@Composable
fun InsightsScreen(state: AppState, onBack: () -> Unit, onArticle: (String) -> Unit) {
    val cycle = state.cycle as? CycleState.Known
    val ageGroup = state.settings.baseline.ageGroup
    val overview = remember(state.logs, cycle) { cycle?.let { CycleInsights.overview(state.logs, it, ageGroup) } }
    val onsets = remember(state.logs, cycle) { cycle?.let { CycleInsights.onsets(state.logs, it) }.orEmpty() }
    val duringPeriod = remember(state.logs, cycle) { cycle?.let { PhasePatterns.compute(state.logs, it, Phase.MENSTRUAL) } }
    val context = LocalContext.current

    Page {
        PageTitle("Mes cycles", onBack = onBack)

        if (cycle == null || overview == null) {
            LunaCard { Text(InsightTexts.NOTHING_YET, style = MaterialTheme.typography.bodyMedium) }
            Muted(InsightTexts.HOW_IT_WORKS)
            return@Page
        }

        SectionTitle("Durée de vos cycles")
        LunaCard {
            InsightTexts.overviewLines(overview).forEach { Bullet(it) }
            CycleBars(overview)
            if (!overview.withinReferences) {
                TextButton(onClick = { onArticle("consulter") }) { Text("Quand consulter") }
            }
        }

        SectionTitle("Vos prévisions")
        LunaCard {
            Text(InsightTexts.predictionLine(overview), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onArticle("previsions") }) { Text("Ce que les prévisions peuvent dire") }
        }

        if (onsets.isNotEmpty()) {
            SectionTitle("Avant vos règles")
            LunaCard { onsets.forEach { Bullet(InsightTexts.onsetLine(it)) } }
        }

        if (duringPeriod != null && !duringPeriod.isEmpty) {
            SectionTitle("Pendant vos règles")
            LunaCard {
                Text("Lors de vos dernières règles, vous avez souvent noté :", style = MaterialTheme.typography.bodyMedium)
                duringPeriod.symptoms.forEach { (s, n) -> Bullet("${s.label} (${n} cycles sur ${duringPeriod.cycles})") }
                duringPeriod.moods.forEach { (m, n) -> Bullet("${m.label} (${n} cycles sur ${duringPeriod.cycles})") }
            }
        }

        val painful = overview.records.filter { it.periodLength != null }
        if (painful.any { it.painDays > 0 }) {
            SectionTitle("Douleurs pendant les règles")
            LunaCard {
                Text("Jours de douleur gênante ou forte, des règles les plus anciennes aux plus récentes :", style = MaterialTheme.typography.bodyMedium)
                Text(painful.joinToString("  ·  ") { "${Format.dayMonth(it.start)} : ${it.painDays}" }, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { onArticle("douleurs") }) { Text("Soulager les douleurs") }
            }
        }

        Muted(InsightTexts.HOW_IT_WORKS)
        TextButton(onClick = { openLink(context, Links.SOURCES) }) { Text("Comment c'est calculé") }
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
                .clearAndSetSemantics { contentDescription = "Cycle du ${Format.dayMonth(record.start)} : ${Format.days(record.length)}" },
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
            Text("${record.length} j", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
        }
    }
}
