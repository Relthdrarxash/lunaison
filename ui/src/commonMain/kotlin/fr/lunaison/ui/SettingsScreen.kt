package fr.lunaison.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import fr.lunaison.core.model.plusHours
import fr.lunaison.core.model.minusHours

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
    onReplayWelcome: () -> Unit = {},
) {
    val context = rememberPlatformContext()
    val scope = rememberCoroutineScope()
    val settings = state.settings
    val reminders = settings.reminders
    var confirmErase by rememberSaveable { mutableStateOf(false) }
    var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var showImportSourceDialog by rememberSaveable { mutableStateOf(false) }
    var importResultMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val notifications = rememberNotificationAccess()
    fun ensureNotifications() {
        if (!notifications.allowed) notifications.request()
    }

    val exportCsv = rememberFileExporter("text/csv") { actions.exportCsv() }

    Page {
        PageTitle(tr("Réglages", "Settings", pl = "Ustawienia", uk = "Налаштування", es = "Ajustes", de = "Einstellungen"))

        SectionTitle(tr("Rappels", "Reminders", pl = "Przypomnienia", uk = "Нагадування", es = "Recordatorios", de = "Erinnerungen"))
        LunaCard {
            Text(tr("Règles et carnet", "Period & daily log", pl = "Miesiączka i dziennik", uk = "Менструація й щоденник", es = "Regla y registro diario", de = "Periode und Tagebuch"), style = MaterialTheme.typography.titleSmall)
            SwitchRow(
                tr("Prévenir avant mes règles", "Notify me before my period", pl = "Powiadom mnie przed miesiączką", uk = "Попереджати перед менструацією", es = "Avisarme antes de mi regla", de = "Vor meiner Periode benachrichtigen"),
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
                    label = if (days == 0) tr("Le jour même", "On the day", pl = "Tego samego dnia", uk = "Того самого дня", es = "El mismo día", de = "Am selben Tag") else tr("${Format.days(days)} avant", "${Format.days(days)} before", pl = "${Format.days(days)} wcześniej", uk = "За ${Format.days(days)} до", es = "${Format.days(days)} antes", de = "${Format.days(days)} vorher"),
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days - 1).coerceAtLeast(0))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(periodReminderDaysBefore = (days + 1).coerceAtMost(7))) } },
                )
            }
            Text(tr("Rappel pour noter ma journée", "Reminder to log my day", pl = "Przypomnienie o zapisaniu dnia", uk = "Нагадування записати мій день", es = "Recordatorio para anotar mi día", de = "Erinnerung, meinen Tag einzutragen"), style = MaterialTheme.typography.bodyMedium)
            ChoiceChips(
                options = LogReminderMode.entries,
                label = {
                    when (it) {
                        LogReminderMode.OFF -> tr("Jamais", "Never", pl = "Nigdy", uk = "Ніколи", es = "Nunca", de = "Nie")
                        LogReminderMode.AROUND_PERIOD -> tr("Autour des règles", "Around my period", pl = "Wokół miesiączki", uk = "Навколо менструації", es = "Alrededor de la regla", de = "Rund um die Periode")
                        LogReminderMode.DAILY -> tr("Tous les jours", "Every day", pl = "Codziennie", uk = "Щодня", es = "Todos los días", de = "Jeden Tag")
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
                    label = tr("À ${time.hour} h", "At ${time.hour}:00", pl = "O ${time.hour}:00", uk = "О ${time.hour}:00", es = "${time.hour}:00 h", de = "Um ${time.hour}:00 Uhr"),
                    onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.minusHours(1))) } },
                    onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(logReminderTime = time.plusHours(1))) } },
                )
            }
        }

        LunaCard {
            Text(tr("Contraception et traitements", "Contraception & medication", pl = "Antykoncepcja i leczenie", uk = "Контрацепція та лікування", es = "Anticoncepción y tratamientos", de = "Verhütung und Medikamente"), style = MaterialTheme.typography.titleSmall)
            SwitchRow(
                tr("Rappel quotidien (pilule ou traitement)", "Daily reminder (pill or medication)", pl = "Codzienne przypomnienie (tabletka lub lek)", uk = "Щоденне нагадування (таблетка або ліки)", es = "Recordatorio diario (píldora o tratamiento)", de = "Tägliche Erinnerung (Pille oder Medikament)"),
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
                        label = tr("À ${medTime.hour} h", "At ${medTime.hour}:00", pl = "O ${medTime.hour}:00", uk = "О ${medTime.hour}:00", es = "${medTime.hour}:00 h", de = "Um ${medTime.hour}:00 Uhr"),
                        onMinus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationReminderTime = medTime.minusHours(1))) } },
                        onPlus = { actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationReminderTime = medTime.plusHours(1))) } },
                    )
                    OutlinedTextField(
                        value = reminders.medicationLabel,
                        onValueChange = { text ->
                            actions.updateSettings { it.copy(reminders = it.reminders.copy(medicationLabel = text.take(40))) }
                        },
                        label = { Text(tr("Nom du traitement ou contraceptif (facultatif)", "Medication or contraceptive name (optional)", pl = "Nazwa leku lub środka antykoncepcyjnego (opcjonalnie)", uk = "Назва ліків або контрацептиву (необов'язково)", es = "Nombre del tratamiento o anticonceptivo (opcional)", de = "Name des Medikaments oder Verhütungsmittels (optional)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            TextButton(onClick = { onArticle("contraception") }) {
                Text(tr("Comprendre l'impact de ma contraception sur mes règles", "Understand how my contraception affects my periods", pl = "Zrozumieć wpływ mojej antykoncepcji na miesiączki", uk = "Зрозуміти вплив моєї контрацепції на менструації", es = "Entender cómo afecta mi anticoncepción a mis reglas", de = "Verstehen, wie meine Verhütung meine Periode beeinflusst"))
            }
            Muted(tr("Sur l'écran verrouillé, les notifications n'affichent que « Lunaison ».", "On the lock screen, notifications only show \"Lunaison\".", pl = "Na ekranie blokady powiadomienia pokazują tylko „Lunaison”.", uk = "На заблокованому екрані сповіщення показують лише «Lunaison».", es = "En la pantalla de bloqueo, las notificaciones solo muestran «Lunaison».", de = "Auf dem Sperrbildschirm zeigen Benachrichtigungen nur „Lunaison“ an."))
        }

        SectionTitle(tr("Mon cycle", "My cycle", pl = "Mój cykl", uk = "Мій цикл", es = "Mi ciclo", de = "Mein Zyklus"))
        LunaCard {
            Text(tr("Tranche d'âge", "Age group", pl = "Grupa wiekowa", uk = "Вікова група", es = "Grupo de edad", de = "Altersgruppe"), style = MaterialTheme.typography.titleSmall)
            ChoiceChips(
                options = AgeGroup.entries,
                label = { if (it == AgeGroup.UNDER_18) tr("Moins de 18 ans", "Under 18", pl = "Poniżej 18 lat", uk = "Молодше 18 років", es = "Menos de 18 años", de = "Unter 18") else tr("18 ans ou plus", "18 or over", pl = "18 lat lub więcej", uk = "18 років або більше", es = "18 años o más", de = "18 oder älter") },
                isSelected = { it == settings.baseline.ageGroup },
                onToggle = { group ->
                    actions.updateSettings {
                        // Le partage est réservé aux adultes : on l'efface si la tranche change.
                        it.copy(baseline = it.baseline.copy(ageGroup = group), partner = if (group == AgeGroup.ADULT) it.partner else null)
                    }
                },
            )
            Muted(tr("Elle règle les repères de cycle normal et l'accès au partage.", "It sets the normal cycle ranges and access to sharing.", pl = "Określa typowe zakresy cyklu i dostęp do udostępniania.", uk = "Вона визначає звичні межі циклу та доступ до обміну.", es = "Define los intervalos habituales del ciclo y el acceso a compartir.", de = "Sie legt die üblichen Zyklusbereiche und den Zugang zum Teilen fest."))
            SwitchRow(
                tr("Afficher la fenêtre fertile estimée", "Show estimated fertile window", pl = "Pokaż szacowane dni płodne", uk = "Показувати орієнтовне фертильне вікно", es = "Mostrar la ventana fértil estimada", de = "Geschätztes fruchtbares Fenster anzeigen"),
                checked = settings.showFertility,
                onChange = { on -> actions.updateSettings { it.copy(showFertility = on) } },
            )
            SwitchRow(
                tr("Suivre les signes d'ovulation (glaire cervicale, température)", "Track ovulation signs (cervical mucus, temperature)", pl = "Śledź oznaki owulacji (śluz szyjkowy, temperatura)", uk = "Стежити за ознаками овуляції (цервікальний слиз, температура)", es = "Seguir los signos de ovulación (moco cervical, temperatura)", de = "Eisprungzeichen verfolgen (Zervixschleim, Temperatur)"),
                checked = settings.trackOvulationSigns,
                onChange = { on -> actions.updateSettings { it.copy(trackOvulationSigns = on) } },
            )
            Muted(tr("Permet de noter chaque jour l'aspect de la glaire et la température basale au réveil pour repérer l'ovulation.", "Allows logging cervical mucus and morning basal temperature to track ovulation signs.", pl = "Pozwala codziennie zapisywać wygląd śluzu i podstawową temperaturę ciała po przebudzeniu, aby rozpoznać owulację.", uk = "Дає змогу щодня записувати вигляд слизу та базальну температуру після пробудження, щоб відстежувати овуляцію.", es = "Permite anotar cada día el aspecto del moco y la temperatura basal al despertar para detectar la ovulación.", de = "Ermöglicht es, jeden Tag das Aussehen des Zervixschleims und die Basaltemperatur nach dem Aufwachen einzutragen, um den Eisprung zu erkennen."))
            SwitchRow(
                tr("Cycles naturellement irréguliers (périménopause, SOPK…)", "Naturally irregular cycles (perimenopause, PCOS…)", pl = "Naturalnie nieregularne cykle (perimenopauza, PCOS…)", uk = "Природно нерегулярні цикли (перименопауза, СПКЯ…)", es = "Ciclos naturalmente irregulares (perimenopausia, SOP…)", de = "Natürlich unregelmäßige Zyklen (Perimenopause, PCOS…)"),
                checked = settings.irregularCyclesMode,
                onChange = { on -> actions.updateSettings { it.copy(irregularCyclesMode = on) } },
            )
            Muted(tr("Élargit les plages prévisionnelles sans alertes de retard anxiogènes (syndrome des ovaires polykystiques, arrêt de pilule, périménopause).", "Widens predicted ranges without alarming alerts (polycystic ovary syndrome, post-pill, perimenopause).", pl = "Poszerza przedziały prognoz bez niepokojących alertów o opóźnieniu (zespół policystycznych jajników, odstawienie tabletek, perimenopauza).", uk = "Розширює діапазони прогнозів без тривожних сповіщень про затримку (синдром полікістозних яєчників, припинення прийому таблеток, перименопауза).", es = "Amplía los intervalos de previsión sin alertas de retraso que generen ansiedad (síndrome de ovario poliquístico, abandono de la píldora, perimenopausia).", de = "Erweitert die Vorhersagebereiche ohne beunruhigende Verspätungswarnungen (polyzystisches Ovarialsyndrom, nach dem Absetzen der Pille, Perimenopause)."))
            SwitchRow(
                tr("Mettre les prévisions en pause (grossesse, allaitement, contraception)", "Pause predictions (pregnancy, breastfeeding, contraception)", pl = "Wstrzymaj prognozy (ciąża, karmienie piersią, antykoncepcja)", uk = "Призупинити прогнози (вагітність, годування груддю, контрацепція)", es = "Pausar las previsiones (embarazo, lactancia, anticoncepción)", de = "Vorhersagen pausieren (Schwangerschaft, Stillzeit, Verhütung)"),
                checked = settings.pausePredictions,
                onChange = { on -> actions.updateSettings { it.copy(pausePredictions = on) } },
            )
        }

        SectionTitle(tr("Apparence et sécurité", "Appearance and security", pl = "Wygląd i bezpieczeństwo", uk = "Вигляд і безпека", es = "Apariencia y seguridad", de = "Erscheinungsbild und Sicherheit"))
        LunaCard {
            Text(tr("Palette de couleurs", "Color palette", pl = "Paleta kolorów", uk = "Колірна палітра", es = "Paleta de colores", de = "Farbpalette"), style = MaterialTheme.typography.titleSmall)
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
                tr("Verrouiller l'application par code à 4 chiffres", "Lock the app with a 4-digit PIN", pl = "Zablokuj aplikację 4-cyfrowym kodem", uk = "Заблокувати застосунок 4-значним кодом", es = "Bloquear la aplicación con un código de 4 dígitos", de = "App mit 4-stelligem Code sperren"),
                checked = settings.pinHash != null,
                onChange = { on ->
                    if (on) {
                        showPinSetup = true
                    } else {
                        actions.updateSettings { it.copy(pinHash = null) }
                    }
                },
            )
            Muted(tr("Quand le code est activé, l'aperçu de l'application est également masqué dans le sélecteur d'applications récentes.", "When the PIN is enabled, the app preview is also hidden in the recent apps switcher.", pl = "Gdy kod jest włączony, podgląd aplikacji jest też ukrywany w przełączniku ostatnich aplikacji.", uk = "Коли код увімкнено, попередній перегляд застосунку також приховується в перемикачі нещодавніх застосунків.", es = "Cuando el código está activado, la vista previa de la aplicación también se oculta en el selector de aplicaciones recientes.", de = "Ist der Code aktiviert, wird auch die Vorschau der App in der Übersicht der zuletzt verwendeten Apps ausgeblendet."))
        }

        SectionTitle(tr("Mes données", "My data", pl = "Moje dane", uk = "Мої дані", es = "Mis datos", de = "Meine Daten"))
        LunaCard {
            if (cloudBackupControllable) {
                var backupOn by remember { mutableStateOf(isCloudBackupEnabled()) }
                Text(
                    tr(
                    fr = "Tout est enregistré sur cet iPhone. Lunaison n'a pas accès à Internet et n'envoie rien. Si la sauvegarde iCloud de votre iPhone est activée, vos données en font partie, sous le contrôle de votre compte Apple. Vous pouvez les en exclure ci-dessous.",
                    en = "Everything is saved on this iPhone. Lunaison has no Internet access and sends nothing. If your iPhone's iCloud backup is on, your data is part of it, under the control of your Apple account. You can exclude it below.",
                    pl = "Wszystko jest zapisane na tym iPhonie. Lunaison nie ma dostępu do internetu i niczego nie wysyła. Jeśli kopia zapasowa iCloud na Twoim iPhonie jest włączona, Twoje dane są w niej zawarte, pod kontrolą Twojego konta Apple. Możesz je z niej wyłączyć poniżej.",
                    uk = "Усе зберігається на цьому iPhone. Lunaison не має доступу до інтернету й нічого не надсилає. Якщо резервне копіювання iCloud на вашому iPhone увімкнено, ваші дані входять до нього під контролем вашого облікового запису Apple. Нижче можна виключити їх із копії.",
                    es = "Todo se guarda en este iPhone. Lunaison no tiene acceso a Internet y no envía nada. Si la copia de seguridad de iCloud de tu iPhone está activada, tus datos forman parte de ella, bajo el control de tu cuenta de Apple. Puedes excluirlos abajo.",
                    de = "Alles wird auf diesem iPhone gespeichert. Lunaison hat keinen Internetzugriff und sendet nichts. Ist die iCloud-Sicherung deines iPhones aktiviert, sind deine Daten darin enthalten, unter der Kontrolle deines Apple-Kontos. Unten kannst du sie ausschließen.",
                ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                SwitchRow(
                    tr("Inclure dans la sauvegarde iCloud", "Include in iCloud backup", pl = "Uwzględnij w kopii zapasowej iCloud", uk = "Включити в резервну копію iCloud", es = "Incluir en la copia de seguridad de iCloud", de = "In die iCloud-Sicherung aufnehmen"),
                    checked = backupOn,
                    onChange = { backupOn = it; setCloudBackupEnabled(it) },
                )
            } else {
                Text(tr("Tout est enregistré sur ce téléphone, nulle part ailleurs. Lunaison n'a pas accès à Internet et ne fait pas de sauvegarde dans le nuage.", "Everything is saved on this phone, nowhere else. Lunaison has no Internet access and makes no cloud backup.", pl = "Wszystko jest zapisane na tym telefonie i nigdzie indziej. Lunaison nie ma dostępu do internetu i nie tworzy kopii zapasowych w chmurze.", uk = "Усе зберігається на цьому телефоні й ніде більше. Lunaison не має доступу до інтернету й не створює резервних копій у хмарі.", es = "Todo se guarda en este teléfono, en ningún otro sitio. Lunaison no tiene acceso a Internet y no hace copias de seguridad en la nube.", de = "Alles wird auf diesem Telefon gespeichert, nirgendwo sonst. Lunaison hat keinen Internetzugriff und erstellt keine Cloud-Sicherung."), style = MaterialTheme.typography.bodyMedium)
            }
            LunaButton(tr("Bilan de santé pour un soignant", "Health report for a clinician", pl = "Raport medyczny dla personelu medycznego", uk = "Медичний звіт для медичного працівника", es = "Informe médico para el profesional sanitario", de = "Arztbericht für medizinisches Fachpersonal"), onClick = onHealthReport, style = LunaButtonStyle.Outlined)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LunaButton(
                    tr("Exporter (CSV)", "Export (CSV)", pl = "Eksportuj (CSV)", uk = "Експортувати (CSV)", es = "Exportar (CSV)", de = "Exportieren (CSV)"),
                    onClick = { exportCsv("lunaison.csv") },
                    style = LunaButtonStyle.Outlined,
                    modifier = Modifier.weight(1f),
                )
                if (importAvailable) {
                    LunaButton(
                        tr("Importer", "Import", pl = "Importuj", uk = "Імпортувати", es = "Importar", de = "Importieren"),
                        onClick = { showImportSourceDialog = true },
                        style = LunaButtonStyle.Outlined,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            importResultMessage?.let { msg ->
                Muted(msg)
            }
            LunaButton(tr("Tout effacer", "Erase everything", pl = "Usuń wszystko", uk = "Стерти все", es = "Borrar todo", de = "Alles löschen"), onClick = { confirmErase = true }, style = LunaButtonStyle.Quiet, icon = Icons.Filled.Delete)
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

        SectionTitle(tr("À propos", "About", pl = "O aplikacji", uk = "Про застосунок", es = "Acerca de", de = "Über die App"))
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
                    "Libre (MPL-2.0), gratuit, sans publicité. Ce n'est ni un dispositif médical ni une méthode de contraception.",
                    "Free and open source (MPL-2.0), no ads. It is neither a medical device nor a method of contraception.",
                    pl = "Wolne i otwarte (MPL-2.0), darmowe, bez reklam. Nie jest to wyrób medyczny ani metoda antykoncepcji.",
                    uk = "Вільне та відкрите (MPL-2.0), без реклами. Це не медичний виріб і не метод контрацепції.",
                    es = "Libre y abierto (MPL-2.0), gratuito, sin anuncios. No es un producto sanitario ni un método anticonceptivo.",
                    de = "Frei und quelloffen (MPL-2.0), kostenlos, werbefrei. Weder Medizinprodukt noch Verhütungsmethode.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onArticle("previsions") }, modifier = Modifier.weight(1f)) {
                    Text(tr("Prévisions", "Predictions", pl = "Prognozy", uk = "Прогнози", es = "Previsiones", de = "Vorhersagen"), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { openLink(context, Links.PRIVACY) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Confidentialité", "Privacy", pl = "Prywatność", uk = "Конфіденційність", es = "Privacidad", de = "Datenschutz"), style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { openLink(context, Links.SOURCE) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Code source", "Source code", pl = "Kod źródłowy", uk = "Вихідний код", es = "Código fuente", de = "Quellcode"), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { openLink(context, Links.CONTACT) }, modifier = Modifier.weight(1f)) {
                    Text(tr("Nous écrire", "Contact us", pl = "Napisz do nas", uk = "Напишіть нам", es = "Escríbenos", de = "Schreib uns"), style = MaterialTheme.typography.labelMedium)
                }
            }
            TextButton(onClick = onReplayWelcome, modifier = Modifier.fillMaxWidth()) {
                Text(tr("Revoir l'accueil de bienvenue", "Replay the welcome tour", pl = "Zobacz ponownie ekran powitalny", uk = "Переглянути вітання ще раз", es = "Volver a ver la bienvenida", de = "Willkommen erneut ansehen"), style = MaterialTheme.typography.labelMedium)
            }
            if (storePageAvailable) {
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

    ImportDialogs(
        showSource = showImportSourceDialog,
        onDismiss = { showImportSourceDialog = false },
        actions = actions,
        onResult = { importResultMessage = it },
    )

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(tr("Tout effacer ?", "Erase everything?", pl = "Usunąć wszystko?", uk = "Стерти все?", es = "¿Borrar todo?", de = "Alles löschen?")) },
            text = { Text(tr("Toutes vos journées et vos réglages seront supprimés de ce téléphone. Ce n'est pas réversible.", "All your days and settings will be deleted from this phone. This cannot be undone.", pl = "Wszystkie Twoje dni i ustawienia zostaną usunięte z tego telefonu. Tego nie można cofnąć.", uk = "Усі ваші дні та налаштування буде видалено з цього телефону. Це неможливо скасувати.", es = "Todos tus días y ajustes se eliminarán de este teléfono. No se puede deshacer.", de = "Alle deine Tage und Einstellungen werden von diesem Telefon gelöscht. Das lässt sich nicht rückgängig machen.")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmErase = false
                    actions.eraseAll()
                }) { Text(tr("Effacer", "Erase", pl = "Usuń", uk = "Стерти", es = "Borrar", de = "Löschen")) }
            },
            dismissButton = { TextButton(onClick = { confirmErase = false }) { Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen")) } },
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
