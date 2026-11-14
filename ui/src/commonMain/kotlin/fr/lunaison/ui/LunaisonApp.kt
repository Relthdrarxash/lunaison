package fr.lunaison.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.data.ImportSummary
import fr.lunaison.core.data.LunaisonRepository
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import fr.lunaison.core.model.now
import fr.lunaison.core.data.importAuto

private enum class Tab(
    val route: String,
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
    val icon: ImageVector,
) {
    HOME("accueil", "Aujourd'hui", "Today", "Dzisiaj", "Сьогодні", "Hoy", "Heute", Icons.Filled.Home),
    CALENDAR("calendrier", "Calendrier", "Calendar", "Kalendarz", "Календар", "Calendario", "Kalender", Icons.Filled.DateRange),
    LEARN("comprendre", "Comprendre", "Learn", "Wiedza", "Довідник", "Aprender", "Wissen", Icons.Filled.Info),
    SETTINGS("reglages", "Réglages", "Settings", "Ustawienia", "Налаштування", "Ajustes", "Einstellungen", Icons.Filled.Settings),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

@Composable
fun LunaisonApp(
    repository: LunaisonRepository,
    versionName: String,
    /** « log » : ouvrir la saisie du jour (rappel du soir), une fois l'appli déverrouillée. */
    link: String? = null,
    onLinkHandled: () -> Unit = {},
) {
    val logs by repository.logs.collectAsStateWithLifecycle(initialValue = null)
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    // La date change à minuit sans redémarrer l'appli.
    val today by produceState(LocalDate.now()) {
        while (true) {
            delay(60_000)
            value = LocalDate.now()
        }
    }
    val scope = rememberCoroutineScope()
    var unlocked by rememberSaveable { mutableStateOf(false) }
    // Fin d'onboarding avec « Noter ma journée maintenant » : même chemin qu'un lien de notification.
    var localLink by rememberSaveable { mutableStateOf<String?>(null) }
    // Accueil rouvert depuis les réglages : il se ferme sans rien effacer.
    var replayWelcome by rememberSaveable { mutableStateOf(false) }

    val currentLogs = logs
    val currentSettings = settings
    if (currentLogs == null || currentSettings == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    val expectedPinHash = currentSettings.pinHash
    if (expectedPinHash != null && !unlocked) {
        PinUnlockScreen(
            expectedHash = expectedPinHash,
            onUnlocked = { unlocked = true },
        )
        return
    }

    if (OnboardingFlow.shouldShow(currentSettings.onboarded) || replayWelcome) {
        val firstLaunch = !currentSettings.onboarded
        val onboardingActions = remember(repository) { RepositoryActions(repository, scope) }
        fun finish(baseline: Baseline, log: Boolean) {
            if (log) localLink = LINK_LOG
            replayWelcome = false
            scope.launch {
                repository.updateSettings { it.copy(onboarded = true, baseline = baseline) }
            }
        }
        OnboardingScreen(
            today = today,
            onFinish = { baseline -> finish(baseline, log = false) },
            onFinishAndLog = { baseline -> finish(baseline, log = true) },
            onFinishAsPartner = { snapshot ->
                replayWelcome = false
                scope.launch {
                    repository.updateSettings {
                        it.copy(
                            onboarded = true,
                            followedPartner = snapshot,
                        )
                    }
                }
            },
            current = if (firstLaunch) null else currentSettings.baseline,
            hasHistory = currentLogs.isNotEmpty(),
            pinEnabled = currentSettings.pinHash != null,
            // Le code choisi pendant l'accueil ne doit pas redemander le déverrouillage tout de suite.
            onPinChosen = { pin ->
                unlocked = true
                scope.launch { repository.updateSettings { it.copy(pinHash = AppSettings.hashPin(pin)) } }
            },
            actions = onboardingActions,
            onClose = if (firstLaunch) null else ({ replayWelcome = false }),
        )
        return
    }

    val state = remember(currentLogs, currentSettings, today) { AppState.compute(currentLogs, currentSettings, today) }
    val nextPrediction = (state.cycle as? CycleState.Known)?.next
    LaunchedEffect(currentSettings.reminders, nextPrediction?.start) {
        syncReminders(currentSettings, nextPrediction)
    }
    val actions = remember(repository) { RepositoryActions(repository, scope) }
    MainScaffold(
        state,
        actions,
        versionName,
        onReplayWelcome = { replayWelcome = true },
        link = link ?: localLink,
        onLinkHandled = {
            localLink = null
            onLinkHandled()
        },
    )
}

const val LINK_LOG = "log"

/** Ce que les écrans peuvent faire. Une interface, pour afficher les écrans sans base de données dans les captures. */
interface Actions {
    fun save(log: DayLog)

    /** « Mes règles ont commencé » : un flux moyen par défaut, modifiable ensuite. */
    fun startPeriod(existing: DayLog) {
        save(existing.copy(flow = existing.flow?.takeIf { it.isBleeding } ?: Flow.MEDIUM))
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings)
    fun eraseAll()
    suspend fun exportCsv(): String
    suspend fun importCsv(content: String): Int = 0
    suspend fun importAuto(bytes: ByteArray, password: String? = null): ImportSummary = ImportSummary(0, 0)
}

/** Les actions réelles, toutes enregistrées localement. */
class RepositoryActions(private val repository: LunaisonRepository, private val scope: kotlinx.coroutines.CoroutineScope) : Actions {
    override fun save(log: DayLog) {
        scope.launch { repository.save(log) }
    }

    override fun updateSettings(transform: (AppSettings) -> AppSettings) {
        scope.launch { repository.updateSettings(transform) }
    }

    override fun eraseAll() {
        scope.launch { repository.eraseAll() }
    }

    override suspend fun exportCsv(): String = repository.exportCsv()
    override suspend fun importCsv(content: String): Int = repository.importCsv(content)
    override suspend fun importAuto(bytes: ByteArray, password: String?): ImportSummary =
        repository.importAuto(bytes, password)
}

@Composable
private fun MainScaffold(
    state: AppState,
    actions: Actions,
    versionName: String,
    onReplayWelcome: () -> Unit,
    link: String? = null,
    onLinkHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = Tab.entries.any { it.route == route }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Lien de notification : la navigation n'est prête qu'une fois la première destination affichée.
    LaunchedEffect(link, route == null) {
        if (link == null || route == null) return@LaunchedEffect
        if (link == LINK_LOG) navController.navigate("jour/${state.today}") { launchSingleTop = true }
        onLinkHandled()
    }
    val savedMessage = tr("Journée enregistrée", "Day saved", pl = "Dzień zapisany", uk = "День збережено", es = "Día guardado", de = "Tag gespeichert")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBar) {
                val navLabel = tr(
                    "Navigation principale",
                    "Main navigation",
                    pl = "Główna nawigacja",
                    uk = "Основна навігація",
                    es = "Navegación principal",
                    de = "Hauptnavigation",
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.semantics { contentDescription = navLabel },
                ) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Tab.HOME.route, modifier = Modifier.padding(padding)) {
            composable(Tab.HOME.route) {
                HomeScreen(
                    state = state,
                    actions = actions,
                    onLog = { date, periodStart -> navController.navigate("jour/$date?debut=$periodStart") },
                    onArticle = { navController.navigate("fiche/$it") },
                    onShare = { navController.navigate("partage") },
                    onCalendar = { navController.openTab(Tab.CALENDAR.route) },
                    onInsights = { navController.navigate("cycles") },
                    onBreathe = { navController.navigate("respirer") },
                )
            }
            composable(Tab.CALENDAR.route) {
                CalendarScreen(
                    state = state,
                    onDay = { navController.navigate("jour/$it") },
                    onInsights = { navController.navigate("cycles") },
                    onHealthReport = { navController.navigate("bilan") },
                )
            }
            composable(Tab.LEARN.route) {
                LearnScreen(onArticle = { navController.navigate("fiche/$it") })
            }
            composable(Tab.SETTINGS.route) {
                SettingsScreen(
                    state = state,
                    actions = actions,
                    versionName = versionName,
                    onArticle = { navController.navigate("fiche/$it") },
                    onHealthReport = { navController.navigate("bilan") },
                    onReplayWelcome = onReplayWelcome,
                )
            }
            composable(
                "jour/{date}?debut={debut}",
                arguments = listOf(navArgument("debut") { type = NavType.BoolType; defaultValue = false }),
            ) { entry ->
                val date = entry.arguments?.getString("date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: state.today
                LogScreen(
                    state = state,
                    initialDate = date,
                    periodStart = entry.arguments?.getBoolean("debut") == true,
                    onSave = { log ->
                        actions.save(log)
                        navController.popBackStack()
                        // Confirmation discrète : la saisie est bien prise en compte.
                        scope.launch { snackbar.showSnackbar(savedMessage) }
                    },
                    onBack = { navController.popBackStack() },
                    onPersist = { log -> actions.save(log) },
                )
            }
            composable("fiche/{id}") { entry ->
                ArticleScreen(id = entry.arguments?.getString("id").orEmpty(), onBack = { navController.popBackStack() })
            }
            composable("cycles") {
                InsightsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onArticle = { navController.navigate("fiche/$it") },
                    onHealthReport = { navController.navigate("bilan") },
                )
            }
            composable("bilan") {
                HealthReportScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("respirer") {
                BreathingScreen(onBack = { navController.popBackStack() })
            }
            composable("partage") {
                ShareScreen(
                    state = state,
                    actions = actions,
                    onBack = { navController.popBackStack() },
                    onArticle = { navController.navigate("fiche/$it") },
                )
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
