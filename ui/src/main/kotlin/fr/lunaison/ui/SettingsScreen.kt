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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import fr.lunaison.core.cycle.LogReminderMode
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.data.InvalidPasswordException
import fr.lunaison.core.data.PeriodCalendarImporter
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
    var showImportSourceDialog by rememberSaveable { mutableStateOf(false) }
    var importResultMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingZipBytes by remember { mutableStateOf<ByteArray?>(null) }

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
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null || bytes.isEmpty()) {
                        importResultMessage = tr("Fichier vide ou inaccessible.", "Empty or inaccessible file.")
                        return@launch
                    }
                    if (PeriodCalendarImporter.isZip(bytes) && PeriodCalendarImporter.isEncryptedZip(bytes)) {
                        pendingZipBytes = bytes
                        passwordInput = ""
                        passwordError = null
                        showPasswordDialog = true
                    } else {
                        val summary = actions.importAuto(bytes, null)
                        importResultMessage = if (summary.cyclesCount > 0) {
                            tr(
                                "${summary.daysCount} journées et ${summary.cyclesCount} cycles importés depuis Calendrier.",
                                "${summary.daysCount} days and ${summary.cyclesCount} cycles imported from Calendar.",
                            )
                        } else {
                            tr("${summary.daysCount} journées importées.", "${summary.daysCount} days imported.")
                        }
                    }
                } catch (e: Exception) {
                    importResultMessage = tr("Erreur lors de l'import : ${e.localizedMessage}", "Import error: ${e.localizedMessage}")
                }
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
            TextButton(onClick = { onArticle("contraception") }) {
                Text(tr("Comprendre l'impact de ma contraception sur mes règles", "Understand how my contraception affects my periods"))
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LunaButton(
                    tr("Exporter (CSV)", "Export (CSV)"),
                    onClick = { exporter.launch("lunaison.csv") },
                    style = LunaButtonStyle.Outlined,
                    modifier = Modifier.weight(1f),
                )
                LunaButton(
                    tr("Importer", "Import"),
                    onClick = { showImportSourceDialog = true },
                    style = LunaButtonStyle.Outlined,
                    modifier = Modifier.weight(1f),
                )
            }
            importResultMessage?.let { msg ->
                Muted(msg)
            }
            LunaButton(tr("Tout effacer", "Erase everything"), onClick = { confirmErase = true }, style = LunaButtonStyle.Quiet, icon = Icons.Filled.Delete)
        }

        SectionTitle(
            tr(
                "Dans la même démarche",
                "In the same spirit",
                pl = "W tym samym duchu",
                uk = "У тому ж дусі",
                es = "En la misma línea",
                de = "Im gleichen Sinne",
            ),
        )
        LunaCard {
            Text(
                tr(
                    "Applications libres, 100 % hors-ligne et respectueuses de votre santé.",
                    "Free, 100% offline apps that respect your health and attention.",
                    pl = "Wolne, w 100% działające offline aplikacje szanujące Twoje zdrowie i uwagę.",
                    uk = "Вільні, на 100% офлайн-програми, що поважають ваше здоров'я та увагу.",
                    es = "Aplicaciones libres, 100 % sin conexión que respetan tu salud y atención.",
                    de = "Freie, 100 % Offline-Apps, die Ihre Gesundheit und Aufmerksamkeit respektieren.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CompanionAppRow(
                badge = "🌿",
                name = "Respire",
                description = tr(
                    "Pause de respiration & anti-doomscrolling, libre et sans compte",
                    "Breathing pause & anti-doomscrolling, free and offline",
                    pl = "Pauza oddechowa i ochrona przed doomscrollingiem, bez konta i offline",
                    uk = "Дихальна пауза та захист від думскролінгу, вільна та офлайн",
                    es = "Pausa de respiración y anti-doomscrolling, libre y sin conexión",
                    de = "Atempause & Anti-Doomscrolling, frei und offline",
                ),
                onClick = { openLink(context, "https://relthdrarxash.github.io/respire/") },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            CompanionAppRow(
                badge = "🛏️",
                name = "Sommelle",
                description = tr(
                    "Accompagnement de l'insomnie chronique par TCC-I, libre et sans compte",
                    "CBT-I accompaniment for chronic insomnia, free and offline",
                    pl = "Wsparcie w przewlekłej bezsenności metodą CBT-I, bez konta i offline",
                    uk = "Супровід при хронічному безсонні за методом КПТ-Б, вільна та офлайн",
                    es = "Acompañamiento del insomnio crónico con TCC-I, libre y sin conexión",
                    de = "KVT-I-Begleitung bei chronischer Schlaflosigkeit, frei und offline",
                ),
                onClick = { openLink(context, "https://relthdrarxash.github.io/sommelle/") },
            )
        }

        SectionTitle(tr("À propos", "About"))
        LunaCard {
            Text("Lunaison $versionName", style = MaterialTheme.typography.titleSmall)
            Text(
                tr(
                    "100 % hors-ligne • Zéro pisteur, zéro compte • Code source libre et auditable",
                    "100% offline • Zero trackers, zero account • Open and auditable source code",
                    pl = "100% offline • Bez trackerów, bez konta • Otwarty i audytowalny kod źródłowy",
                    uk = "100% офлайн • Без трекерів, без облікового запису • Відкритий вихідний код",
                    es = "100 % sin conexión • Cero rastreadores, cero cuentas • Código abierto y auditable",
                    de = "100 % offline • Keine Tracker, kein Konto • Freier und auditierbarer Quellcode",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                tr(
                    "Libre (GPL-3.0), gratuit, sans publicité. Ce n'est ni un dispositif médical ni une méthode de contraception.",
                    "Free and open source (GPL-3.0), no ads. It is neither a medical device nor a method of contraception.",
                    pl = "Wolne i otwarte (GPL-3.0), darmowe, bez reklam. Nie jest to wyrób medyczny ani metoda antykoncepcji.",
                    uk = "Вільне та відкрите (GPL-3.0), без реклами. Це не медичний виріб і не метод контрацепції.",
                    es = "Libre y abierto (GPL-3.0), gratuito, sin anuncios. No es un producto sanitario ni un método anticonceptivo.",
                    de = "Frei und quelloffen (GPL-3.0), kostenlos, werbefrei. Weder Medizinprodukt noch Verhütungsmethode.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onArticle("previsions") }, modifier = Modifier.weight(1f)) {
                    Text(tr("Prévisions", "Predictions"), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { openLink(context, Links.PRIVACY) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Confidentialité", "Privacy"), style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { openLink(context, Links.SOURCE) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Code source", "Source code"), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { openLink(context, Links.CONTACT) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Nous écrire", "Contact us"), style = MaterialTheme.typography.labelMedium)
                }
            }
            TextButton(onClick = { openStore(context) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    tr(
                        "Laisser un avis sur le Play Store",
                        "Leave a review on Google Play",
                        pl = "Wystaw opinię w Google Play",
                        uk = "Залишити відгук у Google Play",
                        es = "Dejar una opinión en Google Play",
                        de = "Bewertung im Play Store abgeben",
                    ),
                    style = MaterialTheme.typography.labelMedium,
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

    if (showImportSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImportSourceDialog = false },
            title = { Text(tr("Importer des données", "Import data")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        tr(
                            "De quelle application souhaitez-vous récupérer votre historique ?",
                            "Which app do you want to import your history from?",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Card(
                        onClick = {
                            showImportSourceDialog = false
                            importer.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                tr("Calendrier des règles", "Period Calendar"),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                tr(
                                    "Archive .zip (chiffrée par mot de passe) ou export .json de l'application.",
                                    ".zip archive (password-protected) or .json export from the app.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Card(
                        onClick = {
                            showImportSourceDialog = false
                            importer.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv", "*/*"))
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                tr("Lunaison ou tableur (CSV)", "Lunaison or spreadsheet (CSV)"),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                tr(
                                    "Fichier .csv de sauvegarde exporté depuis Lunaison ou un tableur.",
                                    ".csv backup file exported from Lunaison or a spreadsheet.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text(
                        tr(
                            "D'autres applications (Clue, Flo...) seront ajoutées dans les prochaines mises à jour.",
                            "Other applications (Clue, Flo...) will be added in upcoming updates.",
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportSourceDialog = false }) {
                    Text(tr("Annuler", "Cancel"))
                }
            },
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                pendingZipBytes = null
            },
            title = { Text(tr("Sauvegarde protégée", "Protected backup")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        tr(
                            "Cette sauvegarde de l'application « Calendrier » est chiffrée. Entrez le mot de passe fourni lors de l'exportation :",
                            "This backup from the \"Calendar\" app is encrypted. Enter the password provided during export:",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text(tr("Mot de passe", "Password")) },
                        singleLine = true,
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (passwordError != null) {
                        Text(
                            passwordError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bytes = pendingZipBytes
                        if (bytes != null) {
                            scope.launch {
                                try {
                                    val summary = actions.importAuto(bytes, passwordInput.trim())
                                    showPasswordDialog = false
                                    pendingZipBytes = null
                                    importResultMessage = if (summary.cyclesCount > 0) {
                                        tr(
                                            "${summary.daysCount} journées et ${summary.cyclesCount} cycles importés depuis Calendrier.",
                                            "${summary.daysCount} days and ${summary.cyclesCount} cycles imported from Calendar.",
                                        )
                                    } else {
                                        tr("${summary.daysCount} journées importées.", "${summary.daysCount} days imported.")
                                    }
                                } catch (e: InvalidPasswordException) {
                                    passwordError = tr("Mot de passe incorrect.", "Incorrect password.")
                                } catch (e: Exception) {
                                    passwordError = tr("Erreur : ${e.localizedMessage}", "Error: ${e.localizedMessage}")
                                }
                            }
                        }
                    },
                ) {
                    Text(tr("Déchiffrer et importer", "Decrypt & import"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        pendingZipBytes = null
                    },
                ) {
                    Text(tr("Annuler", "Cancel"))
                }
            },
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

@Composable
private fun CompanionAppRow(
    badge: String,
    name: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, fontSize = 18.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
