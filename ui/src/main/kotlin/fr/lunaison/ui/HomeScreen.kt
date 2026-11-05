package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.DailyTip
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.content.PhaseGuides
import fr.lunaison.core.content.PhaseHormoneInfo
import fr.lunaison.core.content.ShareMessages
import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleConfig
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.cycle.PhasePatterns
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.DayLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import fr.lunaison.core.model.tr

@Composable
fun HomeScreen(
    state: AppState,
    actions: Actions,
    onLog: (date: LocalDate, periodStart: Boolean) -> Unit,
    onArticle: (String) -> Unit,
    onShare: () -> Unit,
    onCalendar: () -> Unit,
    onInsights: () -> Unit = {},
    onBreathe: () -> Unit = {},
) {
    val cycle = state.cycle
    val todayLog = state.logFor(state.today)
    val paused = state.settings.pausePredictions
    val followed = state.settings.followedPartner
    val followedCycle = state.followedPartnerCycle
    var showPartnerScanner by rememberSaveable { mutableStateOf(false) }
    var showSetupOwnCycle by rememberSaveable { mutableStateOf(false) }

    Page {
        PageTitle(Format.weekday(state.today))

        if (!paused && cycle is CycleState.Unknown && followed != null && followedCycle != null && todayLog.isEmpty) {
            PartnerSnapshotCard(
                snapshot = followed,
                cycle = followedCycle,
                onRescan = { showPartnerScanner = true },
                onRemove = { actions.updateSettings { it.copy(followedPartner = null) } },
            )
            val guide = remember(followedCycle.phase) { ShareMessages.partnerGuide(followedCycle.phase) }
            SectionTitle(tr("Comprendre sa phase actuelle", "Understand their current phase"))
            LunaCard {
                Text(guide.phaseTitle, style = MaterialTheme.typography.titleMedium)
                Text(guide.whatPartnerShouldKnow, style = MaterialTheme.typography.bodyMedium)
                Text(
                    tr("Comment soutenir concrètement :", "Concrete ways to help:"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                guide.supportTips.forEach { Bullet(it) }
                TextButton(onClick = { onArticle("entourage") }) {
                    Text(tr("Lire la fiche : accompagner un ou une proche", "Read the guide: supporting a partner"))
                }
            }
            LunaButton(
                tr(
                    "Suivre aussi mon propre cycle",
                    "Also track my own cycle",
                    pl = "Śledź również mój własny cykl",
                    uk = "Стежити також за власним циклом",
                    es = "Seguir también mi propio ciclo",
                    de = "Auch meinen eigenen Zyklus begleiten",
                ),
                onClick = { showSetupOwnCycle = true },
                style = LunaButtonStyle.Quiet,
                icon = Icons.Filled.DateRange,
            )
            if (showPartnerScanner) {
                QrScannerDialog(
                    existingPartner = followed,
                    onScanned = { scanned ->
                        showPartnerScanner = false
                        actions.updateSettings { it.copy(followedPartner = scanned) }
                    },
                    onDismiss = { showPartnerScanner = false },
                )
            }
            if (showSetupOwnCycle) {
                SetupOwnCycleDialog(
                    today = state.today,
                    onConfirm = { startDate ->
                        showSetupOwnCycle = false
                        actions.updateSettings { it.copy(baseline = it.baseline.copy(lastPeriodStart = startDate)) }
                    },
                    onOpenCalendar = {
                        showSetupOwnCycle = false
                        onCalendar()
                    },
                    onDismiss = { showSetupOwnCycle = false },
                )
            }
            return@Page
        }

        when {
            paused -> LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    tr("Prévisions en pause", "Predictions paused"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    tr("Les estimations de règles sont en pause (grossesse, allaitement ou contraception continue). Vous pouvez continuer à noter vos journées librement.", "Period estimates are paused (pregnancy, breastfeeding, or continuous contraception). You can keep logging your days freely."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                TextButton(onClick = { actions.updateSettings { it.copy(pausePredictions = false) } }) {
                    Text(tr("Réactiver les prévisions", "Resume predictions"), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            cycle is CycleState.Unknown -> LunaCard {
                Text(tr("Notez vos dernières règles pour commencer", "Log your last period to get started"), style = MaterialTheme.typography.titleLarge)
                Text(tr("Dès que Lunaison connaît le début de vos dernières règles, il estime les suivantes.", "As soon as Lunaison knows when your last period started, it estimates the next ones."), style = MaterialTheme.typography.bodyMedium)
                LunaButton(tr("Ouvrir le calendrier", "Open the calendar"), onClick = onCalendar)
            }
            cycle is CycleState.Known -> CycleCard(cycle, state.settings.showFertility, onArticle)
        }

        // Actions du jour.
        val inPeriod = (cycle as? CycleState.Known)?.inPeriod == true
        if (!paused && !inPeriod && todayLog.flow?.isBleeding != true) {
            LunaButton(
                tr("Mes règles ont commencé", "My period has started"),
                onClick = {
                    actions.startPeriod(todayLog)
                    onLog(state.today, true)
                },
                style = LunaButtonStyle.Accent,
                icon = Icons.Filled.Add,
            )
        }
        LunaButton(
            if (todayLog.isEmpty) tr("Noter ma journée", "Log my day") else tr("Modifier ma journée", "Edit my day"),
            onClick = { onLog(state.today, false) },
            style = LunaButtonStyle.Primary,
            icon = Icons.Filled.Edit,
        )
        if (!todayLog.isEmpty) TodaySummary(todayLog, state.settings.reminders.medicationLabel)

        LunaButton(
            tr("Respirer et relâcher (douleur, stress)", "Breathe and relax (pain, stress)"),
            onClick = onBreathe,
            style = LunaButtonStyle.Outlined,
        )

        if (state.settings.isAdult) {
            val partner = state.settings.partner
            LunaButton(
                if (partner != null) tr("Prévenir ${partner.name}", "Let ${partner.name} know") else tr("Partager avec un ou une partenaire", "Share with a partner"),
                onClick = onShare,
                style = LunaButtonStyle.Outlined,
                icon = Icons.Filled.Share,
            )
            if (followed != null && followedCycle != null) {
                PartnerSnapshotCard(
                    snapshot = followed,
                    cycle = followedCycle,
                    onRescan = { showPartnerScanner = true },
                    onRemove = { actions.updateSettings { it.copy(followedPartner = null) } },
                )
            } else {
                LunaButton(
                    tr(
                        "Suivre le cycle d'un ou d'une partenaire (QR / NFC / SMS)",
                        "Follow a partner's cycle (QR / NFC / SMS)",
                        pl = "Śledź cykl partnera (QR / NFC / SMS)",
                        uk = "Стежити за циклом партнера (QR / NFC / SMS)",
                        es = "Seguir el ciclo de una pareja (QR / NFC / SMS)",
                        de = "Zyklus eines Partners begleiten (QR / NFC / SMS)",
                    ),
                    onClick = { showPartnerScanner = true },
                    style = LunaButtonStyle.Quiet,
                    icon = Icons.Filled.Favorite,
                )
            }
            if (showPartnerScanner) {
                QrScannerDialog(
                    existingPartner = followed,
                    onScanned = { scanned ->
                        showPartnerScanner = false
                        actions.updateSettings { it.copy(followedPartner = scanned) }
                    },
                    onDismiss = { showPartnerScanner = false },
                )
            }
        }

        if (!paused && (cycle as? CycleState.Known)?.possiblePregnancyHint == true) {
            LunaCard {
                Text(tr("Retard prolongé : à savoir", "Extended delay: good to know"), style = MaterialTheme.typography.titleMedium)
                Text(
                    tr("Un retard peut venir du stress, d'un changement de rythme, d'une maladie ou d'une grossesse. En cas de rapport susceptible d'entraîner une grossesse, un test urinaire est fiable dès le premier jour de retard présumé (ou 3 semaines après le rapport).", "A delay can be caused by stress, a change in routine, illness, or pregnancy. If pregnancy is possible, a home urine test is reliable from the first day of a missed period (or 3 weeks after intercourse)."),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { actions.updateSettings { it.copy(pausePredictions = true) } }) {
                        Text(tr("Mettre les prévisions en pause", "Pause predictions"))
                    }
                    TextButton(onClick = { onArticle("consulter") }) {
                        Text(tr("En savoir plus", "Learn more"))
                    }
                }
            }
        }

        state.alerts.forEach { alert ->
            AlertCard(
                alert = alert,
                onRead = { onArticle(alert.articleId) },
                onDismiss = {
                    actions.updateSettings { it.copy(dismissedAlerts = it.dismissedAlerts + (alert to state.today)) }
                },
            )
        }

        state.recap?.let { recap ->
            RecapCard(
                recap = recap,
                onInsights = onInsights,
                onDismiss = { actions.updateSettings { it.copy(recapSeen = recap.record.start) } },
            )
        }

        val completedCycles = (cycle as? CycleState.Known)?.stats?.completedCycles ?: 0
        if (!state.settings.reviewPromptDismissed && completedCycles >= 2) {
            val context = LocalContext.current
            ReviewPromptCard(
                onReview = {
                    actions.updateSettings { it.copy(reviewPromptDismissed = true) }
                    openStore(context)
                },
                onDismiss = {
                    actions.updateSettings { it.copy(reviewPromptDismissed = true) }
                },
            )
        }

        if (!paused && cycle is CycleState.Known) {
            UpcomingForecastCard(cycle)
            PhaseHormonesCard(cycle.phase)
            PhaseCard(state, cycle, onArticle)
            if (cycle.stats.completedCycles >= CycleConfig.MIN_CYCLES_FOR_STATS) {
                LunaButton(tr("Mes cycles", "My cycles"), onClick = onInsights, style = LunaButtonStyle.Quiet, icon = Icons.Filled.DateRange)
            }
        }
    }
}

@Composable
private fun RecapCard(recap: CycleRecap, onInsights: () -> Unit, onDismiss: () -> Unit) {
    LunaCard {
        Text(tr("Votre cycle qui vient de finir", "Your cycle that just ended"), style = MaterialTheme.typography.titleMedium)
        InsightTexts.recapLines(recap).forEach { Bullet(it) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide")) }
            TextButton(onClick = onInsights) { Text(tr("Mes cycles", "My cycles")) }
        }
    }
}

@Composable
private fun ReviewPromptCard(onReview: () -> Unit, onDismiss: () -> Unit) {
    LunaCard {
        Text(
            tr(
                "Vous utilisez Lunaison depuis quelques cycles",
                "You've been using Lunaison for a few cycles",
                pl = "Używasz Lunaison od kilku cykli",
                uk = "Ви користуєтеся Lunaison уже кілька циклів",
                es = "Llevas varios ciclos usando Lunaison",
                de = "Du nutzt Lunaison seit einigen Zyklen",
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "Lunaison est une application libre, gratuite, sans publicité et sans serveur. Si elle vous est utile au quotidien, laisser un mot doux ou une note sur le Play Store aide d'autres personnes à la trouver face aux applications payantes.",
                "Lunaison is a free and open-source app with no ads and no servers. If it helps you in your daily life, leaving a warm review or rating on the Play Store helps others find it among paid alternatives.",
                pl = "Lunaison to darmowa, otwarta aplikacja bez reklam i bez serwerów. Jeśli pomaga Ci na co dzień, miłe słowo lub ocena w sklepie Play pomoże innym odnaleźć ją wśród płatnych aplikacji.",
                uk = "Lunaison — це вільний, безкоштовний застосунок без реклами та серверів. Якщо він корисний для вас у щоденному житті, теплий відгук чи оцінка в Play Store допоможуть іншим знайти його серед платних додатків.",
                es = "Lunaison es una aplicación libre, gratuita, sin anuncios y sin servidores. Si te resulta útil en tu día a día, dejar unas palabras o una valoración en Google Play ayuda a que otras personas la descubran frente a las aplicaciones de pago.",
                de = "Lunaison ist eine freie, kostenlose App ohne Werbung und ohne Server. Wenn sie dir im Alltag hilft, unterstützt eine freundliche Bewertung im Play Store andere dabei, sie neben den kostenpflichtigen Apps zu finden.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) {
                Text(tr("Plus tard", "Not now", pl = "Później", uk = "Пізніше", es = "Más tarde", de = "Später"))
            }
            TextButton(onClick = onReview) {
                Text(tr("Laisser un avis", "Leave a review", pl = "Wystaw opinię", uk = "Залишити відгук", es = "Dejar una opinión", de = "Bewertung abgeben"))
            }
        }
    }
}

@Composable
private fun CycleCard(cycle: CycleState.Known, showFertility: Boolean, onArticle: (String) -> Unit) {
    val phaseName = Format.phaseName(cycle.phase)
    LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(84.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = tr("Jour ${cycle.dayOfCycle} du cycle", "Cycle day ${cycle.dayOfCycle}") },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tr("Jour", "Day"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text("${cycle.dayOfCycle}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
            Column {
                Text(phaseName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (!cycle.inPeriod) {
                    Text(tr("Phase estimée", "Estimated phase"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Text(Format.nextPeriod(cycle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            Text(Format.range(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (showFertility) {
                Text(Format.fertileWindow(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    tr("Repère de calendrier : ne remplace pas une contraception.", "Calendar estimate: does not replace contraception."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Probabilités estimées de grossesse au jour le jour
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        tr("Chances de grossesse :", "Pregnancy likelihood:"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        cycle.todayPregnancyOdds.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    cycle.todayPregnancyOdds.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Text(Format.confidence(cycle.stats.confidence), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            TextButton(onClick = { onArticle("previsions") }) {
                Text(tr("Pourquoi cette plage ?", "Why this range?"), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun UpcomingForecastCard(cycle: CycleState.Known) {
    val forecasts = remember(cycle) { cycle.upcomingDayForecasts(7) }
    SectionTitle(tr("Dans les 7 prochains jours", "In the next 7 days"))
    LunaCard {
        Text(
            tr("Aperçu prévisionnel jour par jour :", "Day-by-day forecast overview:"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            items(forecasts.size) { idx ->
                val f = forecasts[idx]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (f.inLikelyPeriod) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(112.dp),
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(Format.weekday(f.date).take(4), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        Text(Format.dayMonth(f.date), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                        Text("J${f.dayOfCycle}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(Format.phaseName(f.phase), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ) {
                            Text(
                                f.odds.label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseHormonesCard(phase: Phase) {
    val profile = remember(phase) { PhaseHormoneInfo.profile(phase) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    SectionTitle(tr("Hormones & dynamique du corps", "Hormones & body dynamics"))
    LunaCard {
        Text(profile.title, style = MaterialTheme.typography.titleMedium)
        Text(profile.hormones.summary, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tr("Œstrogènes :", "Estrogen:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(profile.hormones.estrogen.label, style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.weight(1f)) {
                Text(tr("Progestérone :", "Progesterone:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(profile.hormones.progesterone.label, style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.weight(1f)) {
                Text(tr("LH / FSH :", "LH / FSH:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text("${profile.hormones.lh.label} / ${profile.hormones.fsh.label}", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (expanded) {
            Text(tr("Forces & atouts de cette phase :", "Strengths & advantages:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            profile.strengths.forEach { Bullet(it) }
            Text(tr("Défis & ce qui peut gêner :", "Possible challenges:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            profile.challenges.forEach { Bullet(it) }
            Text(tr("Ce qui vous soutient :", "What supports you:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            profile.supportAdvice.forEach { Bullet(it) }
        }
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) tr("Moins de détails", "Show less") else tr("Atouts, défis et conseils de cette phase", "Strengths, challenges & advice"))
        }
    }
}

@Composable
private fun TodaySummary(log: DayLog, medicationLabel: String = "") {
    val parts = buildList {
        log.flow?.let { add(tr("Flux : ${it.label.lowercase()}", "Flow: ${it.label.lowercase()}")) }
        log.pain?.let { add(tr("Douleur : ${it.label.lowercase()}", "Pain: ${it.label.lowercase()}")) }
        if (log.symptoms.isNotEmpty()) add(log.symptoms.joinToString(", ") { it.label.lowercase() })
        if (log.moods.isNotEmpty()) add(tr("Humeur : ", "Mood: ") + log.moods.joinToString(", ") { it.label.lowercase() })
        val pill = log.pillStatus
        if (pill != null) {
            add(tr("Pilule : ${pill.label.lowercase()}", "Pill: ${pill.label.lowercase()}"))
        } else if (log.medicationTaken) {
            val label = medicationLabel.trim().ifBlank { tr("contraception ou traitement", "contraception or medication") }
            add(tr("Pris aujourd'hui : $label", "Taken today: $label"))
        }
        if (log.medications.isNotEmpty()) add(tr("Médicaments : ", "Medications: ") + log.medications.joinToString(", ") { it.label.lowercase() })
        log.weight?.let { add(tr("Poids : ${"%.1f".format(java.util.Locale.FRENCH, it)} kg", "Weight: ${"%.1f".format(java.util.Locale.ENGLISH, it)} kg")) }
        if (log.intimacy.isNotEmpty()) add(tr("Intimité : ", "Intimacy: ") + log.intimacy.joinToString(", ") { it.label.lowercase() })
        log.libido?.let { add(tr("Désir : ${it.label.lowercase()}", "Desire: ${it.label.lowercase()}")) }
        log.mucus?.let { add(tr("Glaire : ${it.label.lowercase()}", "Mucus: ${it.label.lowercase()}")) }
        log.basalTemperature?.let { add(tr("Température : ${"%.1f".format(java.util.Locale.FRENCH, it)} °C", "Temperature: ${"%.1f".format(java.util.Locale.ENGLISH, it)} °C")) }
    }
    LunaCard {
        Text(tr("Noté aujourd'hui", "Logged today"), style = MaterialTheme.typography.titleMedium)
        parts.forEach { Bullet(it.replaceFirstChar { c -> c.uppercase() }) }
        if (log.note.isNotBlank()) Muted(log.note)
    }
}

@Composable
private fun AlertCard(alert: AlertKind, onRead: () -> Unit, onDismiss: () -> Unit) {
    LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(alert.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide")) }
            TextButton(onClick = onRead) { Text(tr("Quand consulter", "When to see a doctor")) }
        }
    }
}

@Composable
private fun PhaseCard(state: AppState, cycle: CycleState.Known, onArticle: (String) -> Unit) {
    val guide = PhaseGuides.forPhase(cycle.phase)
    val pattern = PhasePatterns.compute(state.logs, cycle, cycle.phase)
    val tip = DailyTip.pick(guide, state.today, state.logs.firstOrNull { it.date == state.today })
    SectionTitle(tr("Ce qui se passe en ce moment", "What is happening right now"))
    // Ce qui est propre à la personne passe avant les conseils généraux.
    if (!pattern.isEmpty) {
        LunaCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(tr("Ce qui revient chez vous", "What comes back for you"), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                tr("Pendant cette phase, lors de vos cycles précédents, vous avez souvent noté :", "During this phase, in your previous cycles, you often logged:"),
                style = MaterialTheme.typography.bodyMedium,
            )
            pattern.symptoms.forEach { (symptom, count) -> Bullet(tr("${symptom.label} (${count} cycles sur ${pattern.cycles})", "${symptom.label} (${count} of ${pattern.cycles} cycles)")) }
            pattern.moods.forEach { (mood, count) -> Bullet(tr("${mood.label} (${count} cycles sur ${pattern.cycles})", "${mood.label} (${count} of ${pattern.cycles} cycles)")) }
        }
    }
    LunaCard {
        Text(guide.name, style = MaterialTheme.typography.titleMedium)
        Text(guide.whatHappens, style = MaterialTheme.typography.bodyMedium)
        if (tip != null) {
            Text(tr("Le conseil du jour", "Tip of the day"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Text(tip.text, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { onArticle(guide.articleId) }) { Text(tr("En savoir plus", "Learn more")) }
    }
    state.anticipation?.let { coming ->
        Muted(InsightTexts.anticipationLine(coming))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupOwnCycleDialog(
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onOpenCalendar: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                tr(
                    "Suivre aussi mon propre cycle",
                    "Also track my own cycle",
                    pl = "Śledź również mój własny cykl",
                    uk = "Стежити також за власним циклом",
                    es = "Seguir también mi propio ciclo",
                    de = "Auch meinen eigenen Zyklus begleiten",
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    tr(
                        "Vous suivez actuellement le cycle de votre partenaire. Vous pouvez également activer le suivi de votre propre cycle à tout moment.",
                        "You are currently following your partner's cycle. You can also activate tracking for your own cycle at any time.",
                        pl = "Obecnie śledzisz cykl partnera. W każdej chwili możesz włączyć śledzenie własnego cyklu.",
                        uk = "Зараз ви стежите за циклом партнера. Ви також можете в будь-який момент увімкнути відстеження власного циклу.",
                        es = "Actualmente sigues el ciclo de tu pareja. También puedes activar el seguimiento de tu propio ciclo en cualquier momento.",
                        de = "Du begleitest aktuell den Zyklus deines Partners. Du kannst die Begleitung deines eigenen Zyklus jederzeit aktivieren.",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    tr(
                        "Quel jour ont commencé vos dernières règles ?",
                        "When did your last period start?",
                        pl = "Kiedy rozpoczęła się Twoja ostatnia miesiączka?",
                        uk = "Коли почалася ваша остання менструація?",
                        es = "¿Cuándo empezó tu última menstruación?",
                        de = "Wann hat deine letzte Periode begonnen?",
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                LunaButton(
                    tr(
                        "Le ${Format.dayMonth(selectedDate)} (changer)",
                        "${Format.dayMonth(selectedDate)} (change)",
                        pl = "${Format.dayMonth(selectedDate)} (zmień)",
                        uk = "${Format.dayMonth(selectedDate)} (змінити)",
                        es = "${Format.dayMonth(selectedDate)} (cambiar)",
                        de = "${Format.dayMonth(selectedDate)} (ändern)",
                    ),
                    onClick = { showDatePicker = true },
                    style = LunaButtonStyle.Outlined,
                    icon = Icons.Filled.DateRange,
                )
                TextButton(onClick = onOpenCalendar) {
                    Text(
                        tr(
                            "Ouvrir le calendrier complet",
                            "Open full calendar",
                            pl = "Otwórz pełny kalendarz",
                            uk = "Відкрити повний календар",
                            es = "Abrir el calendario completo",
                            de = "Vollständigen Kalender öffnen",
                        ),
                    )
                }
            }
        },
        confirmButton = {
            LunaButton(
                tr(
                    "Activer mon cycle",
                    "Activate my cycle",
                    pl = "Włącz mój cykl",
                    uk = "Увімкнути мій цикл",
                    es = "Activar mi ciclo",
                    de = "Meinen Zyklus aktivieren",
                ),
                onClick = { onConfirm(selectedDate) },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"),
                )
            }
        },
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = remember(today) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        return !date.isAfter(today) && date.isAfter(today.minusDays(120))
                    }
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(tr("Valider", "OK", pl = "OK", uk = "OK", es = "Aceptar", de = "OK"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
