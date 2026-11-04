package fr.lunaison.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private enum class DayMark { PERIOD, SPOTTING, PREDICTED, NONE }

@Composable
fun CalendarScreen(state: AppState, onDay: (LocalDate) -> Unit) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.from(state.today).toString()) }
    val month = YearMonth.parse(monthText)
    val known = state.cycle as? CycleState.Known
    val predicted = remember(known) {
        known?.upcoming(3)?.flatMap { it.likelyDays() }?.toSet().orEmpty()
    }
    val loggedDates = remember(state.logs) { state.logs.associateBy { it.date } }

    fun markOf(date: LocalDate): DayMark {
        val log = loggedDates[date]
        return when {
            log?.flow?.isBleeding == true -> DayMark.PERIOD
            log?.flow != null -> DayMark.SPOTTING
            date.isAfter(state.today) && date in predicted -> DayMark.PREDICTED
            else -> DayMark.NONE
        }
    }

    Page {
        PageTitle("Calendrier", subtitle = "Touchez un jour passé pour le remplir.")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { monthText = month.minusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
            }
            Text(Format.monthYear(month.atDay(1)), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { monthText = month.plusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
            }
        }

        MonthGrid(month, state.today, ::markOf, loggedDates.keys, onDay)
        Legend()

        if (known != null) History(known)
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    markOf: (LocalDate) -> DayMark,
    logged: Set<LocalDate>,
    onDay: (LocalDate) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).clearAndSetSemantics { },
                )
            }
        }
        val first = month.atDay(1)
        val offset = first.dayOfWeek.value - DayOfWeek.MONDAY.value
        val cells = offset + month.lengthOfMonth()
        val rows = (cells + 6) / 7
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (column in 0 until 7) {
                    val index = row * 7 + column - offset
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (index in 0 until month.lengthOfMonth()) {
                            val date = first.plusDays(index.toLong())
                            DayCell(date, today, markOf(date), date in logged, onDay)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, today: LocalDate, mark: DayMark, logged: Boolean, onDay: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val clickable = !date.isAfter(today)
    val (background, foreground) = when (mark) {
        DayMark.PERIOD -> colors.secondary to colors.onSecondary
        DayMark.SPOTTING -> colors.secondaryContainer to colors.onSecondaryContainer
        DayMark.PREDICTED -> Color.Transparent to colors.onSurface
        DayMark.NONE -> Color.Transparent to if (clickable) colors.onSurface else colors.onSurfaceVariant
    }
    val description = buildString {
        append(Format.weekday(date))
        when (mark) {
            DayMark.PERIOD -> append(", règles")
            DayMark.SPOTTING -> append(", traces")
            DayMark.PREDICTED -> append(", règles probables")
            DayMark.NONE -> Unit
        }
        if (logged && mark == DayMark.NONE) append(", journée notée")
        if (date == today) append(", aujourd'hui")
    }
    var modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(CircleShape)
    if (mark == DayMark.PREDICTED) modifier = modifier.border(BorderStroke(2.dp, colors.secondary), CircleShape)
    if (date == today) modifier = modifier.border(BorderStroke(2.dp, colors.primary), CircleShape)
    if (clickable) modifier = modifier.clickable { onDay(date) }
    modifier = modifier.clearAndSetSemantics {
        contentDescription = description
        if (clickable) {
            role = Role.Button
            onClick { onDay(date); true }
        }
    }
    Surface(color = background, shape = CircleShape, modifier = modifier) {
        Box(contentAlignment = Alignment.Center) {
            Text("${date.dayOfMonth}", style = MaterialTheme.typography.labelMedium, color = foreground)
            if (logged && mark == DayMark.NONE) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(foreground),
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem("Règles") { Surface(color = colors.secondary, shape = CircleShape, modifier = Modifier.size(16.dp)) {} }
        LegendItem("Traces") { Surface(color = colors.secondaryContainer, shape = CircleShape, modifier = Modifier.size(16.dp)) {} }
        LegendItem("Prévues") {
            Box(Modifier.size(16.dp).border(BorderStroke(2.dp, colors.secondary), CircleShape))
        }
    }
}

@Composable
private fun LegendItem(label: String, swatch: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        swatch()
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun History(state: CycleState.Known) {
    val cycles = state.cycles.filter { it.isPlausible }.takeLast(6).reversed()
    SectionTitle("Vos cycles")
    LunaCard {
        if (cycles.isEmpty()) {
            Text("Dès que vous aurez noté deux débuts de règles, vos cycles apparaîtront ici.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(
                "Votre cycle dure en général ${Format.days(state.stats.cycleLength)}, et vos règles ${Format.days(state.stats.periodLength)}.",
                style = MaterialTheme.typography.bodyLarge,
            )
            cycles.forEach { cycle ->
                val period = state.periods.firstOrNull { it.start == cycle.start }
                val periodText = period?.let { ", règles de ${Format.days(it.length)}" }.orEmpty()
                Bullet("Depuis le ${Format.dayMonth(cycle.start)} : ${Format.days(cycle.length)}$periodText")
            }
            Muted("Un écart de quelques jours d'un cycle à l'autre est normal.")
        }
    }
}
