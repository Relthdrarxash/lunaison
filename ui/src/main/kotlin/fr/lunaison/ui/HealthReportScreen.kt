package fr.lunaison.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import fr.lunaison.core.model.tr
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    PAIN_AND_ENDOMETRIOSIS("Douleurs & endométriose", "Pain & endometriosis", "Ból i endometrioza", "Біль та ендометріоз", "Dolor y endometriosis", "Schmerzen & Endometriose"),
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
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }

    val logsLast90Days = remember(state.logs, state.today) {
        state.logs.filter { !it.date.isBefore(state.today.minusDays(90)) }
    }

    var selectedSections by rememberSaveable {
        mutableStateOf(
            setOf(
                ReportSection.OVERVIEW,
                ReportSection.CYCLES_HISTORY,
                ReportSection.PAIN_AND_ENDOMETRIOSIS,
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

        if (ReportSection.PAIN_AND_ENDOMETRIOSIS in selectedSections) {
            val severePainDays = logsLast90Days.count { it.pain == Pain.SEVERE }
            val moderatePainDays = logsLast90Days.count { it.pain == Pain.MODERATE }
            val pelvicPainDays = logsLast90Days.count { Symptom.PELVIC_PAIN in it.symptoms }
            val dyspareuniaDays = logsLast90Days.count { Symptom.DYSPAREUNIA in it.symptoms }
            val digestivePainDays = logsLast90Days.count { Symptom.DIGESTIVE_PAIN in it.symptoms }
            val urinaryPainDays = logsLast90Days.count { Symptom.URINARY_PAIN in it.symptoms }
            val radiatingPainDays = logsLast90Days.count { Symptom.RADIATING_PAIN in it.symptoms }
            val crampsDays = logsLast90Days.count { Symptom.CRAMPS in it.symptoms }
            val hasPainData = severePainDays > 0 || moderatePainDays > 0 || pelvicPainDays > 0 ||
                dyspareuniaDays > 0 || digestivePainDays > 0 || urinaryPainDays > 0 ||
                radiatingPainDays > 0 || crampsDays > 0

            if (hasPainData) {
                SectionTitle(tr("Repères douleurs & endométriose (90 derniers jours)", "Pain & endometriosis indicators (last 90 days)"))
                LunaCard {
                    if (severePainDays > 0 || moderatePainDays > 0) {
                        val totalPainDays = severePainDays + moderatePainDays
                        Bullet(tr("Douleur gênante ou forte : ${Format.days(totalPainDays)} (dont ${Format.days(severePainDays)} sévère(s))", "Bothersome or severe pain: ${Format.days(totalPainDays)} (including ${Format.days(severePainDays)} severe)"))
                    }
                    if (crampsDays > 0) {
                        Bullet(tr("Crampes abdominales : notées ${Format.days(crampsDays)}", "Abdominal cramps: logged ${Format.days(crampsDays)}"))
                    }
                    if (pelvicPainDays > 0) {
                        Bullet(tr("Douleurs pelviennes (bas-ventre) : notées ${Format.days(pelvicPainDays)}", "Pelvic pain: logged ${Format.days(pelvicPainDays)}"))
                    }
                    if (dyspareuniaDays > 0) {
                        Bullet(tr("Douleurs aux rapports (dyspareunie) : notées ${Format.days(dyspareuniaDays)}", "Pain during sex (dyspareunia): logged ${Format.days(dyspareuniaDays)}"))
                    }
                    if (digestivePainDays > 0) {
                        Bullet(tr("Douleurs au transit ou à la défécation : notées ${Format.days(digestivePainDays)}", "Pain with bowel movements: logged ${Format.days(digestivePainDays)}"))
                    }
                    if (urinaryPainDays > 0) {
                        Bullet(tr("Douleurs en urinant (dysurie) : notées ${Format.days(urinaryPainDays)}", "Pain when urinating: logged ${Format.days(urinaryPainDays)}"))
                    }
                    if (radiatingPainDays > 0) {
                        Bullet(tr("Douleurs irradiantes (dos, lombaires, jambes) : notées ${Format.days(radiatingPainDays)}", "Radiating pain (back, legs): logged ${Format.days(radiatingPainDays)}"))
                    }
                    Text(
                        tr("Repères utiles pour le dialogue soignant selon les recommandations HAS / ESHRE.", "Helpful indicators for clinician dialogue per HAS / ESHRE guidelines."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
            if (exporting) tr("Création du PDF...", "Creating the PDF...") else tr("Exporter en PDF", "Export as PDF"),
            onClick = {
                if (!exporting) {
                    exporting = true
                    val sections = selectedSections
                    val medicationLabel = state.settings.reminders.medicationLabel
                    scope.launch {
                        try {
                            val file = withContext(Dispatchers.IO) {
                                val document = buildReportDocument(report, medicationLabel, logsLast90Days, sections)
                                HealthReportPdf.export(context, document)
                            }
                            sharePdf(context, file)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            Toast.makeText(context, tr("Impossible de créer le PDF.", "Could not create the PDF."), Toast.LENGTH_LONG).show()
                        } finally {
                            exporting = false
                        }
                    }
                }
            },
            icon = Icons.Filled.Share,
            enabled = !exporting,
        )
        Muted(tr("Ce résumé est généré sur votre téléphone pour faciliter l'échange avec un professionnel de santé. Il ne constitue pas un diagnostic médical.", "This summary is generated on your phone to help discuss with a healthcare professional. It is not a medical diagnosis."))
    }
}

/** Ouvre le sélecteur de partage pour le PDF, lu par les autres applis via le FileProvider de Lunaison. */
private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fichiers", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_SUBJECT, tr("Bilan de cycle Lunaison", "Lunaison cycle health report"))
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    send.clipData = ClipData.newRawUri(file.name, uri)
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, tr("Aucune appli pour partager ce PDF.", "No app available to share this PDF."), Toast.LENGTH_LONG).show()
    }
}
