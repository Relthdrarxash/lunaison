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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.data.ImportSummary
import fr.lunaison.core.data.LunaisonRepository
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

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
fun LunaisonApp(repository: LunaisonRepository, versionName: String) {
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

    if (!currentSettings.onboarded) {
        OnboardingScreen(
            today = today,
            onFinish = { baseline ->
                scope.launch {
                    repository.updateSettings { it.copy(onboarded = true, baseline = baseline) }
                }
            },
            onFinishAsPartner = { snapshot ->
                scope.launch {
                    repository.updateSettings {
                        it.copy(
                            onboarded = true,
                            followedPartner = snapshot,
                        )
                    }
                }
            },
        )
        return
    }

    val state = remember(currentLogs, currentSettings, today) { AppState.compute(currentLogs, currentSettings, today) }
    val actions = remember(repository) { RepositoryActions(repository, scope) }
    MainScaffold(state, actions, versionName)
}

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
private fun MainScaffold(state: AppState, actions: Actions, versionName: String) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = Tab.entries.any { it.route == route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
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
                    },
                    onBack = { navController.popBackStack() },
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
