package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.time.LocalDate

/**
 * Saisie d'une journée : tout est facultatif et se fait sans clavier, sauf la note.
 * On peut remplir n'importe quel jour passé, sans reproche pour les oublis.
 */
@Composable
fun LogScreen(
    state: AppState,
    initialDate: LocalDate,
    periodStart: Boolean,
    onSave: (DayLog) -> Unit,
    onBack: () -> Unit,
) {
    var date by remember { mutableStateOf(initialDate) }
    // Le brouillon repart de ce qui est enregistré à chaque changement de jour.
    var draft by remember(date) {
        val saved = state.logFor(date)
        mutableStateOf(if (periodStart && date == initialDate && saved.flow?.isBleeding != true) saved.copy(flow = Flow.MEDIUM) else saved)
    }

    Page {
        PageTitle("Ma journée", onBack = onBack)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { date = date.minusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Jour précédent")
            }
            Text(
                Format.relativeDay(date, state.today),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { date = date.plusDays(1) }, enabled = date.isBefore(state.today)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Jour suivant")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Règles")
            ChoiceChips(
                options = Flow.entries,
                label = { it.label },
                isSelected = { draft.flow == it },
                onToggle = { draft = draft.copy(flow = if (draft.flow == it) null else it) },
            )
            Muted("Des traces seules ne comptent pas comme le début des règles.")
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Douleur")
            ChoiceChips(
                options = Pain.entries,
                label = { it.label },
                isSelected = { draft.pain == it },
                onToggle = { draft = draft.copy(pain = if (draft.pain == it) null else it) },
            )
            draft.pain?.let { Muted(it.description) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Symptômes")
            ChoiceChips(
                options = Symptom.entries,
                label = { it.label },
                isSelected = { it in draft.symptoms },
                onToggle = { draft = draft.copy(symptoms = draft.symptoms.toggle(it)) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Humeur")
            ChoiceChips(
                options = Mood.entries,
                label = { it.label },
                isSelected = { it in draft.moods },
                onToggle = { draft = draft.copy(moods = draft.moods.toggle(it)) },
            )
        }

        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.copy(note = it.take(500)) },
            label = { Text("Note personnelle (facultatif)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        LunaButton("Enregistrer", onClick = { onSave(draft) }, icon = Icons.Filled.Check)
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
