package fr.lunaison.ui

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.HealthReport
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

/**
 * Bilan de santé complet à montrer ou partager avec un médecin, un gynécologue ou une sage-femme.
 * Regroupe les durées des derniers cycles, les jours de douleur, les symptômes récurrents par phase et les repères FIGO/ACOG.
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
            if (report.medicationDaysLast30Days > 0) {
                val medLabel = state.settings.reminders.medicationLabel.trim().ifBlank { tr("contraception ou traitement", "contraception or medication") }
                Bullet(tr("Prises notées sur 30 jours ($medLabel) : ${Format.days(report.medicationDaysLast30Days)}", "Logged doses over 30 days ($medLabel): ${Format.days(report.medicationDaysLast30Days)}"))
            }
        }

        val overview = report.overview
        if (overview != null) {
            SectionTitle(tr("Synthèse des derniers cycles", "Recent cycles summary"))
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

            SectionTitle(tr("Historique des cycles complets", "Completed cycles history"))
            LunaCard {
                overview.records.reversed().forEach { rec ->
                    val periodPart = rec.periodLength?.let { tr(", règles de ${Format.days(it)}", ", period ${Format.days(it)}") }.orEmpty()
                    val painPart = if (rec.painDays > 0) tr(", douleur gênante/forte : ${Format.days(rec.painDays)}", ", bothersome/severe pain: ${Format.days(rec.painDays)}") else ""
                    Bullet(tr("Début le ${Format.dayMonth(rec.start)} : cycle de ${Format.days(rec.length)}$periodPart$painPart", "Started ${Format.dayMonth(rec.start)}: ${Format.days(rec.length)} cycle$periodPart$painPart"))
                }
            }
        }

        if (report.phasePatterns.isNotEmpty()) {
            SectionTitle(tr("Symptômes et humeurs récurrents par phase", "Recurring symptoms and moods by phase"))
            LunaCard {
                report.phasePatterns.forEach { p ->
                    val items = p.symptoms.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" } +
                        p.moods.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" }
                    Bullet("${Format.phaseName(p.phase)} : ${items.joinToString(", ")}")
                }
            }
        }

        if (report.alerts.isNotEmpty()) {
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
                val text = formatPlainReport(report, state.settings.reminders.medicationLabel)
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

private fun formatPlainReport(report: HealthReport, medicationLabel: String): String = buildString {
    appendLine(tr("BILAN DE CYCLE LUNAISON (${Format.dayMonth(report.generatedOn)})", "LUNAISON CYCLE REPORT (${Format.dayMonth(report.generatedOn)})"))
    appendLine(tr("Début des dernières règles : ${Format.dayMonth(report.lastPeriodStart)} (jour ${report.dayOfCycle})", "Last period start: ${Format.dayMonth(report.lastPeriodStart)} (day ${report.dayOfCycle})"))
    if (report.spottingCountLast90Days > 0) {
        appendLine(tr("Saignements hors règles (90 j) : ${Format.days(report.spottingCountLast90Days)}", "Spotting outside periods (90 d): ${Format.days(report.spottingCountLast90Days)}"))
    }
    if (report.medicationDaysLast30Days > 0) {
        val med = medicationLabel.trim().ifBlank { tr("contraception/traitement", "contraception/medication") }
        appendLine(tr("Prises notées sur 30 j ($med) : ${Format.days(report.medicationDaysLast30Days)}", "Logged doses over 30 d ($med): ${Format.days(report.medicationDaysLast30Days)}"))
    }
    report.overview?.let { ov ->
        appendLine()
        InsightTexts.overviewLines(ov).forEach { appendLine("- $it") }
        appendLine()
        appendLine(tr("Cycles complets récents :", "Recent completed cycles:"))
        ov.records.reversed().forEach { rec ->
            val p = rec.periodLength?.let { tr(", règles ${it}j", ", period ${it}d") }.orEmpty()
            val d = if (rec.painDays > 0) tr(", douleur gênante/forte ${rec.painDays}j", ", moderate/severe pain ${rec.painDays}d") else ""
            appendLine("- ${Format.dayMonth(rec.start)} : ${rec.length}j$p$d")
        }
    }
    if (report.phasePatterns.isNotEmpty()) {
        appendLine()
        appendLine(tr("Motifs récurrents par phase :", "Recurring patterns by phase:"))
        report.phasePatterns.forEach { p ->
            val items = p.symptoms.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" } +
                p.moods.map { "${it.first.label.lowercase()} (${it.second}/${p.cycles})" }
            appendLine("- ${Format.phaseName(p.phase)} : ${items.joinToString(", ")}")
        }
    }
    if (report.alerts.isNotEmpty()) {
        appendLine()
        appendLine(tr("Points d'attention :", "Points of attention:"))
        report.alerts.forEach { appendLine("- ${it.title}") }
    }
}.trimEnd()
