package fr.lunaison.screenshots

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityOptionsCompat
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.data.Partner
import fr.lunaison.core.data.PartnerChannel
import fr.lunaison.core.designsystem.LunaisonTheme
import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import fr.lunaison.ui.Actions
import fr.lunaison.ui.AppState
import fr.lunaison.ui.ArticleScreen
import fr.lunaison.ui.CalendarScreen
import fr.lunaison.ui.HomeScreen
import fr.lunaison.ui.LearnScreen
import fr.lunaison.ui.LogScreen
import fr.lunaison.ui.OnboardingScreen
import fr.lunaison.ui.SettingsScreen
import fr.lunaison.ui.ShareScreen
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

/** Données d'exemple : quatre cycles notés, avec des symptômes qui reviennent avant les règles. */
object Sample {
    val today: LocalDate = LocalDate.of(2026, 11, 4)
    private val starts = listOf(
        LocalDate.of(2026, 7, 13),
        LocalDate.of(2026, 8, 11),
        LocalDate.of(2026, 9, 8),
        LocalDate.of(2026, 10, 8),
    )

    fun logs(until: LocalDate = today): List<DayLog> = buildList {
        starts.forEachIndexed { index, start ->
            val flows = listOf(Flow.MEDIUM, Flow.HEAVY, Flow.MEDIUM, Flow.LIGHT, Flow.SPOTTING)
            flows.forEachIndexed { day, flow ->
                add(
                    DayLog(
                        start.plusDays(day.toLong()),
                        flow = flow,
                        pain = if (day == 0) Pain.MODERATE else if (day == 1) Pain.MILD else null,
                        symptoms = if (day == 0) setOf(Symptom.FATIGUE, Symptom.BACK_PAIN) else emptySet(),
                    ),
                )
            }
            if (index > 0) {
                add(DayLog(start.minusDays(3), symptoms = setOf(Symptom.BREAST_TENDERNESS, Symptom.BLOATING), moods = setOf(Mood.IRRITABLE)))
                add(DayLog(start.minusDays(2), symptoms = setOf(Symptom.BREAST_TENDERNESS), moods = setOf(Mood.SENSITIVE)))
            }
            add(DayLog(start.plusDays(10), moods = setOf(Mood.ENERGETIC, Mood.HAPPY)))
        }
        add(DayLog(today, symptoms = setOf(Symptom.BLOATING), moods = setOf(Mood.CALM), note = tr("Grosse journée, mais ça va.", "Busy day, but I'm okay.")))
    }.filter { !it.date.isAfter(until) }

    val settings = AppSettings(
        onboarded = true,
        baseline = Baseline(AgeGroup.ADULT),
        partner = Partner("Camille", "+33 6 12 34 56 78", PartnerChannel.SIGNAL),
    )

    fun state(today: LocalDate = Sample.today, logs: List<DayLog> = logs(today)) = AppState.compute(logs, settings, today)

    val periodToday: LocalDate = LocalDate.of(2026, 10, 9)
}

private object NoActions : Actions {
    override fun save(log: DayLog) = Unit
    override fun updateSettings(transform: (AppSettings) -> AppSettings) = Unit
    override fun eraseAll() = Unit
    override suspend fun exportCsv(): String = ""
}

/**
 * Une série de captures dans une langue : [ScreensTest] en français (docs/captures),
 * [ScreensEnTest] en anglais (docs/captures/en). Les textes de l'appli suivent la langue de la JVM.
 */
abstract class LocalizedScreens(private val locale: Locale) {

    init {
        Locale.setDefault(locale)
    }

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5.copy(locale = locale.language), maxPercentDifference = 0.1)

    // Les écrans qui ouvrent le sélecteur de contacts ou de fichiers ont besoin d'un registre, même factice.
    private val registryOwner = object : ActivityResultRegistryOwner {
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?) = Unit
        }
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        paparazzi.snapshot(name) {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                LunaisonTheme(dark = dark) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
                }
            }
        }
    }

    @Test
    fun captures() {
        val state = Sample.state()
        shot("bienvenue") { OnboardingScreen(today = Sample.today, onFinish = {}) }
        shot("accueil") { HomeScreen(state, NoActions, { _, _ -> }, {}, {}, {}) }
        shot("accueil_sombre", dark = true) { HomeScreen(state, NoActions, { _, _ -> }, {}, {}, {}) }
        shot("accueil_regles") { HomeScreen(Sample.state(Sample.periodToday), NoActions, { _, _ -> }, {}, {}, {}) }
        shot("journee") { LogScreen(state, Sample.today, periodStart = false, onSave = {}, onBack = {}) }
        shot("calendrier") { CalendarScreen(state, onDay = {}) }
        shot("comprendre") { LearnScreen(onArticle = {}) }
        shot("fiche_douleurs") { ArticleScreen("douleurs", onBack = {}) }
        shot("partage") { ShareScreen(state, NoActions, onBack = {}, onArticle = {}) }
        shot("reglages") { SettingsScreen(state, NoActions, versionName = "0.1.0", onArticle = {}) }
    }
}

class ScreensTest : LocalizedScreens(Locale.FRENCH)

class ScreensEnTest : LocalizedScreens(Locale.ENGLISH)
