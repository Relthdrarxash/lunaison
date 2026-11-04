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
                PageTitle("Bienvenue dans Lunaison")
                Text("Pour suivre votre cycle, comprendre ce qui se passe et savoir quand vos règles arrivent.", style = MaterialTheme.typography.bodyLarge)
                LunaCard {
                    Text("Vos données restent sur ce téléphone", style = MaterialTheme.typography.titleMedium)
                    Bullet("Pas de compte, pas de serveur, pas de publicité, pas de traceur.")
                    Bullet("Lunaison n'a même pas accès à Internet.")
                    Bullet("Rien n'est partagé sans que vous l'envoyiez vous-même.")
                    Bullet("Vous pouvez tout effacer en un geste.")
                }
                Muted("Lunaison aide à s'organiser et à mieux se connaître. Ce n'est ni un dispositif médical ni une méthode de contraception.")
                LunaButton("Commencer", onClick = { step = 1 })
            }
            1 -> {
                PageTitle("Quel âge avez-vous ?", onBack = { step = 0 })
                Text(
                    "Les repères d'un cycle normal ne sont pas les mêmes les premières années après les premières règles. Votre réponse reste sur ce téléphone.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                LunaButton("Moins de 18 ans", onClick = { ageGroup = AgeGroup.UNDER_18; step = 2 }, style = LunaButtonStyle.Outlined)
                LunaButton("18 ans ou plus", onClick = { ageGroup = AgeGroup.ADULT; step = 2 }, style = LunaButtonStyle.Outlined)
            }
            2 -> {
                PageTitle("Vos dernières règles", onBack = { step = 1 })
                Text("Quel jour ont-elles commencé ? Une date approximative suffit.", style = MaterialTheme.typography.bodyLarge)
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
                    if (selected != null) "Le ${Format.dayMonth(selected)}" else "Choisissez une date",
                    onClick = { lastStart = selected?.toString(); step = 3 },
                    enabled = selected != null,
                    icon = Icons.Filled.Check,
                )
                LunaButton("Je ne sais plus", onClick = { lastStart = null; step = 3 }, style = LunaButtonStyle.Quiet)
            }
            3 -> {
                PageTitle("Votre cycle habituel", onBack = { step = 2 })
                Text(
                    "Combien de jours entre le début de vos règles et le début des suivantes, en général ? Si vous ne savez pas, Lunaison l'apprendra au fil des cycles.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                val value = usualLength ?: 28
                Stepper(
                    label = "$value jours",
                    onMinus = { usualLength = (value - 1).coerceAtLeast(18) },
                    onPlus = { usualLength = (value + 1).coerceAtMost(60) },
                )
                LunaButton("Continuer", onClick = { usualLength = value; step = 4 })
                LunaButton("Je ne sais pas", onClick = { usualLength = null; step = 4 }, style = LunaButtonStyle.Quiet)
            }
            else -> {
                PageTitle("Les rappels", onBack = { step = 3 })
                Text(
                    "Lunaison peut vous prévenir deux jours avant vos prochaines règles, et vous rappeler de noter vos journées autour des règles. Les notifications restent discrètes sur l'écran verrouillé.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Muted("Vous pourrez tout changer dans les réglages.")
                LunaButton(
                    "Autoriser les notifications",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        finish()
                    },
                )
                LunaButton("Plus tard", onClick = { finish() }, style = LunaButtonStyle.Quiet)
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
        FilledTonalIconButton(onClick = onMinus, modifier = Modifier.semantics { contentDescription = "Moins un" }) {
            Text("−", style = MaterialTheme.typography.headlineMedium)
        }
        Text(label, style = MaterialTheme.typography.headlineMedium)
        FilledTonalIconButton(onClick = onPlus) { Icon(Icons.Filled.Add, contentDescription = "Plus un") }
    }
}
