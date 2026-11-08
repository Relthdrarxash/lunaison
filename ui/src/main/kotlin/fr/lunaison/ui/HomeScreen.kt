package fr.lunaison.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
            SectionTitle(tr("Comprendre sa phase actuelle", "Understand their current phase", pl = "Zrozum jej obecną fazę", uk = "Зрозуміти поточну фазу партнерки", es = "Entender su fase actual", de = "Die aktuelle Phase verstehen"))
            LunaCard {
                Text(guide.phaseTitle, style = MaterialTheme.typography.titleMedium)
                Text(guide.whatPartnerShouldKnow, style = MaterialTheme.typography.bodyMedium)
                Text(
                    tr("Comment soutenir concrètement :", "Concrete ways to help:", pl = "Konkretne sposoby wsparcia:", uk = "Як конкретно підтримати:", es = "Formas concretas de ayudar:", de = "Konkrete Unterstützung:"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                guide.supportTips.forEach { Bullet(it) }
                TextButton(onClick = { onArticle("entourage") }) {
                    Text(tr("Lire la fiche : accompagner un ou une proche", "Read the guide: supporting a partner", pl = "Przeczytaj poradnik: wspieranie partnerki", uk = "Прочитати довідку: як підтримати партнерку", es = "Leer la guía: acompañar a tu pareja", de = "Ratgeber lesen: Partnerin unterstützen"))
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

        RecentDaysStrip(
            today = state.today,
            state = state,
            onDayClick = { date -> onLog(date, false) },
        )

        when {
            paused -> LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    tr("Prévisions en pause", "Predictions paused", pl = "Prognozy wstrzymane", uk = "Прогнози призупинено", es = "Previsiones en pausa", de = "Vorhersagen pausiert"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    tr("Les estimations de règles sont en pause (grossesse, allaitement ou contraception continue). Vous pouvez continuer à noter vos journées librement.", "Period estimates are paused (pregnancy, breastfeeding, or continuous contraception). You can keep logging your days freely.", pl = "Prognozy miesiączki są wstrzymane (ciąża, karmienie piersią lub antykoncepcja ciągła). Możesz dalej swobodnie notować swoje dni.", uk = "Прогнози місячних призупинено (вагітність, годування груддю або безперервна контрацепція). Ви можете й далі вільно записувати свої дні.", es = "Las estimaciones de la regla están en pausa (embarazo, lactancia o anticoncepción continua). Puedes seguir anotando tus días libremente.", de = "Die Periodenschätzungen sind pausiert (Schwangerschaft, Stillzeit oder durchgehende Verhütung). Du kannst deine Tage weiter frei eintragen."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                TextButton(onClick = { actions.updateSettings { it.copy(pausePredictions = false) } }) {
                    Text(tr("Réactiver les prévisions", "Resume predictions", pl = "Wznów prognozy", uk = "Відновити прогнози", es = "Reanudar previsiones", de = "Vorhersagen fortsetzen"), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            cycle is CycleState.Unknown -> LunaCard {
                Text(tr("Notez vos dernières règles pour commencer", "Log your last period to get started", pl = "Zanotuj ostatnią miesiączkę, aby zacząć", uk = "Запишіть останні місячні, щоб почати", es = "Anota tu última regla para empezar", de = "Trag deine letzte Periode ein, um zu beginnen"), style = MaterialTheme.typography.titleLarge)
                Text(tr("Dès que Lunaison connaît le début de vos dernières règles, il estime les suivantes.", "As soon as Lunaison knows when your last period started, it estimates the next ones.", pl = "Gdy tylko Lunaison pozna początek ostatniej miesiączki, oszacuje kolejne.", uk = "Щойно Lunaison дізнається, коли почалися останні місячні, він оцінить наступні.", es = "En cuanto Lunaison sepa cuándo empezó tu última regla, estimará las siguientes.", de = "Sobald Lunaison weiß, wann deine letzte Periode begann, schätzt es die nächsten."), style = MaterialTheme.typography.bodyMedium)
                LunaButton(tr("Ouvrir le calendrier", "Open the calendar", pl = "Otwórz kalendarz", uk = "Відкрити календар", es = "Abrir el calendario", de = "Kalender öffnen"), onClick = onCalendar)
            }
            cycle is CycleState.Known -> CycleCard(
                cycle,
                showFertility = state.settings.showFertility,
                // Probabilité de grossesse : seulement si la personne a choisi d'afficher la fertilité, et majeure.
                showPregnancyOdds = state.settings.showFertility && state.settings.isAdult,
                onArticle = onArticle,
            )
        }

        // Actions du jour.
        val inPeriod = (cycle as? CycleState.Known)?.inPeriod == true
        if (!paused && !inPeriod && todayLog.flow?.isBleeding != true) {
            LunaButton(
                tr("Mes règles ont commencé", "My period has started", pl = "Zaczęła się miesiączka", uk = "Місячні почалися", es = "Me ha venido la regla", de = "Meine Periode hat begonnen"),
                onClick = {
                    actions.startPeriod(todayLog)
                    onLog(state.today, true)
                },
                style = LunaButtonStyle.Accent,
                icon = Icons.Filled.Add,
            )
        }
        LunaButton(
            if (todayLog.isEmpty) tr("Noter ma journée", "Log my day", pl = "Zanotuj mój dzień", uk = "Записати мій день", es = "Anotar mi día", de = "Meinen Tag eintragen") else tr("Modifier ma journée", "Edit my day", pl = "Edytuj mój dzień", uk = "Змінити мій день", es = "Editar mi día", de = "Meinen Tag bearbeiten"),
            onClick = { onLog(state.today, false) },
            style = LunaButtonStyle.Primary,
            icon = Icons.Filled.Edit,
        )
        if (!todayLog.isEmpty) TodaySummary(todayLog, state.settings.reminders.medicationLabel)

        // Ce qui demande une attention (retard, alerte, récap) passe avant le partage et le contenu éducatif.
        if (!paused && (cycle as? CycleState.Known)?.possiblePregnancyHint == true) {
            LunaCard {
                Text(tr("Retard prolongé : à savoir", "Extended delay: good to know", pl = "Dłuższe opóźnienie: warto wiedzieć", uk = "Тривала затримка: варто знати", es = "Retraso prolongado: conviene saberlo", de = "Längere Verspätung: gut zu wissen"), style = MaterialTheme.typography.titleMedium)
                Text(
                    tr("Un retard peut venir du stress, d'un changement de rythme, d'une maladie ou d'une grossesse. En cas de rapport susceptible d'entraîner une grossesse, un test urinaire est fiable dès le premier jour de retard présumé (ou 3 semaines après le rapport).", "A delay can be caused by stress, a change in routine, illness, or pregnancy. If pregnancy is possible, a home urine test is reliable from the first day of a missed period (or 3 weeks after intercourse).", pl = "Opóźnienie może wynikać ze stresu, zmiany rytmu dnia, choroby lub ciąży. Jeśli ciąża jest możliwa, test z moczu jest wiarygodny od pierwszego dnia spodziewanej miesiączki (lub 3 tygodnie po stosunku).", uk = "Затримка може бути спричинена стресом, зміною режиму, хворобою або вагітністю. Якщо вагітність можлива, тест на сечу надійний з першого дня очікуваних місячних (або через 3 тижні після статевого акту).", es = "Un retraso puede deberse al estrés, a un cambio de rutina, a una enfermedad o a un embarazo. Si es posible un embarazo, un test de orina es fiable desde el primer día de retraso (o 3 semanas después de la relación).", de = "Eine Verspätung kann durch Stress, einen veränderten Alltag, Krankheit oder eine Schwangerschaft entstehen. Ist eine Schwangerschaft möglich, ist ein Urintest ab dem ersten Tag der ausgebliebenen Periode zuverlässig (oder 3 Wochen nach dem Geschlechtsverkehr)."),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { actions.updateSettings { it.copy(pausePredictions = true) } }) {
                        Text(tr("Mettre les prévisions en pause", "Pause predictions", pl = "Wstrzymaj prognozy", uk = "Призупинити прогнози", es = "Pausar previsiones", de = "Vorhersagen pausieren"))
                    }
                    TextButton(onClick = { onArticle("consulter") }) {
                        Text(tr("En savoir plus", "Learn more", pl = "Dowiedz się więcej", uk = "Докладніше", es = "Saber más", de = "Mehr erfahren"))
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

        if (state.settings.isAdult) {
            val partner = state.settings.partner
            val shareLabel = if (partner != null) tr("Prévenir ${partner.name}", "Let ${partner.name} know", pl = "Daj znać: ${partner.name}", uk = "Повідомити: ${partner.name}", es = "Avisar a ${partner.name}", de = "${partner.name} Bescheid geben") else tr("Partager", "Share", pl = "Udostępnij", uk = "Поділитися", es = "Compartir", de = "Teilen")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LunaButton(
                    tr("Respirer", "Breathe", pl = "Oddychaj", uk = "Дихати", es = "Respirar", de = "Atmen"),
                    onClick = onBreathe,
                    style = LunaButtonStyle.Outlined,
                    modifier = Modifier.weight(1f),
                )
                LunaButton(
                    shareLabel,
                    onClick = onShare,
                    style = LunaButtonStyle.Outlined,
                    icon = Icons.Filled.Share,
                    modifier = Modifier.weight(1f),
                )
            }
            if (followed != null && followedCycle != null) {
                PartnerSnapshotCard(
                    snapshot = followed,
                    cycle = followedCycle,
                    onRescan = { showPartnerScanner = true },
                    onRemove = { actions.updateSettings { it.copy(followedPartner = null) } },
                )
            } else {
                LunaButton(
                    tr("Suivre un cycle partenaire", "Follow a partner's cycle", pl = "Śledź cykl partnerki", uk = "Стежити за циклом партнерки", es = "Seguir el ciclo de tu pareja", de = "Zyklus der Partnerin verfolgen"),
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
        } else {
            LunaButton(
                tr("Respirer", "Breathe", pl = "Oddychaj", uk = "Дихати", es = "Respirar", de = "Atmen"),
                onClick = onBreathe,
                style = LunaButtonStyle.Outlined,
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
            PhaseCard(state, cycle, onArticle)
            // Prévisions détaillées et hormones : repliées par défaut pour garder l'accueil court.
            var showDetails by rememberSaveable { mutableStateOf(false) }
            LunaButton(
                if (showDetails) {
                    tr("Masquer prévisions et hormones", "Hide forecast and hormones", pl = "Ukryj prognozę i hormony", uk = "Сховати прогноз і гормони", es = "Ocultar previsión y hormonas", de = "Vorschau und Hormone ausblenden")
                } else {
                    tr("Prévisions des 7 jours et hormones", "7-day forecast and hormones", pl = "Prognoza na 7 dni i hormony", uk = "Прогноз на 7 днів і гормони", es = "Previsión de 7 días y hormonas", de = "7-Tage-Vorschau und Hormone")
                },
                onClick = { showDetails = !showDetails },
                style = LunaButtonStyle.Quiet,
            )
            if (showDetails) {
                UpcomingForecastCard(cycle)
                PhaseHormonesCard(cycle.phase, onArticle)
            }
            if (cycle.stats.completedCycles >= CycleConfig.MIN_CYCLES_FOR_STATS) {
                LunaButton(tr("Mes cycles", "My cycles", pl = "Moje cykle", uk = "Мої цикли", es = "Mis ciclos", de = "Meine Zyklen"), onClick = onInsights, style = LunaButtonStyle.Quiet, icon = Icons.Filled.DateRange)
            }
        }
    }
}

@Composable
private fun RecapCard(recap: CycleRecap, onInsights: () -> Unit, onDismiss: () -> Unit) {
    LunaCard {
        Text(tr("Votre cycle qui vient de finir", "Your cycle that just ended", pl = "Cykl, który właśnie się skończył", uk = "Цикл, що щойно завершився", es = "Tu ciclo que acaba de terminar", de = "Dein gerade beendeter Zyklus"), style = MaterialTheme.typography.titleMedium)
        InsightTexts.recapLines(recap).forEach { Bullet(it) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide", pl = "Ukryj", uk = "Сховати", es = "Ocultar", de = "Ausblenden")) }
            TextButton(onClick = onInsights) { Text(tr("Mes cycles", "My cycles", pl = "Moje cykle", uk = "Мої цикли", es = "Mis ciclos", de = "Meine Zyklen")) }
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
private fun CycleCard(cycle: CycleState.Known, showFertility: Boolean, showPregnancyOdds: Boolean, onArticle: (String) -> Unit) {
    val phaseName = Format.phaseName(cycle.phase)
    LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(84.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = tr("Jour ${cycle.dayOfCycle} du cycle", "Cycle day ${cycle.dayOfCycle}", pl = "Dzień cyklu ${cycle.dayOfCycle}", uk = "День циклу ${cycle.dayOfCycle}", es = "Día ${cycle.dayOfCycle} del ciclo", de = "Zyklustag ${cycle.dayOfCycle}") },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tr("Jour", "Day", pl = "Dzień", uk = "День", es = "Día", de = "Tag"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                        Text("${cycle.dayOfCycle}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
            Column {
                Text(phaseName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (!cycle.inPeriod) {
                    Text(tr("Phase estimée", "Estimated phase", pl = "Szacowana faza", uk = "Орієнтовна фаза", es = "Fase estimada", de = "Geschätzte Phase"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Text(Format.nextPeriod(cycle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (!cycle.inPeriod && cycle.daysLate == 0) {
            Text(Format.range(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (showFertility) {
                Text(Format.fertileWindow(cycle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    tr("Repère de calendrier : ne remplace pas une contraception.", "Calendar estimate: does not replace contraception.", pl = "Szacunek kalendarzowy: nie zastępuje antykoncepcji.", uk = "Календарна оцінка: не замінює контрацепцію.", es = "Estimación de calendario: no sustituye a un anticonceptivo.", de = "Kalenderschätzung: ersetzt keine Verhütung."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Probabilités estimées de grossesse au jour le jour
        if (showPregnancyOdds) Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        tr("Chances de grossesse :", "Pregnancy likelihood:", pl = "Szansa na ciążę:", uk = "Імовірність вагітності:", es = "Probabilidad de embarazo:", de = "Schwangerschaftswahrscheinlichkeit:"),
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
                Text(tr("Pourquoi cette plage ?", "Why this range?", pl = "Skąd ten przedział?", uk = "Чому такий діапазон?", es = "¿Por qué este intervalo?", de = "Warum dieser Zeitraum?"), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun UpcomingForecastCard(cycle: CycleState.Known) {
    val forecasts = remember(cycle) { cycle.upcomingDayForecasts(7) }
    SectionTitle(tr("Dans les 7 prochains jours", "In the next 7 days", pl = "W ciągu najbliższych 7 dni", uk = "Протягом наступних 7 днів", es = "En los próximos 7 días", de = "In den nächsten 7 Tagen"))
    LunaCard {
        Text(
            tr("Aperçu prévisionnel jour par jour :", "Day-by-day forecast overview:", pl = "Prognoza dzień po dniu:", uk = "Прогноз день за днем:", es = "Previsión día a día:", de = "Vorschau Tag für Tag:"),
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
                    modifier = Modifier.width(118.dp),
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(Format.dayOfWeek(f.date), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
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
private fun PhaseHormonesCard(phase: Phase, onArticle: (String) -> Unit = {}) {
    val profile = remember(phase) { PhaseHormoneInfo.profile(phase) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    SectionTitle(tr("Hormones & dynamique du corps", "Hormones & body dynamics", pl = "Hormony i dynamika ciała", uk = "Гормони та динаміка тіла", es = "Hormonas y dinámica del cuerpo", de = "Hormone & Körperdynamik"))
    LunaCard {
        Text(profile.title, style = MaterialTheme.typography.titleMedium)
        Text(profile.hormones.summary, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tr("Œstrogènes :", "Estrogen:", pl = "Estrogeny:", uk = "Естрогени:", es = "Estrógenos:", de = "Östrogene:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(profile.hormones.estrogen.label, style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.weight(1f)) {
                Text(tr("Progestérone :", "Progesterone:", pl = "Progesteron:", uk = "Прогестерон:", es = "Progesterona:", de = "Progesteron:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(profile.hormones.progesterone.label, style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.weight(1f)) {
                Text(tr("LH / FSH :", "LH / FSH:", pl = "LH / FSH:", uk = "ЛГ / ФСГ:", es = "LH / FSH:", de = "LH / FSH:"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text("${profile.hormones.lh.label} / ${profile.hormones.fsh.label}", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (expanded) {
            Text(tr("Forces & atouts de cette phase :", "Strengths & advantages:", pl = "Mocne strony tej fazy:", uk = "Сильні сторони цієї фази:", es = "Fortalezas de esta fase:", de = "Stärken dieser Phase:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            profile.strengths.forEach { Bullet(it) }
            Text(tr("Défis & ce qui peut gêner :", "Possible challenges:", pl = "Możliwe trudności:", uk = "Можливі труднощі:", es = "Posibles dificultades:", de = "Mögliche Herausforderungen:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            profile.challenges.forEach { Bullet(it) }
            Text(tr("Ce qui vous soutient :", "What supports you:", pl = "Co Cię wspiera:", uk = "Що вас підтримує:", es = "Lo que te ayuda:", de = "Was dich unterstützt:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            profile.supportAdvice.forEach { Bullet(it) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) tr("Moins de détails", "Show less", pl = "Mniej szczegółów", uk = "Менше деталей", es = "Menos detalles", de = "Weniger anzeigen") else tr("Atouts & conseils", "Strengths & advice", pl = "Mocne strony i porady", uk = "Сильні сторони й поради", es = "Fortalezas y consejos", de = "Stärken & Tipps"))
            }
            TextButton(onClick = { onArticle("cycle") }) {
                Text(tr("Courbe des hormones", "Hormone curve", pl = "Krzywa hormonów", uk = "Крива гормонів", es = "Curva de hormonas", de = "Hormonkurve"))
            }
        }
    }
}

@Composable
private fun TodaySummary(log: DayLog, medicationLabel: String = "") {
    val parts = buildList {
        log.flow?.let { add(tr("Flux : ${it.label.lowercase()}", "Flow: ${it.label.lowercase()}", pl = "Krwawienie: ${it.label.lowercase()}", uk = "Виділення: ${it.label.lowercase()}", es = "Flujo: ${it.label.lowercase()}", de = "Blutung: ${it.label.lowercase()}")) }
        log.pain?.let { add(tr("Douleur : ${it.label.lowercase()}", "Pain: ${it.label.lowercase()}", pl = "Ból: ${it.label.lowercase()}", uk = "Біль: ${it.label.lowercase()}", es = "Dolor: ${it.label.lowercase()}", de = "Schmerz: ${it.label.lowercase()}")) }
        if (log.symptoms.isNotEmpty()) add(log.symptoms.joinToString(", ") { it.label.lowercase() })
        if (log.moods.isNotEmpty()) add(tr("Humeur : ", "Mood: ", pl = "Nastrój: ", uk = "Настрій: ", es = "Estado de ánimo: ", de = "Stimmung: ") + log.moods.joinToString(", ") { it.label.lowercase() })
        val pill = log.pillStatus
        if (pill != null) {
            add(tr("Pilule : ${pill.label.lowercase()}", "Pill: ${pill.label.lowercase()}", pl = "Tabletka: ${pill.label.lowercase()}", uk = "Таблетка: ${pill.label.lowercase()}", es = "Píldora: ${pill.label.lowercase()}", de = "Pille: ${pill.label.lowercase()}"))
        } else if (log.medicationTaken) {
            val label = medicationLabel.trim().ifBlank { tr("contraception ou traitement", "contraception or medication", pl = "antykoncepcja lub lek", uk = "контрацепція або ліки", es = "anticonceptivo o tratamiento", de = "Verhütung oder Medikament") }
            add(tr("Pris aujourd'hui : $label", "Taken today: $label", pl = "Przyjęte dziś: $label", uk = "Прийнято сьогодні: $label", es = "Tomado hoy: $label", de = "Heute genommen: $label"))
        }
        if (log.medications.isNotEmpty()) add(tr("Médicaments : ", "Medications: ", pl = "Leki: ", uk = "Ліки: ", es = "Medicamentos: ", de = "Medikamente: ") + log.medications.joinToString(", ") { it.label.lowercase() })
        log.weight?.let { add(tr("Poids : ${"%.1f".format(java.util.Locale.FRENCH, it)} kg", "Weight: ${"%.1f".format(java.util.Locale.ENGLISH, it)} kg")) }
        if (log.intimacy.isNotEmpty()) add(tr("Intimité : ", "Intimacy: ", pl = "Intymność: ", uk = "Інтимність: ", es = "Intimidad: ", de = "Intimität: ") + log.intimacy.joinToString(", ") { it.label.lowercase() })
        log.libido?.let { add(tr("Désir : ${it.label.lowercase()}", "Desire: ${it.label.lowercase()}", pl = "Pożądanie: ${it.label.lowercase()}", uk = "Бажання: ${it.label.lowercase()}", es = "Deseo: ${it.label.lowercase()}", de = "Lust: ${it.label.lowercase()}")) }
        log.mucus?.let { add(tr("Glaire : ${it.label.lowercase()}", "Mucus: ${it.label.lowercase()}", pl = "Śluz: ${it.label.lowercase()}", uk = "Слиз: ${it.label.lowercase()}", es = "Flujo cervical: ${it.label.lowercase()}", de = "Zervixschleim: ${it.label.lowercase()}")) }
        log.basalTemperature?.let { add(tr("Température : ${"%.1f".format(java.util.Locale.FRENCH, it)} °C", "Temperature: ${"%.1f".format(java.util.Locale.ENGLISH, it)} °C")) }
    }
    LunaCard {
        Text(tr("Noté aujourd'hui", "Logged today", pl = "Zanotowane dziś", uk = "Записано сьогодні", es = "Anotado hoy", de = "Heute eingetragen"), style = MaterialTheme.typography.titleMedium)
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
            TextButton(onClick = onDismiss) { Text(tr("Masquer", "Hide", pl = "Ukryj", uk = "Сховати", es = "Ocultar", de = "Ausblenden")) }
            TextButton(onClick = onRead) { Text(tr("Quand consulter", "When to see a doctor", pl = "Kiedy iść do lekarza", uk = "Коли звернутися до лікаря", es = "Cuándo consultar", de = "Wann zum Arzt")) }
        }
    }
}

@Composable
private fun PhaseCard(state: AppState, cycle: CycleState.Known, onArticle: (String) -> Unit) {
    val guide = PhaseGuides.forPhase(cycle.phase)
    val pattern = PhasePatterns.compute(state.logs, cycle, cycle.phase)
    val tip = DailyTip.pick(guide, state.today, state.logs.firstOrNull { it.date == state.today })
    SectionTitle(tr("Ce qui se passe en ce moment", "What is happening right now", pl = "Co się teraz dzieje", uk = "Що відбувається зараз", es = "Lo que está pasando ahora", de = "Was gerade passiert"))
    // Ce qui est propre à la personne passe avant les conseils généraux.
    if (!pattern.isEmpty) {
        LunaCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(tr("Ce qui revient chez vous", "What comes back for you", pl = "Co u Ciebie powraca", uk = "Що у вас повторюється", es = "Lo que se repite en ti", de = "Was bei dir wiederkehrt"), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                tr("Pendant cette phase, lors de vos cycles précédents, vous avez souvent noté :", "During this phase, in your previous cycles, you often logged:", pl = "W tej fazie, w poprzednich cyklach, często notowałaś:", uk = "У цій фазі в попередніх циклах ви часто записували:", es = "En esta fase, en tus ciclos anteriores, anotaste a menudo:", de = "In dieser Phase hast du in früheren Zyklen oft eingetragen:"),
                style = MaterialTheme.typography.bodyMedium,
            )
            pattern.symptoms.forEach { (symptom, count) -> Bullet(tr("${symptom.label} (${count} cycles sur ${pattern.cycles})", "${symptom.label} (${count} of ${pattern.cycles} cycles)", pl = "${symptom.label} (${count} z ${pattern.cycles} cykli)", uk = "${symptom.label} (${count} з ${pattern.cycles} циклів)", es = "${symptom.label} (${count} de ${pattern.cycles} ciclos)", de = "${symptom.label} (${count} von ${pattern.cycles} Zyklen)")) }
            pattern.moods.forEach { (mood, count) -> Bullet(tr("${mood.label} (${count} cycles sur ${pattern.cycles})", "${mood.label} (${count} of ${pattern.cycles} cycles)", pl = "${mood.label} (${count} z ${pattern.cycles} cykli)", uk = "${mood.label} (${count} з ${pattern.cycles} циклів)", es = "${mood.label} (${count} de ${pattern.cycles} ciclos)", de = "${mood.label} (${count} von ${pattern.cycles} Zyklen)")) }
        }
    }
    LunaCard {
        Text(guide.name, style = MaterialTheme.typography.titleMedium)
        Text(guide.whatHappens, style = MaterialTheme.typography.bodyMedium)
        if (tip != null) {
            Text(tr("Le conseil du jour", "Tip of the day", pl = "Wskazówka dnia", uk = "Порада дня", es = "Consejo del día", de = "Tipp des Tages"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Text(tip.text, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { onArticle(guide.articleId) }) { Text(tr("En savoir plus", "Learn more", pl = "Dowiedz się więcej", uk = "Докладніше", es = "Saber más", de = "Mehr erfahren")) }
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

/** Ruban des jours récents sur l'accueil permettant de visualiser et noter les jours en 1 tap avec swipe par semaine. */
@Composable
private fun RecentDaysStrip(
    today: LocalDate,
    state: AppState,
    onDayClick: (LocalDate) -> Unit,
) {
    var weekOffset by rememberSaveable { mutableIntStateOf(0) }
    var totalDrag by remember { mutableFloatStateOf(0f) }
    val startOffset = 6 + weekOffset * 7
    val endOffset = weekOffset * 7
    val days = remember(today, weekOffset) { (startOffset downTo endOffset).map { today.minusDays(it.toLong()) } }
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(weekOffset) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -50f && weekOffset > 0) {
                            weekOffset--
                        } else if (totalDrag > 50f && weekOffset < 8) {
                            weekOffset++
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            }
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (weekOffset > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr("Il y a $weekOffset sem. (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})", "$weekOffset w. ago (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})", pl = "$weekOffset tyg. temu (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})", uk = "$weekOffset тиж. тому (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})", es = "Hace $weekOffset sem. (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})", de = "Vor $weekOffset Wo. (${Format.dayMonth(days.first())} – ${Format.dayMonth(days.last())})"),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                )
                Text(
                    tr("Aujourd'hui", "Today", pl = "Dzisiaj", uk = "Сьогодні", es = "Hoy", de = "Heute"),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondary,
                    modifier = Modifier.clickable { weekOffset = 0 },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.forEach { date ->
                val isToday = date == today
                val log = state.logFor(date)
                val hasBleeding = log.flow?.isBleeding == true
                val hasOtherLog = !log.isEmpty && !hasBleeding

                val shortWeekday = Format.narrowWeekday(date)

                val containerColor = when {
                    isToday -> colors.primaryContainer
                    hasBleeding -> colors.secondaryContainer.copy(alpha = 0.7f)
                    hasOtherLog -> colors.surfaceVariant.copy(alpha = 0.7f)
                    else -> colors.surfaceVariant.copy(alpha = 0.35f)
                }
                val contentColor = when {
                    isToday -> colors.onPrimaryContainer
                    hasBleeding -> colors.onSecondaryContainer
                    else -> colors.onSurfaceVariant
                }

                Surface(
                    onClick = { onDayClick(date) },
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    border = if (isToday) BorderStroke(1.5.dp, colors.primary) else null,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .semantics { contentDescription = Format.weekday(date) },
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            shortWeekday,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.75f),
                        )
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = contentColor,
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    color = when {
                                        hasBleeding -> colors.secondary
                                        hasOtherLog -> colors.primary
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}
