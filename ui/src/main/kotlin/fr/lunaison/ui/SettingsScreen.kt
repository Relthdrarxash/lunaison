package fr.lunaison.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.ThemePalette
import kotlinx.coroutines.launch
import fr.lunaison.core.model.tr

private val ThemePalette.swatchColor: Color
    get() = when (this) {
        ThemePalette.PLUM -> Color(0xFF4A2C4F)
        ThemePalette.SAGE -> Color(0xFF2E5443)
        ThemePalette.OCEAN -> Color(0xFF234A63)
        ThemePalette.TERRACOTTA -> Color(0xFF6E3828)
    }

@Composable
fun SettingsScreen(
    state: AppState,
    actions: Actions,
    versionName: String,
    onArticle: (String) -> Unit,
    onHealthReport: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = state.settings
    val reminders = settings.reminders
    var confirmErase by rememberSaveable { mutableStateOf(false) }
    var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var importResultCount by rememberSaveable { mutableStateOf<Int?>(null) }

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

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                importResultCount = actions.importCsv(text)
            }
        }
    }

    Page {
        PageTitle(tr("Réglages", "Settings"))

        SectionTitle(tr("Rappels", "Reminders"))
        LunaCard {
            Text(tr("Règles et carnet", "Period & daily log"), style = MaterialTheme.typography.titleSmall)
            SwitchRow(
                tr("Prévenir avant mes règles", "Notify me before my period"),
                checked = reminders.periodReminder,
                onChange = { on ->
                    if (on) ensureNotifications()
                    actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminder = on)) }
                },
            )
            AnimatedVisibility(
                visible = reminders.periodReminder,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val days = reminders.periodReminderDaysBefore
                Stepper(
                    label = if (days == 0) tr("Le jour même", "On the day") else tr("${Format.days(days)} avant", "${Format.days(days)} before"),
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days - 1).coerceAtLeast(0))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days + 1).coerceAtMost(7))) } },
                )
            }
            Text(tr("Rappel pour noter ma journée", "Reminder to log my day"), style = MaterialTheme.typography.bodyMedium)
            ChoiceChips(
                options = LogReminderMode.entries,
                label = {
                    when (it) {
                        LogReminderMode.OFF -> tr("Jamais", "Never")
                        LogReminderMode.AROUND_PERIOD -> tr("Autour des règles", "Around my period")
                        LogReminderMode.DAILY -> tr("Tous les jours", "Every day")
                    }
                },
                isSelected = { it == reminders.logReminder },
                onToggle = { mode ->
                    if (mode != LogReminderMode.OFF) ensureNotifications()
                    actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminder = mode)) }
                },
            )
            AnimatedVisibility(
                visible = reminders.logReminder != LogReminderMode.OFF,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val time = reminders.logReminderTime
                Stepper(
                    label = tr("À ${time.hour} h", "At ${time.hour}:00"),
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.minusHours(1))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.plusHours(1))) } },
                )
            }
        }

        LunaCard {
            Text(tr("Contraception et traitements", "Contraception & medication"), style = MaterialTheme.typography.titleSmall)
            SwitchRow(
                tr("Rappel quotidien (pilule ou traitement)", "Daily reminder (pill or medication)"),
                checked = reminders.medicationReminder,
                onChange = { on ->
                    if (on) ensureNotifications()
                    actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationReminder = on)) }
                },
            )
            AnimatedVisibility(
                visible = reminders.medicationReminder,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val medTime = reminders.medicationReminderTime
                    Stepper(
                        label = tr("À ${medTime.hour} h", "At ${medTime.hour}:00"),
                        onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationReminderTime = medTime.minusHours(1))) } },
                        onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationReminderTime = medTime.plusHours(1))) } },
                    )
                    OutlinedTextField(
                        value = reminders.medicationLabel,
                        onValueChange = { text ->
                            actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationLabel = text.take(40))) }
                        },
                        label = { Text(tr("Nom du traitement ou contraceptif (facultatif)", "Medication or contraceptive name (optional)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Muted(tr("Sur l'écran verrouillé, les notifications n'affichent que « Lunaison ».", "On the lock screen, notifications only show \"Lunaison\"."))
        }

        SectionTitle(tr("Mon cycle", "My cycle"))
        LunaCard {
            Text(tr("Tranche d'âge", "Age group"), style = MaterialTheme.typography.titleSmall)
            ChoiceChips(
                options = AgeGroup.entries,
                label = { if (it == AgeGroup.UNDER_18) tr("Moins de 18 ans", "Under 18") else tr("18 ans ou plus", "18 or over") },
                isSelected = { it == settings.baseline.ageGroup },
                onToggle = { group ->
                    actions.updateSettings {
                        // Le partage est réservé aux adultes : on l'efface si la tranche change.
                        it.copy(baseline = it.baseline.copy(ageGroup = group), partner = if (group == AgeGroup.ADULT) it.partner else null)
                    }
                },
            )
            Muted(tr("Elle règle les repères de cycle normal et l'accès au partage.", "It sets the normal cycle ranges and access to sharing."))
            SwitchRow(
                tr("Afficher la fenêtre fertile estimée", "Show estimated fertile window"),
                checked = settings.showFertility,
                onChange = { on -> actions.updateSettings { it.copy(showFertility = on) } },
            )
            SwitchRow(
                tr("Suivre les signes d'ovulation (glaire cervicale, température)", "Track ovulation signs (cervical mucus, temperature)"),
                checked = settings.trackOvulationSigns,
                onChange = { on -> actions.updateSettings { it.copy(trackOvulationSigns = on) } },
            )
            Muted(tr("Permet de noter chaque jour l'aspect de la glaire et la température basale au réveil pour repérer l'ovulation.", "Allows logging cervical mucus and morning basal temperature to track ovulation signs."))
            SwitchRow(
                tr("Cycles naturellement irréguliers (périménopause, SOPK…)", "Naturally irregular cycles (perimenopause, PCOS…)"),
                checked = settings.irregularCyclesMode,
                onChange = { on -> actions.updateSettings { it.copy(irregularCyclesMode = on) } },
            )
            Muted(tr("Élargit les plages prévisionnelles sans alertes de retard anxiogènes (syndrome des ovaires polykystiques, arrêt de pilule, périménopause).", "Widens predicted ranges without alarming alerts (polycystic ovary syndrome, post-pill, perimenopause)."))
            SwitchRow(
                tr("Mettre les prévisions en pause (grossesse, allaitement, contraception)", "Pause predictions (pregnancy, breastfeeding, contraception)"),
                checked = settings.pausePredictions,
                onChange = { on -> actions.updateSettings { it.copy(pausePredictions = on) } },
            )
        }

        SectionTitle(tr("Apparence et sécurité", "Appearance and security"))
        LunaCard {
            Text(tr("Palette de couleurs", "Color palette"), style = MaterialTheme.typography.titleSmall)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemePalette.entries.forEach { palette ->
                    val selected = palette == settings.themePalette
                    FilterChip(
                        selected = selected,
                        onClick = { actions.updateSettings { it.copy(themePalette = palette) } },
                        label = {
                            Text(
                                palette.label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(vertical = 10.dp),
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(palette.swatchColor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            SwitchRow(
                tr("Verrouiller l'application par code à 4 chiffres", "Lock the app with a 4-digit PIN"),
                checked = settings.pinHash != null,
                onChange = { on ->
                    if (on) {
                        showPinSetup = true
                    } else {
                        actions.updateSettings { it.copy(pinHash = null) }
                    }
                },
            )
            Muted(tr("Quand le code est activé, l'aperçu de l'application est également masqué dans le sélecteur d'applications récentes.", "When the PIN is enabled, the app preview is also hidden in the recent apps switcher."))
        }

        SectionTitle(tr("Mes données", "My data"))
        LunaCard {
            Text(tr("Tout est enregistré sur ce téléphone, nulle part ailleurs. Lunaison n'a pas accès à Internet et ne fait pas de sauvegarde dans le nuage.", "Everything is saved on this phone, nowhere else. Lunaison has no Internet access and makes no cloud backup."), style = MaterialTheme.typography.bodyMedium)
            LunaButton(tr("Bilan de santé pour un soignant", "Health report for a clinician"), onClick = onHealthReport, style = LunaButtonStyle.Outlined)
            LunaButton(tr("Exporter (tableur)", "Export (spreadsheet)"), onClick = { exporter.launch("lunaison.csv") }, style = LunaButtonStyle.Outlined)
            LunaButton(
                tr("Importer une sauvegarde (CSV)", "Import a backup (CSV)"),
                onClick = { importer.launch(arrayOf("text/*", "application/csv", "*/*")) },
                style = LunaButtonStyle.Outlined,
            )
            importResultCount?.let { count ->
                Muted(tr("$count journées importées.", "$count days imported."))
            }
            LunaButton(tr("Tout effacer", "Erase everything"), onClick = { confirmErase = true }, style = LunaButtonStyle.Quiet, icon = Icons.Filled.Delete)
        }

        SectionTitle(tr("À propos", "About"))
        LunaCard {
            Text("Lunaison $versionName", style = MaterialTheme.typography.titleSmall)
            Text(tr("Libre (GPL-3.0), gratuit, sans publicité ni traceur. Ce n'est ni un dispositif médical ni une méthode de contraception.", "Free and open source (GPL-3.0), no ads, no trackers. It is neither a medical device nor a method of contraception."), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onArticle("previsions") }) { Text(tr("Ce que les prévisions peuvent dire", "What predictions can tell you")) }
            TextButton(onClick = { openLink(context, Links.PRIVACY) }) { Text(tr("Politique de confidentialité", "Privacy policy")) }
            TextButton(onClick = { openLink(context, Links.SOURCE) }) { Text(tr("Code source", "Source code")) }
            TextButton(onClick = { openLink(context, Links.CONTACT) }) { Text(tr("Nous écrire", "Contact us")) }
            TextButton(onClick = { openStore(context) }) {
                Text(
                    tr(
                        "Laisser un avis sur le Play Store",
                        "Leave a review on Google Play",
                        pl = "Wystaw opinię w Google Play",
                        uk = "Залишити відгук у Google Play",
                        es = "Dejar una opinión en Google Play",
                        de = "Bewertung im Play Store abgeben",
                    ),
                )
            }
        }
    }

    if (showPinSetup) {
        PinSetupDialog(
            onSavePin = { pin ->
                showPinSetup = false
                actions.updateSettings { it.copy(pinHash = AppSettings.hashPin(pin)) }
            },
            onDismiss = { showPinSetup = false },
        )
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(tr("Tout effacer ?", "Erase everything?")) },
            text = { Text(tr("Toutes vos journées et vos réglages seront supprimés de ce téléphone. Ce n'est pas réversible.", "All your days and settings will be deleted from this phone. This cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmErase = false
                    actions.eraseAll()
                }) { Text(tr("Effacer", "Erase")) }
            },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text(tr("Annuler", "Cancel")) } },
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
