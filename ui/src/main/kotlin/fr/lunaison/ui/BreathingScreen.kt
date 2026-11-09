package fr.lunaison.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr
import kotlinx.coroutines.delay

/**
 * Exercice guidé de respiration lente (inspiration 4 s, expiration longue 6 s)
 * pour relâcher le périnée, le bas-ventre et apaiser les crampes ou la tension prémenstruelle.
 */
@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var running by rememberSaveable { mutableStateOf(false) }
    var inhaling by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(4) }
    var completedCycles by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(running) {
        if (!running) {
            inhaling = true
            secondsLeft = 4
            return@LaunchedEffect
        }
        while (running) {
            delay(1_000)
            if (secondsLeft > 1) {
                secondsLeft -= 1
            } else {
                if (inhaling) {
                    inhaling = false
                    secondsLeft = 6
                } else {
                    inhaling = true
                    secondsLeft = 4
                    completedCycles += 1
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (!running) 0.78f else if (inhaling) 1.0f else 0.68f,
        animationSpec = tween(durationMillis = if (inhaling) 4_000 else 6_000),
        label = "breathing_scale",
    )

    val instruction = when {
        !running -> tr("Prête à commencer", "Ready to begin", pl = "Gotowe do rozpoczęcia", uk = "Готово до початку", es = "Todo listo para empezar", de = "Bereit zum Start")
        inhaling -> tr("Inspirez doucement par le nez", "Breathe in gently through your nose", pl = "Wdychaj powoli nosem", uk = "Вдихайте повільно через ніс", es = "Inspira suavemente por la nariz", de = "Atme sanft durch die Nase ein")
        else -> tr("Expirez lentement en relâchant le ventre", "Breathe out slowly, relaxing your belly", pl = "Wydychaj powoli, rozluźniając brzuch", uk = "Видихайте повільно, розслабляючи живіт", es = "Espira despacio relajando el vientre", de = "Atme langsam aus und entspanne dabei den Bauch")
    }

    Page {
        PageTitle(
            tr("Respirer et relâcher", "Breathe and relax", pl = "Oddychaj i odpuść", uk = "Дихайте й розслабтеся", es = "Respirar y relajar", de = "Atmen und entspannen"),
            onBack = onBack,
            subtitle = tr("Respiration abdominale apaisante (4 s / 6 s)", "Soothing abdominal breathing (4 s / 6 s)", pl = "Kojące oddychanie brzuszne (4 s / 6 s)", uk = "Заспокійливе дихання животом (4 с / 6 с)", es = "Respiración abdominal relajante (4 s / 6 s)", de = "Beruhigende Bauchatmung (4 s / 6 s)"),
        )

        LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(168.dp)
                        .scale(scale),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (running) "${secondsLeft}s" else "4s / 6s",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            if (completedCycles > 0) {
                                Text(
                                    tr("$completedCycles cycles", "$completedCycles cycles", pl = "cykle oddechowe: $completedCycles", uk = "дихальні цикли: $completedCycles", es = "$completedCycles ciclos", de = "$completedCycles Zyklen"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
            }
            Text(
                instruction,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            LunaButton(
                if (running) tr("Mettre en pause", "Pause", pl = "Wstrzymaj", uk = "Призупинити", es = "Pausar", de = "Pausieren") else tr("Commencer (3 minutes conseillées)", "Start (3 minutes recommended)", pl = "Zacznij (zalecane 3 minuty)", uk = "Почати (рекомендовано 3 хвилини)", es = "Empezar (se recomiendan 3 minutos)", de = "Starten (3 Minuten empfohlen)"),
                onClick = { running = !running },
                style = if (running) LunaButtonStyle.Outlined else LunaButtonStyle.Primary,
            )
        }

        SectionTitle(tr("Pourquoi l'expiration longue aide", "Why a longer exhale helps", pl = "Dlaczego długi wydech pomaga", uk = "Чому довгий видих допомагає", es = "Por qué ayuda espirar más tiempo", de = "Warum ein langes Ausatmen hilft"))
        LunaCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Bullet(tr("Une expiration plus longue que l'inspiration (6 secondes contre 4) active le système nerveux parasympathique et réduit la perception de la douleur.", "Exhaling longer than you inhale (6 seconds vs. 4) activates the parasympathetic nervous system and lowers pain perception.", pl = "Wydech dłuższy niż wdech (6 sekund wobec 4) aktywuje przywspółczulny układ nerwowy i zmniejsza odczuwanie bólu.", uk = "Видих, довший за вдих (6 секунд проти 4), активує парасимпатичну нервову систему та знижує сприйняття болю.", es = "Una espiración más larga que la inspiración (6 segundos frente a 4) activa el sistema nervioso parasimpático y reduce la percepción del dolor.", de = "Ein längeres Ausatmen als Einatmen (6 Sekunden gegenüber 4) aktiviert das parasympathische Nervensystem und verringert die Schmerzwahrnehmung."))
                Bullet(tr("En laissant le bas-ventre et le périnée se détendre à chaque expiration, on diminue la crispation musculaire autour de l'utérus.", "Letting your lower belly and pelvic floor soften on each exhale reduces muscle tension around the uterus.", pl = "Gdy przy każdym wydechu podbrzusze i dno miednicy się rozluźniają, zmniejsza się napięcie mięśni wokół macicy.", uk = "Коли на кожному видиху низ живота й тазове дно розслабляються, зменшується напруження м'язів навколо матки.", es = "Al dejar que el bajo vientre y el suelo pélvico se relajen en cada espiración, disminuye la tensión muscular alrededor del útero.", de = "Wenn sich Unterbauch und Beckenboden bei jedem Ausatmen entspannen, lässt die Muskelspannung rund um die Gebärmutter nach."))
                Bullet(tr("Trois minutes (soit 18 respirations) suffisent souvent à apaiser un pic de crampe ou de tension en fin de cycle.", "Three minutes (about 18 breaths) is often enough to ease a cramp peak or late-cycle tension.", pl = "Trzy minuty (czyli 18 oddechów) często wystarczą, by złagodzić szczyt skurczu lub napięcie pod koniec cyklu.", uk = "Три хвилини (тобто 18 вдихів) часто достатньо, щоб полегшити пік спазму чи напруження наприкінці циклу.", es = "Tres minutos (es decir, 18 respiraciones) suelen bastar para aliviar un pico de calambres o la tensión al final del ciclo.", de = "Drei Minuten (also 18 Atemzüge) reichen oft aus, um einen Krampfschub oder die Anspannung am Zyklusende zu lindern."))
            }
        }
    }
}
