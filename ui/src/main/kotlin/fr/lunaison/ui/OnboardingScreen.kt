package fr.lunaison.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.PartnerSnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import fr.lunaison.core.model.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    today: LocalDate,
    onFinish: (Baseline) -> Unit,
    onFinishAsPartner: (PartnerSnapshot) -> Unit = {},
    onFinishAndLog: (Baseline) -> Unit = onFinish,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var ageGroup by rememberSaveable { mutableStateOf<AgeGroup?>(null) }
    var lastStart by rememberSaveable { mutableStateOf<String?>(null) }
    var usualLength by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPartnerScanner by rememberSaveable { mutableStateOf(false) }

    var notificationsOn by rememberSaveable { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsOn = granted
        step = 5
    }

    fun baseline() = Baseline(ageGroup ?: AgeGroup.ADULT, lastStart?.let(LocalDate::parse), usualLength)
    fun finish() = onFinish(baseline())

    Page(Modifier.safeDrawingPadding()) {
        when (step) {
            0 -> {
                PageTitle(tr("Bienvenue dans Lunaison", "Welcome to Lunaison", pl = "Witaj w Lunaison", uk = "Ласкаво просимо до Lunaison", es = "Te damos la bienvenida a Lunaison", de = "Willkommen bei Lunaison"))
                Text(tr("Pour suivre votre cycle, comprendre ce qui se passe et savoir quand vos règles arrivent.", "To track your cycle, understand what is happening and know when your period is coming.", pl = "Aby śledzić cykl, rozumieć, co się dzieje, i wiedzieć, kiedy nadejdzie miesiączka.", uk = "Щоб стежити за циклом, розуміти, що відбувається, і знати, коли настануть місячні.", es = "Para seguir tu ciclo, entender lo que pasa y saber cuándo llega tu regla.", de = "Um deinen Zyklus zu begleiten, zu verstehen, was passiert, und zu wissen, wann deine Periode kommt."), style = MaterialTheme.typography.bodyLarge)
                LunaCard {
                    Text(tr("Vos données restent sur ce téléphone", "Your data stays on this phone", pl = "Twoje dane zostają w tym telefonie", uk = "Ваші дані залишаються на цьому телефоні", es = "Tus datos se quedan en este teléfono", de = "Deine Daten bleiben auf diesem Handy"), style = MaterialTheme.typography.titleMedium)
                    Bullet(tr("Pas de compte, pas de serveur, pas de publicité, pas de traceur.", "No account, no server, no ads, no trackers.", pl = "Bez konta, bez serwera, bez reklam, bez śledzenia.", uk = "Без облікового запису, без сервера, без реклами, без трекерів.", es = "Sin cuenta, sin servidor, sin anuncios, sin rastreadores.", de = "Kein Konto, kein Server, keine Werbung, keine Tracker."))
                    Bullet(tr("Lunaison n'a même pas accès à Internet.", "Lunaison does not even have Internet access.", pl = "Lunaison nie ma nawet dostępu do Internetu.", uk = "Lunaison навіть не має доступу до Інтернету.", es = "Lunaison ni siquiera tiene acceso a Internet.", de = "Lunaison hat nicht einmal Internetzugriff."))
                    Bullet(tr("Rien n'est partagé sans que vous l'envoyiez vous-même.", "Nothing is shared unless you send it yourself.", pl = "Nic nie jest udostępniane, dopóki sama tego nie wyślesz.", uk = "Нічого не передається, якщо ви самі цього не надішлете.", es = "No se comparte nada a menos que lo envíes tú.", de = "Nichts wird geteilt, außer du schickst es selbst."))
                    Bullet(tr("Vous pouvez tout effacer en un geste.", "You can erase everything in one tap.", pl = "Możesz wszystko usunąć jednym dotknięciem.", uk = "Ви можете стерти все одним дотиком.", es = "Puedes borrarlo todo con un toque.", de = "Du kannst alles mit einem Tippen löschen."))
                }
                Muted(tr("Lunaison aide à s'organiser et à mieux se connaître. Ce n'est ni un dispositif médical ni une méthode de contraception.", "Lunaison helps you plan and get to know yourself better. It is neither a medical device nor a method of contraception.", pl = "Lunaison pomaga się zorganizować i lepiej poznać siebie. Nie jest wyrobem medycznym ani metodą antykoncepcji.", uk = "Lunaison допомагає планувати й краще пізнати себе. Це не медичний виріб і не метод контрацепції.", es = "Lunaison te ayuda a organizarte y a conocerte mejor. No es un dispositivo médico ni un método anticonceptivo.", de = "Lunaison hilft dir, dich zu organisieren und dich besser kennenzulernen. Es ist weder ein Medizinprodukt noch eine Verhütungsmethode."))
                LunaButton(tr("Commencer (suivre mon cycle)", "Get started (track my cycle)", pl = "Zaczynamy (śledzę swój cykl)", uk = "Почати (стежити за своїм циклом)", es = "Empezar (seguir mi ciclo)", de = "Los geht's (meinen Zyklus begleiten)"), onClick = { step = 1 })
                LunaButton(
                    tr(
                        "Je suis partenaire (scanner un QR Code, NFC ou SMS)",
                        "I am a partner (scan a QR Code, NFC or SMS)",
                        pl = "Jestem partnerem (kod QR, NFC lub SMS)",
                        uk = "Я партнер (QR-код, NFC або SMS)",
                        es = "Soy pareja (código QR, NFC o SMS)",
                        de = "Ich bin Partner (QR-Code, NFC oder SMS)",
                    ),
                    onClick = { showPartnerScanner = true },
                    style = LunaButtonStyle.Outlined,
                )
                if (showPartnerScanner) {
                    QrScannerDialog(
                        onScanned = { scanned ->
                            showPartnerScanner = false
                            onFinishAsPartner(scanned)
                        },
                        onDismiss = { showPartnerScanner = false },
                    )
                }
            }
            1 -> {
                PageTitle(tr("Quel âge avez-vous ?", "How old are you?", pl = "Ile masz lat?", uk = "Скільки вам років?", es = "¿Cuántos años tienes?", de = "Wie alt bist du?"), onBack = { step = 0 })
                Text(
                    tr("Les repères d'un cycle normal ne sont pas les mêmes les premières années après les premières règles. Votre réponse reste sur ce téléphone.", "What counts as a normal cycle is different in the first years after your first period. Your answer stays on this phone.", pl = "To, co uznaje się za normalny cykl, wygląda inaczej w pierwszych latach po pierwszej miesiączce. Twoja odpowiedź zostaje w tym telefonie.", uk = "Те, що вважається нормальним циклом, відрізняється в перші роки після першої менструації. Ваша відповідь залишається на цьому телефоні.", es = "Lo que se considera un ciclo normal es distinto en los primeros años tras la primera regla. Tu respuesta se queda en este teléfono.", de = "Was als normaler Zyklus gilt, ist in den ersten Jahren nach der ersten Periode anders. Deine Antwort bleibt auf diesem Handy."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LunaButton(tr("Moins de 18 ans", "Under 18", pl = "Poniżej 18 lat", uk = "До 18 років", es = "Menos de 18 años", de = "Unter 18"), onClick = { ageGroup = AgeGroup.UNDER_18; step = 2 }, style = LunaButtonStyle.Outlined)
                LunaButton(tr("18 ans ou plus", "18 or over", pl = "18 lat lub więcej", uk = "18 років або більше", es = "18 años o más", de = "18 oder älter"), onClick = { ageGroup = AgeGroup.ADULT; step = 2 }, style = LunaButtonStyle.Outlined)
            }
            2 -> {
                PageTitle(tr("Vos dernières règles", "Your last period", pl = "Twoja ostatnia miesiączka", uk = "Ваші останні місячні", es = "Tu última regla", de = "Deine letzte Periode"), onBack = { step = 1 })
                Text(tr("Quel jour ont-elles commencé ? Une date approximative suffit.", "What day did it start? An approximate date is enough.", pl = "Którego dnia się zaczęła? Wystarczy przybliżona data.", uk = "Якого дня вони почалися? Достатньо приблизної дати.", es = "¿Qué día empezó? Basta con una fecha aproximada.", de = "An welchem Tag hat sie begonnen? Ein ungefähres Datum reicht."), style = MaterialTheme.typography.bodyLarge)
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = lastStart?.let { LocalDate.parse(it).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() },
                    selectableDates = remember(today) {
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                                return !date.isAfter(today) && date.isAfter(today.minusDays(120))
                            }
                        }
                    },
                )
                DatePicker(state = pickerState, title = null, headline = null, showModeToggle = false)
                val selected = pickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                LunaButton(
                    if (selected != null) tr("Le ${Format.dayMonth(selected)}", "${Format.dayMonth(selected)}") else tr("Choisissez une date", "Choose a date", pl = "Wybierz datę", uk = "Оберіть дату", es = "Elige una fecha", de = "Datum wählen"),
                    onClick = { lastStart = selected?.toString(); step = 3 },
                    enabled = selected != null,
                    icon = Icons.Filled.Check,
                )
                LunaButton(tr("Je ne sais plus", "I don't remember", pl = "Nie pamiętam", uk = "Не пам’ятаю", es = "No me acuerdo", de = "Weiß ich nicht mehr"), onClick = { lastStart = null; step = 3 }, style = LunaButtonStyle.Quiet)
            }
            3 -> {
                PageTitle(tr("Votre cycle habituel", "Your usual cycle", pl = "Twój zwykły cykl", uk = "Ваш звичайний цикл", es = "Tu ciclo habitual", de = "Dein üblicher Zyklus"), onBack = { step = 2 })
                Text(
                    tr("Combien de jours entre le début de vos règles et le début des suivantes, en général ? Si vous ne savez pas, Lunaison l'apprendra au fil des cycles.", "How many days between the start of one period and the start of the next, usually? If you don't know, Lunaison will learn it over your cycles.", pl = "Ile dni zwykle mija od początku jednej miesiączki do początku następnej? Jeśli nie wiesz, Lunaison nauczy się tego z kolejnych cykli.", uk = "Скільки днів зазвичай минає від початку одних місячних до початку наступних? Якщо не знаєте, Lunaison дізнається це з часом.", es = "¿Cuántos días suelen pasar entre el inicio de una regla y el de la siguiente? Si no lo sabes, Lunaison lo aprenderá ciclo a ciclo.", de = "Wie viele Tage liegen normalerweise zwischen dem Beginn einer Periode und dem der nächsten? Wenn du es nicht weißt, lernt Lunaison es mit der Zeit."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                val value = usualLength ?: 28
                Stepper(
                    label = tr("$value jours", "$value days", pl = "$value dni", uk = "$value днів", es = "$value días", de = "$value Tage"),
                    onMinus = { usualLength = (value - 1).coerceAtLeast(18) },
                    onPlus = { usualLength = (value + 1).coerceAtMost(60) },
                )
                LunaButton(tr("Continuer", "Continue", pl = "Dalej", uk = "Продовжити", es = "Continuar", de = "Weiter"), onClick = { usualLength = value; step = 4 })
                LunaButton(tr("Je ne sais pas", "I don't know", pl = "Nie wiem", uk = "Не знаю", es = "No lo sé", de = "Weiß ich nicht"), onClick = { usualLength = null; step = 4 }, style = LunaButtonStyle.Quiet)
            }
            4 -> {
                PageTitle(tr("Les rappels", "Reminders", pl = "Przypomnienia", uk = "Нагадування", es = "Recordatorios", de = "Erinnerungen"), onBack = { step = 3 })
                Text(
                    tr("Lunaison peut vous prévenir deux jours avant vos prochaines règles, et vous rappeler de noter vos journées autour des règles. Les notifications restent discrètes sur l'écran verrouillé.", "Lunaison can let you know two days before your next period, and remind you to log your days around your period. Notifications stay discreet on the lock screen.", pl = "Lunaison może dać Ci znać dwa dni przed kolejną miesiączką i przypominać o notowaniu dni wokół miesiączki. Powiadomienia są dyskretne na ekranie blokady.", uk = "Lunaison може попередити вас за два дні до наступних місячних і нагадувати записувати дні навколо них. На заблокованому екрані сповіщення залишаються непомітними.", es = "Lunaison puede avisarte dos días antes de tu próxima regla y recordarte que anotes tus días alrededor de la regla. Las notificaciones son discretas en la pantalla de bloqueo.", de = "Lunaison kann dich zwei Tage vor deiner nächsten Periode benachrichtigen und dich erinnern, deine Tage rund um die Periode einzutragen. Auf dem Sperrbildschirm bleiben die Benachrichtigungen diskret."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Muted(tr("Vous pourrez tout changer dans les réglages.", "You can change everything in the settings.", pl = "Wszystko możesz zmienić w ustawieniach.", uk = "Усе можна змінити в налаштуваннях.", es = "Puedes cambiarlo todo en los ajustes.", de = "Du kannst alles in den Einstellungen ändern."))
                LunaButton(
                    tr("Autoriser les notifications", "Allow notifications", pl = "Zezwól na powiadomienia", uk = "Дозволити сповіщення", es = "Permitir notificaciones", de = "Benachrichtigungen erlauben"),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationsOn = true
                            step = 5
                        }
                    },
                )
                LunaButton(
                    tr("Plus tard", "Later", pl = "Później", uk = "Пізніше", es = "Más tarde", de = "Später"),
                    onClick = {
                        notificationsOn = false
                        step = 5
                    },
                    style = LunaButtonStyle.Quiet,
                )
            }
            5 -> {
                // Récapitulatif : ce qui est en place, et un premier geste utile tout de suite.
                PageTitle(tr("C'est prêt", "You're all set", pl = "Wszystko gotowe", uk = "Усе готово", es = "Todo listo", de = "Alles bereit"), onBack = { step = 4 })
                LunaCard {
                    Bullet(
                        if (lastStart != null) {
                            tr("Vos prochaines règles sont estimées dès maintenant.", "Your next period is estimated right away.", pl = "Kolejna miesiączka jest szacowana od razu.", uk = "Наступні місячні оцінюються вже зараз.", es = "Tu próxima regla se estima desde ya.", de = "Deine nächste Periode wird ab sofort geschätzt.")
                        } else {
                            tr("Notez vos prochaines règles : Lunaison commencera alors ses estimations.", "Log your next period: Lunaison will then start estimating.", pl = "Zanotuj kolejną miesiączkę: wtedy Lunaison zacznie szacować.", uk = "Запишіть наступні місячні: тоді Lunaison почне оцінювати.", es = "Anota tu próxima regla: Lunaison empezará entonces a estimar.", de = "Trag deine nächste Periode ein: Dann beginnt Lunaison mit den Schätzungen.")
                        },
                    )
                    Bullet(
                        if (notificationsOn) {
                            tr("Rappel 2 jours avant vos règles : activé.", "Reminder 2 days before your period: on.", pl = "Przypomnienie 2 dni przed miesiączką: włączone.", uk = "Нагадування за 2 дні до місячних: увімкнено.", es = "Aviso 2 días antes de tu regla: activado.", de = "Erinnerung 2 Tage vor der Periode: an.")
                        } else {
                            tr("Rappels désactivés : vous pourrez les activer dans les réglages.", "Reminders off: you can turn them on in the settings.", pl = "Przypomnienia wyłączone: możesz je włączyć w ustawieniach.", uk = "Нагадування вимкнено: їх можна увімкнути в налаштуваннях.", es = "Recordatorios desactivados: puedes activarlos en los ajustes.", de = "Erinnerungen aus: Du kannst sie in den Einstellungen einschalten.")
                        },
                    )
                }
                LunaButton(
                    tr("Noter ma journée maintenant", "Log my day now", pl = "Zanotuj mój dzień teraz", uk = "Записати мій день зараз", es = "Anotar mi día ahora", de = "Meinen Tag jetzt eintragen"),
                    onClick = { onFinishAndLog(baseline()) },
                    icon = Icons.Filled.Check,
                )
                LunaButton(
                    tr("Découvrir l'accueil", "Go to the home screen", pl = "Przejdź do ekranu głównego", uk = "Перейти на головний екран", es = "Ir a la pantalla de inicio", de = "Zum Startbildschirm"),
                    onClick = { finish() },
                    style = LunaButtonStyle.Quiet,
                )
            }
        }
    }
}

/** Réglage d'une valeur avec deux gros boutons, sans clavier. */
@Composable
fun Stepper(label: String, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(onClick = onMinus, modifier = Modifier.semantics { contentDescription = tr("Moins un", "Minus one", pl = "Minus jeden", uk = "Мінус один", es = "Menos uno", de = "Minus eins") }) {
            Text("−", style = MaterialTheme.typography.headlineMedium)
        }
        Text(label, style = MaterialTheme.typography.headlineMedium)
        FilledTonalIconButton(onClick = onPlus) { Icon(Icons.Filled.Add, contentDescription = tr("Plus un", "Plus one", pl = "Plus jeden", uk = "Плюс один", es = "Más uno", de = "Plus eins")) }
    }
}
