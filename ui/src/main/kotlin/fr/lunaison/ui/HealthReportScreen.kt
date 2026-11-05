package fr.lunaison.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.tr
import java.util.Locale

/** Rubriques personnalisables pouvant être incluses ou masquées lors du bilan médical. */
enum class ReportSection(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    OVERVIEW("Synthèse & FIGO", "Overview & FIGO", "Podsumowanie", "Підсумок", "Resumen y FIGO", "Übersicht"),
    CYCLES_HISTORY("Historique des cycles", "Cycles history", "Historia cykli", "Історія циклів", "Historial de ciclos", "Zyklushistorie"),
    SYMPTOMS_AND_MOODS("Symptômes par phase", "Symptoms by phase", "Objawy według faz", "Симптоми за фазами", "Síntomas por fase", "Symptome je Phase"),
    MEDICATIONS("Traitements & pilule", "Medications & pill", "Leki i antykoncepcja", "Ліки та контрацепція", "Tratamiento y píldora", "Medikamente & Pille"),
    WEIGHT("Poids", "Weight", "Waga", "Вага", "Peso", "Gewicht"),
    OVULATION_SIGNS("Glaire & température", "Mucus & temp", "Śluz i temperatura", "Слиз і температура", "Moco y temperatura", "Schleim & Basaltemp."),
    INTIMACY("Vie intime", "Intimacy", "Życie intymne", "Інтимне життя", "Vida íntima", "Intimität"),
    NOTES("Notes personnelles", "Personal notes", "Notatki osobiste", "Особисті нотатки", "Notas personales", "Persönliche Notizen"),
    ALERTS("Points d'attention", "Points of attention", "Ważne uwagi", "Важливі моменти", "Puntos de atención", "Wichtige Hinweise"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/**
 * Bilan de santé complet et personnalisable à montrer ou partager avec un médecin, un gynécologue ou une sage-femme.
 * Permet de sélectionner les rubriques à inclure avant la consultation pour respecter l'intimité de la patiente.
 */
@Composable
fun HealthReportScreen(
    state: AppState,
    onBack: () -> Unit,
) {
    val cycle = state.cycle as? CycleState.Known
    val ageGroup = state.settings.baseline.ageGroup
    val report = remember(state.logs, cycle, ageGroup) {
        cycle?.let { CycleInsights.healthReport(state.logs, it, ageGroup) }
    }
    val context = LocalContext.current

    val logsLast90Days = remember(state.logs, state.today) {
        state.logs.filter { !it.date.isBefore(state.today.minusDays(90)) }
    }

    var selectedSections by rememberSaveable {
        mutableStateOf(
            setOf(
                ReportSection.OVERVIEW,
                ReportSection.CYCLES_HISTORY,
                ReportSection.SYMPTOMS_AND_MOODS,
                ReportSection.MEDICATIONS,
                ReportSection.WEIGHT,
                ReportSection.OVULATION_SIGNS,
                ReportSection.ALERTS,
            ),
        )
    }

    Page {
        PageTitle(
            tr("Bilan de santé", "Health report"),
            onBack = onBack,
            subtitle = tr("Synthèse pour une consultation médicale", "Summary for a medical consultation"),
        )

        if (cycle == null || report == null) {
            LunaCard {
                Text(
                    tr("Notez vos prochaines règles pour générer votre bilan de santé complet.", "Log your periods to generate your complete health report."),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            return@Page
        }

        // Sélecteur de sections pour l'export médical
        SectionTitle(tr("Sélectionner ce qui est inclus", "Select what is included"))
        LunaCard {
            Text(
                tr("Cochez les rubriques à afficher et exporter selon ce que vous souhaitez partager avec votre soignant :", "Choose the categories to display and export according to what you wish to share with your clinician:"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChoiceChips(
                options = ReportSection.entries,
                label = { it.label },
                isSelected = { it in selectedSections },
                onToggle = { section ->
                    selectedSections = if (section in selectedSections) selectedSections - section else selectedSections + section
                },
            )
        }

        LunaCard(container = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                tr("Situation au ${Format.dayMonth(report.generatedOn)}", "Status as of ${Format.dayMonth(report.generatedOn)}"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Bullet(tr("Début des dernières règles : ${Format.dayMonth(report.lastPeriodStart)} (jour ${report.dayOfCycle} du cycle)", "Last period start: ${Format.dayMonth(report.lastPeriodStart)} (cycle day ${report.dayOfCycle})"))
            if (report.spottingCountLast90Days > 0) {
                Bullet(tr("Saignements hors règles (90 derniers jours) : ${Format.days(report.spottingCountLast90Days)}", "Spotting outside periods (last 90 days): ${Format.days(report.spottingCountLast90Days)}"))
            }
        }

        val overview = report.overview
        if (ReportSection.OVERVIEW in selectedSections && overview != null) {
            SectionTitle(tr("Synthèse des derniers cycles (repères FIGO)", "Recent cycles summary (FIGO references)"))
            LunaCard {
                InsightTexts.overviewLines(overview).forEach { Bullet(it) }
                Bullet(
                    if (overview.withinReferences) {
                        tr("Repères médicaux (FIGO / ACOG) : durées et régularité dans les valeurs habituelles.", "Medical references (FIGO / ACOG): lengths and regularity within usual ranges.")
                    } else {
                        tr("Repères médicaux (FIGO / ACOG) : au moins un cycle sort des repères habituels.", "Medical references (FIGO / ACOG): at least one cycle is outside usual ranges.")
                    },
                )
            }
        }

        if (ReportSection.CYCLES_HISTORY in selectedSections && overview != null) {
            SectionTitle(tr("Historique des cycles complets", "Completed cycles history"))
            LunaCard {
                overview.records.reversed().forEach { rec ->
                    val periodPart = rec.periodLength?.let { tr(", règles de ${Format.days(it)}", ", period ${Format.days(it)}") }.orEmpty()
                    val painPart = if (rec.painDays > 0) tr(", douleur gênante/forte : ${Format.days(rec.painDays)}", ", bothersome/severe pain: ${Format.days(rec.painDays)}") else ""
                    Bullet(tr("Début le ${Format.dayMonth(rec.start)} : cycle de ${Format.days(rec.length)}$periodPart$painPart", "Started ${Format.dayMonth(rec.start)}: ${Format.days(rec.length)} cycle$periodPart$painPart"))
                }
            }
        }

        if (ReportSection.SYMPTOMS_AND_MOODS in selectedSections && report.phasePatterns.isNotEmpty()) {
            SectionTitle(tr("Symptômes et humeurs récurrents par phase", "Recurring symptoms and moods by phase"))
            LunaCard {
                report.phasePatterns.forEach { p ->
                    val items = p.symptoms.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" } +
                        p.moods.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" }
                    Bullet("${Format.phaseName(p.phase)} : ${items.joinToString(", ")}")
                }
            }
        }

        if (ReportSection.MEDICATIONS in selectedSections) {
            val pillTakenCount = logsLast90Days.count { it.pillStatus == PillStatus.TAKEN || it.medicationTaken }
            val pillMissedCount = logsLast90Days.count { it.pillStatus == PillStatus.MISSED }
            val medsCount = logsLast90Days.flatMap { it.medications }.groupingBy { it }.eachCount()
            if (pillTakenCount > 0 || pillMissedCount > 0 || medsCount.isNotEmpty()) {
                SectionTitle(tr("Contraception & traitements notés (90 j)", "Contraception & medications (90 d)"))
                LunaCard {
                    val medLabel = state.settings.reminders.medicationLabel.trim().ifBlank { tr("Pilule / Contraception", "Pill / Contraception") }
                    if (pillTakenCount > 0) Bullet(tr("$medLabel : $pillTakenCount prise(s) notée(s)", "$medLabel: $pillTakenCount dose(s) logged"))
                    if (pillMissedCount > 0) Bullet(tr("$medLabel : $pillMissedCount oubli(s) noté(s)", "$medLabel: $pillMissedCount missed dose(s) logged"))
                    medsCount.forEach { (kind, count) ->
                        Bullet(tr("${kind.label} : noté ${Format.days(count)}", "${kind.label}: logged ${Format.days(count)}"))
                    }
                }
            }
        }

        if (ReportSection.WEIGHT in selectedSections) {
            val weights = logsLast90Days.filter { it.weight != null }.sortedByDescending { it.date }
            if (weights.isNotEmpty()) {
                SectionTitle(tr("Suivi du poids", "Weight tracking"))
                LunaCard {
                    val latest = weights.first()
                    Bullet(tr("Dernier poids noté le ${Format.dayMonth(latest.date)} : ${"%.1f".format(Locale.FRENCH, latest.weight)} kg", "Last weight on ${Format.dayMonth(latest.date)}: ${"%.1f".format(Locale.ENGLISH, latest.weight)} kg"))
                    if (weights.size > 1) {
                        val min = weights.minOf { it.weight!! }
                        val max = weights.maxOf { it.weight!! }
                        Bullet(tr("Plage sur 90 jours : ${"%.1f".format(Locale.FRENCH, min)} à ${"%.1f".format(Locale.FRENCH, max)} kg (${weights.size} pesées)", "90-day range: ${"%.1f".format(Locale.ENGLISH, min)} to ${"%.1f".format(Locale.ENGLISH, max)} kg (${weights.size} entries)"))
                    }
                }
            }
        }

        if (ReportSection.OVULATION_SIGNS in selectedSections) {
            val mucusDays = logsLast90Days.filter { it.mucus != null }
            val tempDays = logsLast90Days.filter { it.basalTemperature != null }
            if (mucusDays.isNotEmpty() || tempDays.isNotEmpty()) {
                SectionTitle(tr("Signes d'ovulation (glaire et température)", "Ovulation signs (mucus & temp)"))
                LunaCard {
                    if (mucusDays.isNotEmpty()) {
                        Bullet(tr("Observations de glaire : ${Format.days(mucusDays.size)}", "Mucus observations: ${Format.days(mucusDays.size)}"))
                    }
                    if (tempDays.isNotEmpty()) {
                        val avg = tempDays.map { it.basalTemperature!! }.average()
                        Bullet(tr("Température basale notée : ${Format.days(tempDays.size)} (moyenne : ${"%.1f".format(Locale.FRENCH, avg)} °C)", "Basal temperature logged: ${Format.days(tempDays.size)} (avg: ${"%.1f".format(Locale.ENGLISH, avg)} °C)"))
                    }
                }
            }
        }

        if (ReportSection.INTIMACY in selectedSections) {
            val protectedCount = logsLast90Days.count { IntimacyKind.PROTECTED in it.intimacy }
            val unprotectedCount = logsLast90Days.count { IntimacyKind.UNPROTECTED in it.intimacy }
            val nonPenetrativeCount = logsLast90Days.count { IntimacyKind.NON_PENETRATIVE in it.intimacy }
            val totalIntimacy = protectedCount + unprotectedCount + nonPenetrativeCount
            if (totalIntimacy > 0) {
                SectionTitle(tr("Vie intime notée (90 derniers jours)", "Logged intimacy (last 90 days)"))
                LunaCard {
                    if (protectedCount > 0) Bullet(tr("Rapports protégés : $protectedCount", "Protected intercourse: $protectedCount"))
                    if (unprotectedCount > 0) Bullet(tr("Rapports non protégés : $unprotectedCount", "Unprotected intercourse: $unprotectedCount"))
                    if (nonPenetrativeCount > 0) Bullet(tr("Sans pénétration ou solo : $nonPenetrativeCount", "Non-penetrative or solo: $nonPenetrativeCount"))
                }
            }
        }

        if (ReportSection.NOTES in selectedSections) {
            val recentNotes = logsLast90Days.filter { it.note.isNotBlank() }.sortedByDescending { it.date }.take(5)
            if (recentNotes.isNotEmpty()) {
                SectionTitle(tr("Notes personnelles récentes", "Recent personal notes"))
                LunaCard {
                    recentNotes.forEach { log ->
                        Bullet("${Format.dayMonth(log.date)} : ${log.note}")
                    }
                }
            }
        }

        if (ReportSection.ALERTS in selectedSections && report.alerts.isNotEmpty()) {
            SectionTitle(tr("Points d'attention relevés", "Points of attention noted"))
            LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                report.alerts.forEach { alert ->
                    Bullet("${alert.title} : ${alert.message}")
                }
            }
        }

        LunaButton(
            tr("Partager ou exporter ce bilan", "Share or export this report"),
            onClick = {
                val text = formatPlainReport(
                    report = report,
                    medicationLabel = state.settings.reminders.medicationLabel,
                    logsLast90Days = logsLast90Days,
                    sections = selectedSections,
                )
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_SUBJECT, tr("Bilan de cycle Lunaison", "Lunaison cycle health report"))
                    .putExtra(Intent.EXTRA_TEXT, text)
                context.startActivity(Intent.createChooser(intent, null))
            },
            icon = Icons.Filled.Share,
        )
        Muted(tr("Ce résumé est généré sur votre téléphone pour faciliter l'échange avec un professionnel de santé. Il ne constitue pas un diagnostic médical.", "This summary is generated on your phone to help discuss with a healthcare professional. It is not a medical diagnosis."))
    }
}

private fun formatPlainReport(
    report: HealthReport,
    medicationLabel: String,
    logsLast90Days: List<DayLog>,
    sections: Set<ReportSection>,
): String = buildString {
    appendLine(tr("BILAN DE CYCLE LUNAISON (${Format.dayMonth(report.generatedOn)})", "LUNAISON CYCLE REPORT (${Format.dayMonth(report.generatedOn)})"))
    appendLine(tr("Début des dernières règles : ${Format.dayMonth(report.lastPeriodStart)} (jour ${report.dayOfCycle})", "Last period start: ${Format.dayMonth(report.lastPeriodStart)} (day ${report.dayOfCycle})"))
    if (report.spottingCountLast90Days > 0) {
        appendLine(tr("Saignements hors règles (90 j) : ${Format.days(report.spottingCountLast90Days)}", "Spotting outside periods (90 d): ${Format.days(report.spottingCountLast90Days)}"))
    }

    if (ReportSection.OVERVIEW in sections) {
        report.overview?.let { ov ->
            appendLine()
            InsightTexts.overviewLines(ov).forEach { appendLine("- $it") }
        }
    }

    if (ReportSection.CYCLES_HISTORY in sections) {
        report.overview?.let { ov ->
            appendLine()
            appendLine(tr("Cycles complets récents :", "Recent completed cycles:"))
            ov.records.reversed().forEach { rec ->
                val p = rec.periodLength?.let { tr(", règles ${it}j", ", period ${it}d") }.orEmpty()
                val d = if (rec.painDays > 0) tr(", douleur gênante/forte ${rec.painDays}j", ", moderate/severe pain ${rec.painDays}d") else ""
                appendLine("- ${Format.dayMonth(rec.start)} : ${rec.length}j$p$d")
            }
        }
    }

    if (ReportSection.SYMPTOMS_AND_MOODS in sections && report.phasePatterns.isNotEmpty()) {
        appendLine()
        appendLine(tr("Motifs récurrents par phase :", "Recurring patterns by phase:"))
        report.phasePatterns.forEach { p ->
            val items = p.symptoms.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" } +
                p.moods.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" }
            appendLine("- ${Format.phaseName(p.phase)} : ${items.joinToString(", ")}")
        }
    }

    if (ReportSection.MEDICATIONS in sections) {
        val pillTakenCount = logsLast90Days.count { it.pillStatus == PillStatus.TAKEN || it.medicationTaken }
        val pillMissedCount = logsLast90Days.count { it.pillStatus == PillStatus.MISSED }
        val medsCount = logsLast90Days.flatMap { it.medications }.groupingBy { it }.eachCount()
        if (pillTakenCount > 0 || pillMissedCount > 0 || medsCount.isNotEmpty()) {
            appendLine()
            appendLine(tr("Contraception & traitements notés (90 j) :", "Contraception & medications (90 d):"))
            val med = medicationLabel.trim().ifBlank { tr("Pilule / Contraception", "Pill / Contraception") }
            if (pillTakenCount > 0) appendLine("- $med : $pillTakenCount prise(s)")
            if (pillMissedCount > 0) appendLine("- $med : $pillMissedCount oubli(s)")
            medsCount.forEach { (kind, count) ->
                appendLine("- ${kind.label} : ${Format.days(count)}")
            }
        }
    }

    if (ReportSection.WEIGHT in sections) {
        val weights = logsLast90Days.filter { it.weight != null }.sortedByDescending { it.date }
        if (weights.isNotEmpty()) {
            appendLine()
            appendLine(tr("Poids (90 j) :", "Weight (90 d):"))
            val latest = weights.first()
            appendLine("- ${Format.dayMonth(latest.date)} : ${"%.1f".format(Locale.FRENCH, latest.weight)} kg")
        }
    }

    if (ReportSection.OVULATION_SIGNS in sections) {
        val mucusDays = logsLast90Days.filter { it.mucus != null }
        val tempDays = logsLast90Days.filter { it.basalTemperature != null }
        if (mucusDays.isNotEmpty() || tempDays.isNotEmpty()) {
            appendLine()
            appendLine(tr("Signes d'ovulation (90 j) :", "Ovulation signs (90 d):"))
            if (mucusDays.isNotEmpty()) appendLine("- Glaire notée : ${mucusDays.size} j")
            if (tempDays.isNotEmpty()) {
                val avg = tempDays.map { it.basalTemperature!! }.average()
                appendLine("- Température moyenne : ${"%.1f".format(Locale.FRENCH, avg)} °C (${tempDays.size} j)")
            }
        }
    }

    if (ReportSection.INTIMACY in sections) {
        val protectedCount = logsLast90Days.count { IntimacyKind.PROTECTED in it.intimacy }
        val unprotectedCount = logsLast90Days.count { IntimacyKind.UNPROTECTED in it.intimacy }
        val totalIntimacy = protectedCount + unprotectedCount
        if (totalIntimacy > 0) {
            appendLine()
            appendLine(tr("Vie intime (90 j) :", "Intimacy (90 d):"))
            if (protectedCount > 0) appendLine("- Rapports protégés : $protectedCount")
            if (unprotectedCount > 0) appendLine("- Rapports non protégés : $unprotectedCount")
        }
    }

    if (ReportSection.NOTES in sections) {
        val recentNotes = logsLast90Days.filter { it.note.isNotBlank() }.sortedByDescending { it.date }.take(5)
        if (recentNotes.isNotEmpty()) {
            appendLine()
            appendLine(tr("Notes personnelles récentes :", "Recent personal notes:"))
            recentNotes.forEach { appendLine("- ${Format.dayMonth(it.date)} : ${it.note}") }
        }
    }

    if (ReportSection.ALERTS in sections && report.alerts.isNotEmpty()) {
        appendLine()
        appendLine(tr("Points d'attention :", "Points of attention:"))
        report.alerts.forEach { appendLine("- ${it.title}") }
    }
}.trimEnd()
