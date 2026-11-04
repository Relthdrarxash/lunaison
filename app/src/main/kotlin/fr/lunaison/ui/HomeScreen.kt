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
import fr.lunaison.core.content.PhaseGuides
import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.PhasePatterns
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.DayLog
import java.time.LocalDate

@Composable
fun HomeScreen(
    state: AppState,
    actions: Actions,
    onLog: (date: LocalDate, periodStart: Boolean) -> Unit,
    onArticle: (String) -> Unit,
    onShare: () -> Unit,
    onCalendar: () -> Unit,
) {
    val cycle = state.cycle
    val todayLog = state.logFor(state.today)
    Page {
        PageTitle(Format.weekday(state.today))

        when (cycle) {
            CycleState.Unknown -> LunaCard {
                Text("Notez vos dernières règles pour commencer", style = MaterialTheme.typography.titleLarge)
                Text("Dès que Lunaison connaît le début de vos dernières règles, il estime les suivantes.", style = MaterialTheme.typography.bodyMedium)
                LunaButton("Ouvrir le calendrier", onClick = onCalendar)
            }
            is CycleState.Known -> CycleCard(cycle)
        }

        // Actions du jour.
        val inPeriod = (cycle as? CycleState.Known)?.inPeriod == true
        if (!inPeriod && todayLog.flow?.isBleeding != true) {
            LunaButton(
                "Mes règles ont commencé",
                onClick = {
                    actions.startPeriod(todayLog)
                    onLog(state.today, true)
                },
                style = LunaButtonStyle.Accent,
                icon = Icons.Filled.Add,
            )
        }
        LunaButton(
            if (todayLog.isEmpty) "Noter ma journée" else "Modifier ma journée",
            onClick = { onLog(state.today, false) },
            style = if (inPeriod) LunaButtonStyle.Primary else LunaButtonStyle.Outlined,
            icon = Icons.Filled.Edit,
        )
        if (!todayLog.isEmpty) TodaySummary(todayLog)

        if (state.settings.isAdult) {
            val partner = state.settings.partner
            LunaButton(
                if (partner != null) "Prévenir ${partner.name}" else "Partager avec un ou une partenaire",
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

        if (cycle is CycleState.Known) {
            PhaseCard(state, cycle, onArticle)
        }
    }
}

@Composable
private fun CycleCard(cycle: CycleState.Known) {
    val phaseName = Format.phaseName(cycle.phase)
    LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(84.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Jour ${cycle.dayOfCycle} du cycle" },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jour", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text("${cycle.dayOfCycle}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
            Column {
                Text(phaseName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Phase estimée", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Text(Format.nextPeriod(cycle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            Text(Format.range(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(Format.confidence(cycle.stats.confidence), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun TodaySummary(log: DayLog) {
    val parts = buildList {
        log.flow?.let { add("Flux : ${it.label.lowercase()}") }
        log.pain?.let { add("Douleur : ${it.label.lowercase()}") }
        if (log.symptoms.isNotEmpty()) add(log.symptoms.joinToString(", ") { it.label.lowercase() })
        if (log.moods.isNotEmpty()) add("Humeur : " + log.moods.joinToString(", ") { it.label.lowercase() })
    }
    LunaCard {
        Text("Noté aujourd'hui", style = MaterialTheme.typography.titleMedium)
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
            TextButton(onClick = onDismiss) { Text("Masquer") }
            TextButton(onClick = onRead) { Text("Quand consulter") }
        }
    }
}

@Composable
private fun PhaseCard(state: AppState, cycle: CycleState.Known, onArticle: (String) -> Unit) {
    val guide = PhaseGuides.forPhase(cycle.phase)
    val pattern = PhasePatterns.compute(state.logs, cycle, cycle.phase)
    SectionTitle("Ce qui se passe en ce moment")
    LunaCard {
        Text(guide.name, style = MaterialTheme.typography.titleMedium)
        Text(guide.whatHappens, style = MaterialTheme.typography.bodyMedium)
        guide.tips.forEach { Bullet(it) }
        TextButton(onClick = { onArticle(guide.articleId) }) { Text("En savoir plus") }
    }
    if (!pattern.isEmpty) {
        LunaCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("Ce qui revient chez vous", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Pendant cette phase, lors de vos cycles précédents, vous avez souvent noté :",
                style = MaterialTheme.typography.bodyMedium,
            )
            pattern.symptoms.forEach { (symptom, count) -> Bullet("${symptom.label} (${count} cycles sur ${pattern.cycles})") }
            pattern.moods.forEach { (mood, count) -> Bullet("${mood.label} (${count} cycles sur ${pattern.cycles})") }
        }
    }
}
