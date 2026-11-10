package fr.lunaison.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.tr
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Accueil du premier lancement, en cinq écrans courts (voir [OnboardingFlow]) :
 * bienvenue et confidentialité, âge, cycle habituel, verrouillage, rappels. Tout est facultatif sauf l'âge.
 *
 * @param current réglage déjà connu quand l'accueil est rouvert depuis les réglages (null au premier lancement).
 * @param hasHistory un historique existe déjà (import) : l'écran du cycle est sauté.
 * @param actions donne accès à l'import dès l'accueil ; sans elles, le bouton d'import n'est pas affiché.
 * @param onClose rouvert depuis les réglages : ferme l'accueil sans rien changer (null au premier lancement).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    today: LocalDate,
    onFinish: (Baseline) -> Unit,
    onFinishAsPartner: (PartnerSnapshot) -> Unit = {},
    onFinishAndLog: (Baseline) -> Unit = onFinish,
    current: Baseline? = null,
    hasHistory: Boolean = false,
    pinEnabled: Boolean = false,
    onPinChosen: (String) -> Unit = {},
    actions: Actions? = null,
    onClose: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var stepIndex by rememberSaveable { mutableIntStateOf(OnboardingStep.WELCOME.ordinal) }
    val step = OnboardingStep.entries[stepIndex]
    var ageGroup by rememberSaveable { mutableStateOf(current?.ageGroup) }
    var lastStart by rememberSaveable { mutableStateOf<String?>(null) }
    var usualLength by rememberSaveable { mutableStateOf<Int?>(null) }
    var periodLength by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPartnerScanner by rememberSaveable { mutableStateOf(false) }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var importMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showPinSetup by rememberSaveable { mutableStateOf(false) }
    var notificationsAsked by rememberSaveable { mutableStateOf(false) }
    var notificationsOn by remember { mutableStateOf(notificationsAllowed(context)) }

    // Au retour des réglages d'Android, l'état des notifications peut avoir changé.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsOn = notificationsAllowed(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAsked = true
        notificationsOn = granted || notificationsAllowed(context)
    }

    fun baseline() = OnboardingFlow.finalBaseline(current ?: Baseline(AgeGroup.ADULT), ageGroup, lastStart?.let(LocalDate::parse), usualLength, periodLength)
    fun goNext() {
        OnboardingFlow.next(step, hasHistory)?.let { stepIndex = it.ordinal }
    }
    fun goBack() {
        val before = OnboardingFlow.previous(step, hasHistory)
        if (before != null) stepIndex = before.ordinal else onClose?.invoke()
    }
    val back: (() -> Unit)? = if (step != OnboardingStep.WELCOME || onClose != null) ::goBack else null
    BackHandler(enabled = back != null) { goBack() }

    val skip = tr("Passer", "Skip", pl = "Pomiń", uk = "Пропустити", es = "Omitir", de = "Überspringen")
    val later = tr("Plus tard", "Later", pl = "Później", uk = "Пізніше", es = "Más tarde", de = "Später")
    val continueLabel = tr("Continuer", "Continue", pl = "Dalej", uk = "Продовжити", es = "Continuar", de = "Weiter")

    Page(Modifier.safeDrawingPadding()) {
        when (step) {
            OnboardingStep.WELCOME -> {
                PageTitle(tr("Bienvenue dans Lunaison", "Welcome to Lunaison", pl = "Witaj w Lunaison", uk = "Ласкаво просимо до Lunaison", es = "Te damos la bienvenida a Lunaison", de = "Willkommen bei Lunaison"), onBack = back)
                Text(tr("Pour suivre votre cycle, comprendre ce qui se passe et savoir quand vos règles arrivent.", "To track your cycle, understand what is happening and know when your period is coming.", pl = "Aby śledzić cykl, rozumieć, co się dzieje, i wiedzieć, kiedy nadejdzie miesiączka.", uk = "Щоб стежити за циклом, розуміти, що відбувається, і знати, коли настануть місячні.", es = "Para seguir tu ciclo, entender lo que pasa y saber cuándo llega tu regla.", de = "Um deinen Zyklus zu begleiten, zu verstehen, was passiert, und zu wissen, wann deine Periode kommt."), style = MaterialTheme.typography.bodyLarge)
                LunaCard {
                    Text(tr("Vos données restent sur ce téléphone", "Your data stays on this phone", pl = "Twoje dane zostają w tym telefonie", uk = "Ваші дані залишаються на цьому телефоні", es = "Tus datos se quedan en este teléfono", de = "Deine Daten bleiben auf diesem Handy"), style = MaterialTheme.typography.titleMedium)
                    Bullet(tr("Pas de compte, pas de serveur, pas de publicité, pas de traceur. Lunaison n'a même pas accès à Internet.", "No account, no server, no ads, no trackers. Lunaison does not even have Internet access.", pl = "Bez konta, bez serwera, bez reklam, bez śledzenia. Lunaison nie ma nawet dostępu do Internetu.", uk = "Без облікового запису, без сервера, без реклами, без трекерів. Lunaison навіть не має доступу до Інтернету.", es = "Sin cuenta, sin servidor, sin anuncios, sin rastreadores. Lunaison ni siquiera tiene acceso a Internet.", de = "Kein Konto, kein Server, keine Werbung, keine Tracker. Lunaison hat nicht einmal Internetzugriff."))
                    Bullet(tr("Aucune sauvegarde automatique dans le nuage. Rien n'est partagé sans que vous l'envoyiez vous-même.", "No automatic cloud backup. Nothing is shared unless you send it yourself.", pl = "Bez automatycznej kopii zapasowej w chmurze. Nic nie jest udostępniane, dopóki tego nie wyślesz.", uk = "Жодних автоматичних резервних копій у хмарі. Нічого не передається, якщо ви самі цього не надішлете.", es = "Sin copia de seguridad automática en la nube. No se comparte nada a menos que lo envíes tú.", de = "Keine automatische Cloud-Sicherung. Nichts wird geteilt, außer du schickst es selbst."))
                    Bullet(tr("Un code à 4 chiffres facultatif verrouille l'appli. Vous pouvez tout effacer en un geste.", "An optional 4-digit PIN locks the app. You can erase everything in one tap.", pl = "Opcjonalny 4-cyfrowy kod blokuje aplikację. Możesz wszystko usunąć jednym dotknięciem.", uk = "Необов'язковий 4-значний код блокує застосунок. Ви можете стерти все одним дотиком.", es = "Un código opcional de 4 dígitos bloquea la aplicación. Puedes borrarlo todo con un toque.", de = "Ein optionaler 4-stelliger Code sperrt die App. Du kannst alles mit einem Tippen löschen."))
                }
                Muted(tr("Lunaison est un outil de suivi : ses prévisions sont des estimations. Ce n'est ni un dispositif médical ni une méthode de contraception.", "Lunaison is a tracking tool: its predictions are estimates. It is neither a medical device nor a method of contraception.", pl = "Lunaison to narzędzie do śledzenia cyklu: jego prognozy to szacunki. Nie jest wyrobem medycznym ani metodą antykoncepcji.", uk = "Lunaison є інструментом для відстеження: його прогнози є оцінками. Це не медичний виріб і не метод контрацепції.", es = "Lunaison es una herramienta de seguimiento: sus previsiones son estimaciones. No es un dispositivo médico ni un método anticonceptivo.", de = "Lunaison ist ein Werkzeug zum Begleiten des Zyklus: Die Vorhersagen sind Schätzungen. Es ist weder ein Medizinprodukt noch eine Verhütungsmethode."))
                if (hasHistory) {
                    Muted(tr("Un historique est déjà présent : Lunaison l'utilisera pour ses estimations.", "A history is already here: Lunaison will use it for its estimates.", pl = "Historia jest już zapisana: Lunaison użyje jej do szacunków.", uk = "Історія вже є: Lunaison використає її для оцінок.", es = "Ya hay un historial: Lunaison lo usará para sus estimaciones.", de = "Ein Verlauf ist schon vorhanden: Lunaison nutzt ihn für die Schätzungen."))
                }
                importMessage?.let { Muted(it) }
                LunaButton(tr("Commencer (suivre mon cycle)", "Get started (track my cycle)", pl = "Zaczynamy (śledzę swój cykl)", uk = "Почати (стежити за своїм циклом)", es = "Empezar (seguir mi ciclo)", de = "Los geht's (meinen Zyklus begleiten)"), onClick = { goNext() })
                if (actions != null) {
                    LunaButton(
                        tr("J'ai déjà des données : importer", "I already have data: import", pl = "Mam już dane: importuj", uk = "Маю дані: імпортувати", es = "Ya tengo datos: importar", de = "Ich habe schon Daten: importieren"),
                        onClick = { showImport = true },
                        style = LunaButtonStyle.Outlined,
                    )
                }
                LunaButton(
                    tr(
                        "Je suis partenaire (scanner un QR Code ou SMS)",
                        "I am a partner (scan a QR Code or SMS)",
                        pl = "Wspieram bliską osobę (kod QR lub SMS)",
                        uk = "Підтримую близьку людину (QR-код або SMS)",
                        es = "Soy pareja (código QR o SMS)",
                        de = "Ich begleite eine nahestehende Person (QR-Code oder SMS)",
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
                if (actions != null) {
                    ImportDialogs(
                        showSource = showImport,
                        onDismiss = { showImport = false },
                        actions = actions,
                        onResult = { importMessage = it },
                    )
                }
            }
            OnboardingStep.AGE -> {
                PageTitle(tr("Quel âge avez-vous ?", "How old are you?", pl = "Ile masz lat?", uk = "Скільки вам років?", es = "¿Cuántos años tienes?", de = "Wie alt bist du?"), onBack = back)
                Text(
                    tr("Les repères d'un cycle normal ne sont pas les mêmes les premières années après les premières règles. Votre réponse reste sur ce téléphone.", "What counts as a normal cycle is different in the first years after your first period. Your answer stays on this phone.", pl = "To, co uznaje się za normalny cykl, wygląda inaczej w pierwszych latach po pierwszej miesiączce. Twoja odpowiedź zostaje w tym telefonie.", uk = "Те, що вважається нормальним циклом, відрізняється в перші роки після першої менструації. Ваша відповідь залишається на цьому телефоні.", es = "Lo que se considera un ciclo normal es distinto en los primeros años tras la primera regla. Tu respuesta se queda en este teléfono.", de = "Was als normaler Zyklus gilt, ist in den ersten Jahren nach der ersten Periode anders. Deine Antwort bleibt auf diesem Handy."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LunaButton(tr("Moins de 18 ans", "Under 18", pl = "Poniżej 18 lat", uk = "До 18 років", es = "Menos de 18 años", de = "Unter 18"), onClick = { ageGroup = AgeGroup.UNDER_18; goNext() }, style = LunaButtonStyle.Outlined)
                LunaButton(tr("18 ans ou plus", "18 or over", pl = "18 lat lub więcej", uk = "18 років або більше", es = "18 años o más", de = "18 oder älter"), onClick = { ageGroup = AgeGroup.ADULT; goNext() }, style = LunaButtonStyle.Outlined)
                if (OnboardingFlow.canSkip(step, ageAnswered = ageGroup != null)) {
                    LunaButton(skip, onClick = { goNext() }, style = LunaButtonStyle.Quiet)
                }
            }
            OnboardingStep.CYCLE -> {
                PageTitle(tr("Votre cycle habituel", "Your usual cycle", pl = "Twój zwykły cykl", uk = "Ваш звичайний цикл", es = "Tu ciclo habitual", de = "Dein üblicher Zyklus"), onBack = back)
                Text(
                    tr("Tout est facultatif : une date approximative suffit, et Lunaison apprendra de vos cycles.", "Everything is optional: an approximate date is enough, and Lunaison will learn from your cycles.", pl = "Wszystko jest opcjonalne: wystarczy przybliżona data, a Lunaison nauczy się z Twoich cykli.", uk = "Усе необов'язково: достатньо приблизної дати, а Lunaison навчиться з ваших циклів.", es = "Todo es opcional: basta con una fecha aproximada y Lunaison aprenderá de tus ciclos.", de = "Alles ist freiwillig: Ein ungefähres Datum reicht, und Lunaison lernt aus deinen Zyklen."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                SectionTitle(tr("Début de vos dernières règles", "Start of your last period", pl = "Początek ostatniej miesiączki", uk = "Початок останніх місячних", es = "Inicio de tu última regla", de = "Beginn deiner letzten Periode"))
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = lastStart?.let { LocalDate.parse(it).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() },
                    selectableDates = remember(today) {
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                OnboardingFlow.isSelectableLastPeriod(Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate(), today)
                        }
                    },
                )
                DatePicker(state = pickerState, title = null, headline = null, showModeToggle = false)
                val selected = pickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                if (selected != null) {
                    Text(tr("Le ${Format.dayMonth(selected)}", "${Format.dayMonth(selected)}", pl = "${Format.dayMonth(selected)}", uk = "${Format.dayMonth(selected)}", es = "El ${Format.dayMonth(selected)}", de = "Am ${Format.dayMonth(selected)}"), style = MaterialTheme.typography.titleMedium)
                    LunaButton(tr("Je ne sais plus", "I don't remember", pl = "Nie pamiętam", uk = "Не пам’ятаю", es = "No me acuerdo", de = "Weiß ich nicht mehr"), onClick = { pickerState.selectedDateMillis = null }, style = LunaButtonStyle.Quiet)
                } else {
                    Muted(tr("Sans date, Lunaison commencera ses estimations à votre première règle notée.", "Without a date, Lunaison will start estimating from the first period you log.", pl = "Bez daty Lunaison zacznie szacować od pierwszej zanotowanej miesiączki.", uk = "Без дати Lunaison почне оцінювати з першої записаної менструації.", es = "Sin fecha, Lunaison empezará a estimar con la primera regla que anotes.", de = "Ohne Datum beginnt Lunaison mit den Schätzungen ab der ersten eingetragenen Periode."))
                }

                SectionTitle(tr("Durée habituelle du cycle", "Usual cycle length", pl = "Zwykła długość cyklu", uk = "Звичайна тривалість циклу", es = "Duración habitual del ciclo", de = "Übliche Zykluslänge"))
                Muted(tr("Du début d'une règle au début de la suivante.", "From the start of one period to the start of the next.", pl = "Od początku jednej miesiączki do początku następnej.", uk = "Від початку одних місячних до початку наступних.", es = "Del inicio de una regla al inicio de la siguiente.", de = "Vom Beginn einer Periode bis zum Beginn der nächsten."))
                val cycleValue = usualLength ?: OnboardingFlow.DEFAULT_CYCLE_LENGTH
                Stepper(
                    label = if (usualLength != null) {
                        tr("$cycleValue jours", "$cycleValue days", pl = "$cycleValue dni", uk = "$cycleValue днів", es = "$cycleValue días", de = "$cycleValue Tage")
                    } else {
                        tr("$cycleValue jours (par défaut)", "$cycleValue days (default)", pl = "$cycleValue dni (domyślnie)", uk = "$cycleValue днів (за замовчуванням)", es = "$cycleValue días (por defecto)", de = "$cycleValue Tage (Standard)")
                    },
                    onMinus = { usualLength = OnboardingFlow.clampCycleLength(cycleValue - 1) },
                    onPlus = { usualLength = OnboardingFlow.clampCycleLength(cycleValue + 1) },
                    what = tr("durée du cycle", "cycle length", pl = "długość cyklu", uk = "тривалість циклу", es = "duración del ciclo", de = "Zykluslänge"),
                )
                if (usualLength != null) {
                    LunaButton(tr("Je ne sais pas", "I don't know", pl = "Nie wiem", uk = "Не знаю", es = "No lo sé", de = "Weiß ich nicht"), onClick = { usualLength = null }, style = LunaButtonStyle.Quiet)
                } else {
                    Muted(tr("Sans réponse, Lunaison part de ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} jours puis apprend de vos cycles.", "Without an answer, Lunaison starts from ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} days then learns from your cycles.", pl = "Bez odpowiedzi Lunaison zaczyna od ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} dni, a potem uczy się z Twoich cykli.", uk = "Без відповіді Lunaison починає з ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} днів, а потім навчається з ваших циклів.", es = "Sin respuesta, Lunaison parte de ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} días y luego aprende de tus ciclos.", de = "Ohne Antwort startet Lunaison mit ${OnboardingFlow.DEFAULT_CYCLE_LENGTH} Tagen und lernt dann aus deinen Zyklen."))
                }

                SectionTitle(tr("Durée habituelle des règles", "Usual period length", pl = "Zwykła długość miesiączki", uk = "Звичайна тривалість місячних", es = "Duración habitual de la regla", de = "Übliche Periodendauer"))
                val periodValue = periodLength ?: OnboardingFlow.DEFAULT_PERIOD_LENGTH
                Stepper(
                    label = if (periodLength != null) {
                        tr("$periodValue jours", "$periodValue days", pl = "$periodValue dni", uk = "$periodValue днів", es = "$periodValue días", de = "$periodValue Tage")
                    } else {
                        tr("$periodValue jours (par défaut)", "$periodValue days (default)", pl = "$periodValue dni (domyślnie)", uk = "$periodValue днів (за замовчуванням)", es = "$periodValue días (por defecto)", de = "$periodValue Tage (Standard)")
                    },
                    onMinus = { periodLength = OnboardingFlow.clampPeriodLength(periodValue - 1) },
                    onPlus = { periodLength = OnboardingFlow.clampPeriodLength(periodValue + 1) },
                    what = tr("durée des règles", "period length", pl = "długość miesiączki", uk = "тривалість місячних", es = "duración de la regla", de = "Periodendauer"),
                )
                if (periodLength != null) {
                    LunaButton(tr("Je ne sais pas", "I don't know", pl = "Nie wiem", uk = "Не знаю", es = "No lo sé", de = "Weiß ich nicht"), onClick = { periodLength = null }, style = LunaButtonStyle.Quiet)
                } else {
                    Muted(tr("Sans réponse, Lunaison part de ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} jours puis s'ajuste d'après vos règles.", "Without an answer, Lunaison starts from ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} days then adjusts to your periods.", pl = "Bez odpowiedzi Lunaison zaczyna od ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} dni, a potem dopasowuje się do Twoich miesiączek.", uk = "Без відповіді Lunaison починає з ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} днів, а потім підлаштовується під ваші місячні.", es = "Sin respuesta, Lunaison parte de ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} días y luego se ajusta a tus reglas.", de = "Ohne Antwort startet Lunaison mit ${OnboardingFlow.DEFAULT_PERIOD_LENGTH} Tagen und passt sich dann deinen Perioden an."))
                }

                LunaButton(
                    continueLabel,
                    onClick = {
                        lastStart = selected?.toString()
                        goNext()
                    },
                    icon = Icons.Filled.Check,
                )
                LunaButton(skip, onClick = { goNext() }, style = LunaButtonStyle.Quiet)
            }
            OnboardingStep.LOCK -> {
                PageTitle(tr("Verrouiller l'appli ?", "Lock the app?", pl = "Zablokować aplikację?", uk = "Заблокувати застосунок?", es = "¿Bloquear la aplicación?", de = "App sperren?"), onBack = back)
                Text(
                    tr("Facultatif. Un code à 4 chiffres est demandé à l'ouverture et l'aperçu est masqué dans les applications récentes. Vous pourrez le changer dans les réglages.", "Optional. A 4-digit PIN is asked when you open the app, and the preview is hidden in recent apps. You can change it in the settings.", pl = "Opcjonalne. Przy otwieraniu aplikacji wymagany jest 4-cyfrowy kod, a podgląd w ostatnio używanych aplikacjach jest ukryty. Możesz to zmienić w ustawieniach.", uk = "Необов'язково. Під час відкриття застосунку запитується 4-значний код, а попередній перегляд у нещодавніх застосунках приховано. Це можна змінити в налаштуваннях.", es = "Opcional. Se pide un código de 4 dígitos al abrir la aplicación y se oculta la vista previa en las aplicaciones recientes. Puedes cambiarlo en los ajustes.", de = "Optional. Beim Öffnen der App wird ein 4-stelliger Code abgefragt, und die Vorschau in den zuletzt verwendeten Apps wird ausgeblendet. Du kannst das in den Einstellungen ändern."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (pinEnabled) {
                    LunaCard {
                        Bullet(tr("Le verrouillage par code est activé.", "The PIN lock is on.", pl = "Blokada kodem jest włączona.", uk = "Блокування кодом увімкнено.", es = "El bloqueo por código está activado.", de = "Die Codesperre ist aktiviert."))
                    }
                    LunaButton(continueLabel, onClick = { goNext() }, icon = Icons.Filled.Check)
                } else {
                    LunaButton(tr("Choisir un code", "Choose a PIN", pl = "Wybierz kod", uk = "Обрати код", es = "Elegir un código", de = "Code wählen"), onClick = { showPinSetup = true })
                    LunaButton(later, onClick = { goNext() }, style = LunaButtonStyle.Quiet)
                }
                if (showPinSetup) {
                    PinSetupDialog(
                        onSavePin = { pin ->
                            showPinSetup = false
                            onPinChosen(pin)
                        },
                        onDismiss = { showPinSetup = false },
                    )
                }
            }
            OnboardingStep.REMINDERS -> {
                PageTitle(tr("Les rappels", "Reminders", pl = "Przypomnienia", uk = "Нагадування", es = "Recordatorios", de = "Erinnerungen"), onBack = back)
                Text(
                    tr("Lunaison peut vous prévenir deux jours avant vos prochaines règles, et vous rappeler de noter vos journées autour des règles. Les notifications restent discrètes sur l'écran verrouillé.", "Lunaison can let you know two days before your next period, and remind you to log your days around your period. Notifications stay discreet on the lock screen.", pl = "Lunaison może dać Ci znać dwa dni przed kolejną miesiączką i przypominać o notowaniu dni wokół miesiączki. Powiadomienia są dyskretne na ekranie blokady.", uk = "Lunaison може попередити вас за два дні до наступних місячних і нагадувати записувати дні навколо них. На заблокованому екрані сповіщення залишаються непомітними.", es = "Lunaison puede avisarte dos días antes de tu próxima regla y recordarte que anotes tus días alrededor de la regla. Las notificaciones son discretas en la pantalla de bloqueo.", de = "Lunaison kann dich zwei Tage vor deiner nächsten Periode benachrichtigen und dich erinnern, deine Tage rund um die Periode einzutragen. Auf dem Sperrbildschirm bleiben die Benachrichtigungen diskret."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Muted(tr("Un rappel de prise (pilule, traitement) se règle dans les réglages. Vous pourrez tout changer plus tard.", "A reminder to take a pill or treatment can be set in the settings. You can change everything later.", pl = "Przypomnienie o tabletce lub leczeniu ustawisz w ustawieniach. Wszystko możesz zmienić później.", uk = "Нагадування про таблетку чи лікування налаштовується в налаштуваннях. Усе можна змінити пізніше.", es = "El recordatorio de la píldora o del tratamiento se configura en los ajustes. Puedes cambiarlo todo más tarde.", de = "Die Erinnerung an Pille oder Behandlung stellst du in den Einstellungen ein. Du kannst alles später ändern."))
                val finishButtons: @Composable () -> Unit = {
                    LunaButton(
                        tr("Découvrir l'accueil", "Go to the home screen", pl = "Przejdź do ekranu głównego", uk = "Перейти на головний екран", es = "Ir a la pantalla de inicio", de = "Zum Startbildschirm"),
                        onClick = { onFinish(baseline()) },
                        icon = Icons.Filled.Check,
                    )
                    LunaButton(
                        tr("Noter ma journée maintenant", "Log my day now", pl = "Zanotuj mój dzień teraz", uk = "Записати мій день зараз", es = "Anotar mi día ahora", de = "Meinen Tag jetzt eintragen"),
                        onClick = { onFinishAndLog(baseline()) },
                        style = LunaButtonStyle.Outlined,
                    )
                }
                when {
                    notificationsOn -> {
                        LunaCard {
                            Bullet(tr("Les notifications sont autorisées. Vous pourrez choisir vos rappels dans les réglages.", "Notifications are allowed. You can choose your reminders in the settings.", pl = "Powiadomienia są dozwolone. Przypomnienia wybierzesz w ustawieniach.", uk = "Сповіщення дозволено. Нагадування можна обрати в налаштуваннях.", es = "Las notificaciones están permitidas. Podrás elegir tus recordatorios en los ajustes.", de = "Benachrichtigungen sind erlaubt. Deine Erinnerungen wählst du in den Einstellungen."))
                        }
                        finishButtons()
                    }
                    notificationsAsked -> {
                        LunaCard {
                            Bullet(tr("Les notifications sont refusées : aucun rappel ne sera envoyé. Vous pouvez les autoriser à tout moment dans les réglages d'Android.", "Notifications are blocked: no reminder will be sent. You can allow them at any time in Android settings.", pl = "Powiadomienia są zablokowane: żadne przypomnienie nie zostanie wysłane. Możesz je włączyć w dowolnej chwili w ustawieniach Androida.", uk = "Сповіщення заблоковано: нагадування не надходитимуть. Ви можете дозволити їх будь-коли в налаштуваннях Android.", es = "Las notificaciones están bloqueadas: no se enviará ningún aviso. Puedes permitirlas en cualquier momento en los ajustes de Android.", de = "Benachrichtigungen sind blockiert: Es wird keine Erinnerung gesendet. Du kannst sie jederzeit in den Android-Einstellungen erlauben."))
                        }
                        LunaButton(
                            tr("Ouvrir les réglages d'Android", "Open Android settings", pl = "Otwórz ustawienia Androida", uk = "Відкрити налаштування Android", es = "Abrir los ajustes de Android", de = "Android-Einstellungen öffnen"),
                            onClick = { openNotificationSettings(context) },
                            style = LunaButtonStyle.Outlined,
                        )
                        finishButtons()
                    }
                    else -> {
                        Muted(tr("Android va vous demander une autorisation : vous pouvez refuser, l'appli fonctionne sans.", "Android will ask for permission: you can say no, the app works without it.", pl = "Android poprosi o zgodę: możesz odmówić, aplikacja działa także bez niej.", uk = "Android попросить дозвіл: можна відмовити, застосунок працює й без нього.", es = "Android te pedirá permiso: puedes rechazar, la aplicación funciona sin él.", de = "Android fragt nach der Erlaubnis: Du kannst ablehnen, die App funktioniert auch ohne."))
                        LunaButton(
                            tr("Autoriser les notifications", "Allow notifications", pl = "Zezwól na powiadomienia", uk = "Дозволити сповіщення", es = "Permitir notificaciones", de = "Benachrichtigungen erlauben"),
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsAsked = true
                                    notificationsOn = notificationsAllowed(context)
                                }
                            },
                        )
                        LunaButton(later, onClick = { onFinish(baseline()) }, style = LunaButtonStyle.Quiet)
                    }
                }
            }
        }
    }
}

/** Les notifications peuvent être envoyées : autorisation accordée (Android 13 et plus) et non coupées dans les réglages d'Android. */
private fun notificationsAllowed(context: Context): Boolean =
    runCatching { NotificationManagerCompat.from(context).areNotificationsEnabled() }.getOrDefault(false)

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

/** Réglage d'une valeur avec deux gros boutons, sans clavier. [what] précise la valeur pour TalkBack. */
@Composable
fun Stepper(label: String, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier, what: String = "") {
    val minus = tr("Moins un", "Minus one", pl = "Minus jeden", uk = "Мінус один", es = "Menos uno", de = "Minus eins")
    val plus = tr("Plus un", "Plus one", pl = "Plus jeden", uk = "Плюс один", es = "Más uno", de = "Plus eins")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(onClick = onMinus, modifier = Modifier.semantics { contentDescription = if (what.isEmpty()) minus else "$minus, $what" }) {
            Text("−", style = MaterialTheme.typography.headlineMedium)
        }
        Text(label, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = onPlus, modifier = Modifier.semantics { contentDescription = if (what.isEmpty()) plus else "$plus, $what" }) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
