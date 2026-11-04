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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import fr.lunaison.core.model.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(today: LocalDate, onFinish: (Baseline) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var ageGroup by rememberSaveable { mutableStateOf<AgeGroup?>(null) }
    var lastStart by rememberSaveable { mutableStateOf<String?>(null) }
    var usualLength by rememberSaveable { mutableStateOf<Int?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun finish() {
        onFinish(Baseline(ageGroup ?: AgeGroup.ADULT, lastStart?.let(LocalDate::parse), usualLength))
    }

    Page(Modifier.safeDrawingPadding()) {
        when (step) {
            0 -> {
                PageTitle(tr("Bienvenue dans Lunaison", "Welcome to Lunaison"))
                Text(tr("Pour suivre votre cycle, comprendre ce qui se passe et savoir quand vos règles arrivent.", "To track your cycle, understand what is happening and know when your period is coming."), style = MaterialTheme.typography.bodyLarge)
                LunaCard {
                    Text(tr("Vos données restent sur ce téléphone", "Your data stays on this phone"), style = MaterialTheme.typography.titleMedium)
                    Bullet(tr("Pas de compte, pas de serveur, pas de publicité, pas de traceur.", "No account, no server, no ads, no trackers."))
                    Bullet(tr("Lunaison n'a même pas accès à Internet.", "Lunaison does not even have Internet access."))
                    Bullet(tr("Rien n'est partagé sans que vous l'envoyiez vous-même.", "Nothing is shared unless you send it yourself."))
                    Bullet(tr("Vous pouvez tout effacer en un geste.", "You can erase everything in one tap."))
                }
                Muted(tr("Lunaison aide à s'organiser et à mieux se connaître. Ce n'est ni un dispositif médical ni une méthode de contraception.", "Lunaison helps you plan and get to know yourself better. It is neither a medical device nor a method of contraception."))
                LunaButton(tr("Commencer", "Get started"), onClick = { step = 1 })
            }
            1 -> {
                PageTitle(tr("Quel âge avez-vous ?", "How old are you?"), onBack = { step = 0 })
                Text(
                    tr("Les repères d'un cycle normal ne sont pas les mêmes les premières années après les premières règles. Votre réponse reste sur ce téléphone.", "What counts as a normal cycle is different in the first years after your first period. Your answer stays on this phone."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LunaButton(tr("Moins de 18 ans", "Under 18"), onClick = { ageGroup = AgeGroup.UNDER_18; step = 2 }, style = LunaButtonStyle.Outlined)
                LunaButton(tr("18 ans ou plus", "18 or over"), onClick = { ageGroup = AgeGroup.ADULT; step = 2 }, style = LunaButtonStyle.Outlined)
            }
            2 -> {
                PageTitle(tr("Vos dernières règles", "Your last period"), onBack = { step = 1 })
                Text(tr("Quel jour ont-elles commencé ? Une date approximative suffit.", "What day did it start? An approximate date is enough."), style = MaterialTheme.typography.bodyLarge)
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
                    if (selected != null) tr("Le ${Format.dayMonth(selected)}", "${Format.dayMonth(selected)}") else tr("Choisissez une date", "Choose a date"),
                    onClick = { lastStart = selected?.toString(); step = 3 },
                    enabled = selected != null,
                    icon = Icons.Filled.Check,
                )
                LunaButton(tr("Je ne sais plus", "I don't remember"), onClick = { lastStart = null; step = 3 }, style = LunaButtonStyle.Quiet)
            }
            3 -> {
                PageTitle(tr("Votre cycle habituel", "Your usual cycle"), onBack = { step = 2 })
                Text(
                    tr("Combien de jours entre le début de vos règles et le début des suivantes, en général ? Si vous ne savez pas, Lunaison l'apprendra au fil des cycles.", "How many days between the start of one period and the start of the next, usually? If you don't know, Lunaison will learn it over your cycles."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                val value = usualLength ?: 28
                Stepper(
                    label = tr("$value jours", "$value days"),
                    onMinus = { usualLength = (value - 1).coerceAtLeast(18) },
                    onPlus = { usualLength = (value + 1).coerceAtMost(60) },
                )
                LunaButton(tr("Continuer", "Continue"), onClick = { usualLength = value; step = 4 })
                LunaButton(tr("Je ne sais pas", "I don't know"), onClick = { usualLength = null; step = 4 }, style = LunaButtonStyle.Quiet)
            }
            else -> {
                PageTitle(tr("Les rappels", "Reminders"), onBack = { step = 3 })
                Text(
                    tr("Lunaison peut vous prévenir deux jours avant vos prochaines règles, et vous rappeler de noter vos journées autour des règles. Les notifications restent discrètes sur l'écran verrouillé.", "Lunaison can let you know two days before your next period, and remind you to log your days around your period. Notifications stay discreet on the lock screen."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Muted(tr("Vous pourrez tout changer dans les réglages.", "You can change everything in the settings."))
                LunaButton(
                    tr("Autoriser les notifications", "Allow notifications"),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        finish()
                    },
                )
                LunaButton(tr("Plus tard", "Later"), onClick = { finish() }, style = LunaButtonStyle.Quiet)
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
        FilledTonalIconButton(onClick = onMinus, modifier = Modifier.semantics { contentDescription = tr("Moins un", "Minus one") }) {
            Text("−", style = MaterialTheme.typography.headlineMedium)
        }
        Text(label, style = MaterialTheme.typography.headlineMedium)
        FilledTonalIconButton(onClick = onPlus) { Icon(Icons.Filled.Add, contentDescription = tr("Plus un", "Plus one")) }
    }
}
