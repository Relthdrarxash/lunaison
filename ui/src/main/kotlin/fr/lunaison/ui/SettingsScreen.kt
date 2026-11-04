package fr.lunaison.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.AgeGroup
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(state: AppState, actions: Actions, versionName: String, onArticle: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = state.settings
    val reminders = settings.reminders
    var confirmErase by rememberSaveable { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun ensureNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch {
                val csv = actions.exportCsv()
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
            }
        }
    }

    Page {
        PageTitle("Réglages")

        SectionTitle("Rappels")
        LunaCard {
            SwitchRow(
                "Prévenir avant mes règles",
                checked = reminders.periodReminder,
                onChange = { on ->
                    if (on) ensureNotifications()
                    actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminder = on)) }
                },
            )
            if (reminders.periodReminder) {
                val days = reminders.periodReminderDaysBefore
                Stepper(
                    label = if (days == 0) "Le jour même" else "${Format.days(days)} avant",
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days - 1).coerceAtLeast(0))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days + 1).coerceAtMost(7))) } },
                )
            }
            Text("Rappel pour noter ma journée", style = MaterialTheme.typography.titleSmall)
            ChoiceChips(
                options = LogReminderMode.entries,
                label = {
                    when (it) {
                        LogReminderMode.OFF -> "Jamais"
                        LogReminderMode.AROUND_PERIOD -> "Autour des règles"
                        LogReminderMode.DAILY -> "Tous les jours"
                    }
                },
                isSelected = { it == reminders.logReminder },
                onToggle = { mode ->
                    if (mode != LogReminderMode.OFF) ensureNotifications()
                    actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminder = mode)) }
                },
            )
            if (reminders.logReminder != LogReminderMode.OFF) {
                val time = reminders.logReminderTime
                Stepper(
                    label = "À ${time.hour} h",
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.minusHours(1))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.plusHours(1))) } },
                )
            }
            Muted("Sur l'écran verrouillé, les notifications n'affichent que « Lunaison ».")
        }

        SectionTitle("Mon cycle")
        LunaCard {
            Text("Tranche d'âge", style = MaterialTheme.typography.titleSmall)
            ChoiceChips(
                options = AgeGroup.entries,
                label = { if (it == AgeGroup.UNDER_18) "Moins de 18 ans" else "18 ans ou plus" },
                isSelected = { it == settings.baseline.ageGroup },
                onToggle = { group ->
                    actions.updateSettings {
                        // Le partage est réservé aux adultes : on l'efface si la tranche change.
                        it.copy(baseline = it.baseline.copy(ageGroup = group), partner = if (group == AgeGroup.ADULT) it.partner else null)
                    }
                },
            )
            Muted("Elle règle les repères de cycle normal et l'accès au partage.")
        }

        SectionTitle("Mes données")
        LunaCard {
            Text("Tout est enregistré sur ce téléphone, nulle part ailleurs. Lunaison n'a pas accès à Internet et ne fait pas de sauvegarde dans le nuage.", style = MaterialTheme.typography.bodyMedium)
            LunaButton("Exporter (tableur)", onClick = { exporter.launch("lunaison.csv") }, style = LunaButtonStyle.Outlined)
            LunaButton("Tout effacer", onClick = { confirmErase = true }, style = LunaButtonStyle.Quiet, icon = Icons.Filled.Delete)
        }

        SectionTitle("À propos")
        LunaCard {
            Text("Lunaison $versionName", style = MaterialTheme.typography.titleSmall)
            Text("Libre (GPL-3.0), gratuit, sans publicité ni traceur. Ce n'est ni un dispositif médical ni une méthode de contraception.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onArticle("previsions") }) { Text("Ce que les prévisions peuvent dire") }
            TextButton(onClick = { openLink(context, Links.PRIVACY) }) { Text("Politique de confidentialité") }
            TextButton(onClick = { openLink(context, Links.SOURCE) }) { Text("Code source") }
            TextButton(onClick = { openLink(context, Links.CONTACT) }) { Text("Nous écrire") }
        }
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text("Tout effacer ?") },
            text = { Text("Toutes vos journées et vos réglages seront supprimés de ce téléphone. Ce n'est pas réversible.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmErase = false
                    actions.eraseAll()
                }) { Text("Effacer") }
            },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = label })
    }
}
