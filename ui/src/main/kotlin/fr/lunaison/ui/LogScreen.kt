package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.CervicalMucus
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Libido
import fr.lunaison.core.model.MedicationKind
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt
import fr.lunaison.core.model.tr

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
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val savedLog = state.logFor(date)
    // Le brouillon repart de ce qui est enregistré à chaque changement de jour.
    var draft by remember(date) {
        val saved = state.logFor(date)
        mutableStateOf(if (periodStart && date == initialDate && saved.flow?.isBleeding != true) saved.copy(flow = Flow.MEDIUM) else saved)
    }

    Page {
        PageTitle(tr("Ma journée", "My day"), onBack = onBack)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { date = date.minusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = tr("Jour précédent", "Previous day"))
            }
            Text(
                Format.relativeDay(date, state.today),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { date = date.plusDays(1) }, enabled = date.isBefore(state.today)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = tr("Jour suivant", "Next day"))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(tr("Règles", "Period"))
            ChoiceChips(
                options = Flow.entries,
                label = { it.label },
                isSelected = { draft.flow == it },
                onToggle = { draft = draft.copy(flow = if (draft.flow == it) null else it) },
            )
            Muted(tr("De simples traces ne marquent pas le début des règles.", "Spotting alone does not mark the start of a period."))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(tr("Douleur", "Pain"))
            ChoiceChips(
                options = Pain.entries,
                label = { it.label },
                isSelected = { draft.pain == it },
                onToggle = { draft = draft.copy(pain = if (draft.pain == it) null else it) },
            )
            draft.pain?.let { Muted(it.description) }
        }

        var showAllSymptoms by remember { mutableStateOf(false) }
        val extraSymptoms = setOf(Symptom.PELVIC_PAIN, Symptom.HOT_FLASHES, Symptom.NIGHT_SWEATS, Symptom.BRAIN_FOG)
        val visibleSymptoms = if (state.settings.irregularCyclesMode || showAllSymptoms || draft.symptoms.any { it in extraSymptoms }) {
            Symptom.entries
        } else {
            Symptom.entries.filterNot { it in extraSymptoms }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(tr("Symptômes", "Symptoms"))
            ChoiceChips(
                options = visibleSymptoms,
                label = { it.label },
                isSelected = { it in draft.symptoms },
                onToggle = { draft = draft.copy(symptoms = draft.symptoms.toggle(it)) },
            )
            if (visibleSymptoms.size < Symptom.entries.size) {
                LunaButton(
                    tr("Plus de symptômes (périménopause, sommeil…)", "More symptoms (perimenopause, sleep…)"),
                    onClick = { showAllSymptoms = true },
                    style = LunaButtonStyle.Quiet,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(tr("Humeur", "Mood"))
            ChoiceChips(
                options = Mood.entries,
                label = { it.label },
                isSelected = { it in draft.moods },
                onToggle = { draft = draft.copy(moods = draft.moods.toggle(it)) },
            )
        }

        var showMeasurements by remember(date) {
            mutableStateOf(draft.weight != null || draft.medications.isNotEmpty() || draft.basalTemperature != null || draft.mucus != null)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val medName = state.settings.reminders.medicationLabel.trim()
            val title = if (medName.isNotEmpty()) tr("Pilule & contraception ($medName)", "Pill & contraception ($medName)") else tr("Pilule & contraception", "Pill & contraception")
            SectionTitle(title)
            ChoiceChips(
                options = PillStatus.entries,
                label = { it.label },
                isSelected = { draft.pillStatus == it },
                onToggle = {
                    val next = if (draft.pillStatus == it) null else it
                    draft = draft.copy(
                        pillStatus = next,
                        medicationTaken = next == PillStatus.TAKEN,
                    )
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(tr("Vie intime & désir", "Intimacy & desire"))
            ChoiceChips(
                options = IntimacyKind.entries,
                label = { it.label },
                isSelected = { it in draft.intimacy },
                onToggle = { draft = draft.copy(intimacy = draft.intimacy.toggle(it)) },
            )
            ChoiceChips(
                options = Libido.entries,
                label = { it.label },
                isSelected = { draft.libido == it },
                onToggle = { draft = draft.copy(libido = if (draft.libido == it) null else it) },
            )
        }

        val hasMeasurements = draft.weight != null || draft.medications.isNotEmpty() || draft.basalTemperature != null || draft.mucus != null
        val toggleLabel = if (showMeasurements) {
            tr("Masquer les options secondaires", "Hide secondary options")
        } else {
            val badge = if (hasMeasurements) tr(" (notées)", " (logged)") else ""
            tr("Plus d'options : poids, température, traitements…$badge", "More options: weight, temperature, medications…$badge")
        }
        LunaButton(
            toggleLabel,
            onClick = { showMeasurements = !showMeasurements },
            style = LunaButtonStyle.Quiet,
        )

        if (showMeasurements) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(tr("Médicaments pris", "Medications taken"))
                ChoiceChips(
                    options = MedicationKind.entries,
                    label = { it.label },
                    isSelected = { it in draft.medications },
                    onToggle = { draft = draft.copy(medications = draft.medications.toggle(it)) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(tr("Poids du jour", "Today's weight"))
                val w = draft.weight
                if (w == null) {
                    LunaButton(
                        tr("Noter mon poids (kg)", "Log weight (kg)"),
                        onClick = { draft = draft.copy(weight = 60.0f) },
                        style = LunaButtonStyle.Outlined,
                    )
                } else {
                    Stepper(
                        label = tr("Poids : ${"%.1f".format(Locale.FRENCH, w)} kg", "Weight: ${"%.1f".format(Locale.ENGLISH, w)} kg"),
                        onMinus = {
                            val next = ((w * 10).roundToInt() - 5).coerceAtLeast(300) / 10f
                            draft = draft.copy(weight = next)
                        },
                        onPlus = {
                            val next = ((w * 10).roundToInt() + 5).coerceAtMost(2500) / 10f
                            draft = draft.copy(weight = next)
                        },
                    )
                    LunaButton(
                        tr("Effacer le poids", "Clear weight"),
                        onClick = { draft = draft.copy(weight = null) },
                        style = LunaButtonStyle.Quiet,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(tr("Signes d'ovulation (glaire et température)", "Ovulation signs (mucus and temperature)"))
                ChoiceChips(
                    options = CervicalMucus.entries,
                    label = { it.label },
                    isSelected = { draft.mucus == it },
                    onToggle = { draft = draft.copy(mucus = if (draft.mucus == it) null else it) },
                )
                val temp = draft.basalTemperature
                if (temp == null) {
                    LunaButton(
                        tr("Noter ma température au réveil", "Log waking basal temperature"),
                        onClick = { draft = draft.copy(basalTemperature = 36.5f) },
                        style = LunaButtonStyle.Outlined,
                    )
                } else {
                    Stepper(
                        label = tr("Température : ${"%.1f".format(Locale.FRENCH, temp)} °C", "Temperature: ${"%.1f".format(Locale.ENGLISH, temp)} °C"),
                        onMinus = {
                            val next = ((temp * 10).roundToInt() - 1).coerceAtLeast(355) / 10f
                            draft = draft.copy(basalTemperature = next)
                        },
                        onPlus = {
                            val next = ((temp * 10).roundToInt() + 1).coerceAtMost(385) / 10f
                            draft = draft.copy(basalTemperature = next)
                        },
                    )
                    LunaButton(
                        tr("Effacer la température", "Clear temperature"),
                        onClick = { draft = draft.copy(basalTemperature = null) },
                        style = LunaButtonStyle.Quiet,
                    )
                }
            }
        }

        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.copy(note = it.take(500)) },
            label = { Text(tr("Note personnelle (facultatif)", "Personal note (optional)")) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        LunaButton(tr("Enregistrer", "Save"), onClick = { onSave(draft) }, icon = Icons.Filled.Check)
        if (!savedLog.isEmpty) {
            LunaButton(
                tr(
                    "Effacer cette journée",
                    "Clear this day",
                    pl = "Wyczyść ten dzień",
                    uk = "Очистити цей день",
                    es = "Borrar este día",
                    de = "Diesen Tag löschen",
                ),
                onClick = { confirmClear = true },
                style = LunaButtonStyle.Quiet,
                icon = Icons.Filled.Delete,
            )
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = {
                Text(
                    tr(
                        "Effacer la saisie de ce jour ?",
                        "Clear this day's log?",
                        pl = "Wyczyścić wpis z tego dnia?",
                        uk = "Очистити запис за цей день?",
                        es = "¿Borrar el registro de este día?",
                        de = "Eintrag für diesen Tag löschen?",
                    ),
                )
            },
            text = {
                Text(
                    tr(
                        "Les règles, douleurs, symptômes et notes enregistrés pour cette date seront supprimés de vos cycles.",
                        "The period, pain, symptoms and notes recorded for this date will be removed from your cycles.",
                        pl = "Miesiączka, ból, objawy i notatki zapisane na ten dzień zostaną usunięte z Twoich cykli.",
                        uk = "Менструацію, біль, симптоми та нотатки, записані на цю дату, буде видалено з ваших циклів.",
                        es = "La menstruación, dolores, síntomas y notas registrados para esta fecha se eliminarán de tus ciclos.",
                        de = "Periode, Schmerzen, Symptome und Notizen für dieses Datum werden aus deinen Zyklen gelöscht.",
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onSave(DayLog(date))
                        onBack()
                    },
                ) {
                    Text(
                        tr("Effacer", "Clear", pl = "Wyczyść", uk = "Очистити", es = "Borrar", de = "Löschen"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"))
                }
            },
        )
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
