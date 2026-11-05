package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.DailyTip
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.content.PhaseGuides
import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.PhasePatterns
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.DayLog
import java.time.LocalDate
import fr.lunaison.core.model.tr

@Composable
fun HomeScreen(
    state: AppState,
    actions: Actions,
    onLog: (date: LocalDate, periodStart: Boolean) -> Unit,
    onArticle: (String) -> Unit,
    onShare: () -> Unit,
    onCalendar: () -> Unit,
    onInsights: () -> Unit = {},
) {
    val cycle = state.cycle
    val todayLog = state.logFor(state.today)
    Page {
        PageTitle(Format.weekday(state.today))

        when (cycle) {
            CycleState.Unknown -> LunaCard {
                Text(tr("Notez vos dernières règles pour commencer", "Log your last period to get started"), style = MaterialTheme.typography.titleLarge)
                Text(tr("Dès que Lunaison connaît le début de vos dernières règles, il estime les suivantes.", "As soon as Lunaison knows when your last period started, it estimates the next ones."), style = MaterialTheme.typography.bodyMedium)
                LunaButton(tr("Ouvrir le calendrier", "Open the calendar"), onClick = onCalendar)
            }
            is CycleState.Known -> CycleCard(cycle, onArticle)
        }

        // Actions du jour.
        val inPeriod = (cycle as? CycleState.Known)?.inPeriod == true
        if (!inPeriod && todayLog.flow?.isBleeding != true) {
            LunaButton(
                tr("Mes règles ont commencé", "My period has started"),
                onClick = {
                    actions.startPeriod(todayLog)
                    onLog(state.today, true)
                },
                style = LunaButtonStyle.Accent,
                icon = Icons.Filled.Add,
            )
        }
        LunaButton(
            if (todayLog.isEmpty) tr("Noter ma journée", "Log my day") else tr("Modifier ma journée", "Edit my day"),
            onClick = { onLog(state.today, false) },
            style = if (inPeriod) LunaButtonStyle.Primary else LunaButtonStyle.Outlined,
            icon = Icons.Filled.Edit,
        )
        if (!todayLog.isEmpty) TodaySummary(todayLog)

        if (state.settings.isAdult) {
            val partner = state.settings.partner
            LunaButton(
                if (partner != null) tr("Prévenir ${partner.name}", "Let ${partner.name} know") else tr("Partager avec un ou une partenaire", "Share with a partner"),
                onClick = onShare,
                style = LunaButtonStyle.Quiet,
                icon = Icons.Filled.Share,
            )
        }

        state.alerts.forEach { alert ->
            AlertCard(
                alert = alert,
                onRead = { onArticle(alert.articleId) },
                onDismiss = {
                    actions.updateSettings { it.copy(dismissedAlerts = it.dismissedAlerts + (alert to state.today)) }
                },
            )
        }

        state.recap?.let { recap ->
            RecapCard(
                recap = recap,
                onInsights = onInsights,
                onDismiss = { actions.updateSettings { it.copy(recapSeen = recap.record.start) } },
            )
        }

        if (cycle is CycleState.Known) {
            PhaseCard(state, cycle, onArticle)
            if (cycle.stats.completedCycles >= CycleConfig.MIN_CYCLES_FOR_STATS) {
                LunaButton(tr("Mes cycles", "My cycles"), onClick = onInsights, style = LunaButtonStyle.Quiet, icon = Icons.Filled.DateRange)
            }
        }
    }
}

@Composable
private fun RecapCard(recap: CycleRecap, onInsights: () -> Unit, onDismiss: () -> Unit) {
    LunaCard {
        Text(tr("Votre cycle qui vient de finir", "Your cycle that just ended"), style = MaterialTheme.typography.titleMedium)
        InsightTexts.recapLines(recap).forEach { Bullet(it) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide")) }
            TextButton(onClick = onInsights) { Text(tr("Mes cycles", "My cycles")) }
        }
    }
}

@Composable
private fun CycleCard(cycle: CycleState.Known, onArticle: (String) -> Unit) {
    val phaseName = Format.phaseName(cycle.phase)
    LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(84.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = tr("Jour ${cycle.dayOfCycle} du cycle", "Cycle day ${cycle.dayOfCycle}") },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tr("Jour", "Day"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text("${cycle.dayOfCycle}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
            Column {
                Text(phaseName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (!cycle.inPeriod) {
                    Text(tr("Phase estimée", "Estimated phase"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Text(Format.nextPeriod(cycle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            Text(Format.range(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(Format.confidence(cycle.stats.confidence), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            TextButton(onClick = { onArticle("previsions") }) {
                Text(tr("Pourquoi cette plage ?", "Why this range?"), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun TodaySummary(log: DayLog) {
    val parts = buildList {
        log.flow?.let { add(tr("Flux : ${it.label.lowercase()}", "Flow: ${it.label.lowercase()}")) }
        log.pain?.let { add(tr("Douleur : ${it.label.lowercase()}", "Pain: ${it.label.lowercase()}")) }
        if (log.symptoms.isNotEmpty()) add(log.symptoms.joinToString(", ") { it.label.lowercase() })
        if (log.moods.isNotEmpty()) add(tr("Humeur : ", "Mood: ") + log.moods.joinToString(", ") { it.label.lowercase() })
    }
    LunaCard {
        Text(tr("Noté aujourd'hui", "Logged today"), style = MaterialTheme.typography.titleMedium)
        parts.forEach { Bullet(it.replaceFirstChar { c -> c.uppercase() }) }
        if (log.note.isNotBlank()) Muted(log.note)
    }
}

@Composable
private fun AlertCard(alert: AlertKind, onRead: () -> Unit, onDismiss: () -> Unit) {
    LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(alert.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide")) }
            TextButton(onClick = onRead) { Text(tr("Quand consulter", "When to see a doctor")) }
        }
    }
}

@Composable
private fun PhaseCard(state: AppState, cycle: CycleState.Known, onArticle: (String) -> Unit) {
    val guide = PhaseGuides.forPhase(cycle.phase)
    val pattern = PhasePatterns.compute(state.logs, cycle, cycle.phase)
    val tip = DailyTip.pick(guide, state.today, state.logs.firstOrNull { it.date == state.today })
    SectionTitle(tr("Ce qui se passe en ce moment", "What is happening right now"))
    // Ce qui est propre à la personne passe avant les conseils généraux.
    if (!pattern.isEmpty) {
        LunaCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(tr("Ce qui revient chez vous", "What comes back for you"), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                tr("Pendant cette phase, lors de vos cycles précédents, vous avez souvent noté :", "During this phase, in your previous cycles, you often logged:"),
                style = MaterialTheme.typography.bodyMedium,
            )
            pattern.symptoms.forEach { (symptom, count) -> Bullet(tr("${symptom.label} (${count} cycles sur ${pattern.cycles})", "${symptom.label} (${count} of ${pattern.cycles} cycles)")) }
            pattern.moods.forEach { (mood, count) -> Bullet(tr("${mood.label} (${count} cycles sur ${pattern.cycles})", "${mood.label} (${count} of ${pattern.cycles} cycles)")) }
        }
    }
    LunaCard {
        Text(guide.name, style = MaterialTheme.typography.titleMedium)
        Text(guide.whatHappens, style = MaterialTheme.typography.bodyMedium)
        if (tip != null) {
            Text(tr("Le conseil du jour", "Tip of the day"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Text(tip.text, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { onArticle(guide.articleId) }) { Text(tr("En savoir plus", "Learn more")) }
    }
    state.anticipation?.let { coming ->
        Muted(InsightTexts.anticipationLine(coming))
    }
}
