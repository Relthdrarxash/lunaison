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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import fr.lunaison.core.model.tr

private enum class DayMark { PERIOD, SPOTTING, PREDICTED, NONE }

@Composable
fun CalendarScreen(
    state: AppState,
    onDay: (LocalDate) -> Unit,
    onInsights: () -> Unit = {},
    onHealthReport: () -> Unit = {},
) {
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
        PageTitle(tr("Calendrier", "Calendar"), subtitle = tr("Touchez un jour pour le noter ou le modifier.", "Tap a day to log or edit it."))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LunaButton(
                tr("Analyse des cycles", "Cycle insights", "Analiza cykli", "Аналіз циклів", "Análisis de ciclos", "Zyklusanalyse"),
                onClick = onInsights,
                style = LunaButtonStyle.Quiet,
                modifier = Modifier.weight(1f),
            )
            LunaButton(
                tr("Bilan soignant", "Health report", "Raport medyczny", "Медичний звіт", "Informe médico", "Arztbericht"),
                onClick = onHealthReport,
                style = LunaButtonStyle.Quiet,
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { monthText = month.minusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = tr("Mois précédent", "Previous month"))
            }
            Text(Format.monthYear(month.atDay(1)), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { monthText = month.plusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = tr("Mois suivant", "Next month"))
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
            tr("LMMJVSD", "MTWTFSS").map { it.toString() }.forEach {
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
                    Box(Modifier.weight(1f).heightIn(min = 48.dp).aspectRatio(1f), contentAlignment = Alignment.Center) {
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
            DayMark.PERIOD -> append(tr(", règles", ", period"))
            DayMark.SPOTTING -> append(tr(", traces", ", spotting"))
            DayMark.PREDICTED -> append(tr(", règles probables", ", likely period"))
            DayMark.NONE -> Unit
        }
        if (logged && mark == DayMark.NONE) append(tr(", journée notée", ", day logged"))
        if (date == today) append(tr(", aujourd'hui", ", today"))
    }
    var modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(CircleShape)
    if (mark == DayMark.PREDICTED) modifier = modifier.border(BorderStroke(2.dp, colors.secondary), CircleShape)
    if (date == today) modifier = modifier.border(BorderStroke(2.dp, colors.primary), CircleShape)
    if (clickable) modifier = modifier.minimumInteractiveComponentSize().clickable { onDay(date) }
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
        LegendItem(tr("Règles", "Period")) { Surface(color = colors.secondary, shape = CircleShape, modifier = Modifier.size(16.dp)) {} }
        LegendItem(tr("Traces", "Spotting")) { Surface(color = colors.secondaryContainer, shape = CircleShape, modifier = Modifier.size(16.dp)) {} }
        LegendItem(tr("Prévues", "Predicted")) {
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
    SectionTitle(tr("Vos cycles", "Your cycles"))
    LunaCard {
        if (cycles.isEmpty()) {
            Text(tr("Dès que vous aurez noté deux débuts de règles, vos cycles apparaîtront ici.", "Once you have logged the start of two periods, your cycles will appear here."), style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(
                tr("Votre cycle dure en général ${Format.days(state.stats.cycleLength)}, et vos règles ${Format.days(state.stats.periodLength)}.", "Your cycle usually lasts ${Format.days(state.stats.cycleLength)}, and your period ${Format.days(state.stats.periodLength)}."),
                style = MaterialTheme.typography.bodyLarge,
            )
            cycles.forEach { cycle ->
                val period = state.periods.firstOrNull { it.start == cycle.start }
                val periodText = period?.let { tr(", règles de ${Format.days(it.length)}", ", period of ${Format.days(it.length)}") }.orEmpty()
                Bullet(tr("Depuis le ${Format.dayMonth(cycle.start)} : ${Format.days(cycle.length)}$periodText", "From ${Format.dayMonth(cycle.start)}: ${Format.days(cycle.length)}$periodText"))
            }
            Muted(tr("Un écart de quelques jours d'un cycle à l'autre est normal.", "A difference of a few days from one cycle to the next is normal."))
        }
    }
}
