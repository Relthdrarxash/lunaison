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
            tr(fr = "Bilan de santé", en = "Health report", pl = "Raport medyczny", uk = "Медичний звіт", es = "Informe médico", de = "Arztbericht"),
            onBack = onBack,
            subtitle = ReportText.subtitle(),
        )

        if (cycle == null || report == null) {
            LunaCard {
                Text(
                    tr(
                        fr = "Notez vos prochaines règles pour générer votre bilan de santé complet.",
                        en = "Log your periods to generate your complete health report.",
                        pl = "Zapisz kolejne miesiączki, aby wygenerować pełny raport medyczny.",
                        uk = "Позначте наступні менструації, щоб створити повний медичний звіт.",
                        es = "Registra tus próximas menstruaciones para generar tu informe médico completo.",
                        de = "Trage deine nächsten Perioden ein, um deinen vollständigen Arztbericht zu erstellen.",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            return@Page
        }

        // Sélecteur de sections pour l'export médical
        SectionTitle(
            tr(
                fr = "Sélectionner ce qui est inclus",
                en = "Select what is included",
                pl = "Wybierz, co ma zostać uwzględnione",
                uk = "Оберіть, що включити",
                es = "Selecciona qué se incluye",
                de = "Auswählen, was enthalten ist",
            ),
        )
        LunaCard {
            Text(
                tr(
                    fr = "Cochez les rubriques à afficher et exporter selon ce que vous souhaitez partager avec votre soignant :",
                    en = "Choose the categories to display and export according to what you wish to share with your clinician:",
                    pl = "Zaznacz rubryki do wyświetlenia i wyeksportowania, zależnie od tego, czym chcesz podzielić się z personelem medycznym:",
                    uk = "Позначте розділи для показу та експорту залежно від того, чим ви хочете поділитися з медичним працівником:",
                    es = "Marca las secciones que quieres mostrar y exportar según lo que desees compartir con tu profesional sanitario:",
                    de = "Wähle die Rubriken aus, die angezeigt und exportiert werden sollen, je nachdem, was du mit deiner behandelnden Person teilen möchtest:",
                ),
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
                tr(
                    fr = "Situation au ${Format.dayMonth(report.generatedOn)}",
                    en = "Status as of ${Format.dayMonth(report.generatedOn)}",
                    pl = "Stan na dzień ${Format.dayMonth(report.generatedOn)}",
                    uk = "Стан на ${Format.dayMonth(report.generatedOn)}",
                    es = "Situación al ${Format.dayMonth(report.generatedOn)}",
                    de = "Stand: ${Format.dayMonth(report.generatedOn)}",
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Bullet(
                tr(
                    fr = "Début des dernières règles : ${Format.dayMonth(report.lastPeriodStart)} (jour ${report.dayOfCycle} du cycle)",
                    en = "Last period start: ${Format.dayMonth(report.lastPeriodStart)} (cycle day ${report.dayOfCycle})",
                    pl = "Początek ostatniej miesiączki: ${Format.dayMonth(report.lastPeriodStart)} (dzień cyklu: ${report.dayOfCycle})",
                    uk = "Початок останньої менструації: ${Format.dayMonth(report.lastPeriodStart)} (день циклу: ${report.dayOfCycle})",
                    es = "Inicio de la última regla: ${Format.dayMonth(report.lastPeriodStart)} (día ${report.dayOfCycle} del ciclo)",
                    de = "Beginn der letzten Periode: ${Format.dayMonth(report.lastPeriodStart)} (Zyklustag ${report.dayOfCycle})",
                ),
            )
            if (report.spottingCountLast90Days > 0) {
                Bullet(
                    ReportText.labelValue(
                        "${ReportText.spotting()} (${ReportText.last90Days()})",
                        Format.days(report.spottingCountLast90Days),
                    ),
                )
            }
        }

        val overview = report.overview
        if (ReportSection.OVERVIEW in selectedSections && overview != null) {
            SectionTitle(ReportText.overviewTitle())
            LunaCard {
                InsightTexts.overviewLines(overview).forEach { Bullet(it) }
                Bullet(
                    if (overview.withinReferences) ReportText.referencesWithin() else ReportText.referencesOutside(),
                )
            }
        }

        if (ReportSection.CYCLES_HISTORY in selectedSections && overview != null) {
            SectionTitle(ReportText.cyclesHistoryTitle())
            LunaCard {
                overview.records.reversed().forEach { rec ->
                    val periodPart = rec.periodLength?.let {
                        tr(
                            fr = ", règles de ${Format.days(it)}",
                            en = ", period ${Format.days(it)}",
                            pl = "; czas trwania miesiączki: ${Format.days(it)}",
                            uk = "; тривалість менструації: ${Format.days(it)}",
                            es = "; duración de la regla: ${Format.days(it)}",
                            de = "; Blutungsdauer: ${Format.days(it)}",
                        )
                    }.orEmpty()
                    val painPart = if (rec.painDays > 0) {
                        tr(
                            fr = ", douleur gênante/forte : ${Format.days(rec.painDays)}",
                            en = ", bothersome/severe pain: ${Format.days(rec.painDays)}",
                            pl = "; ból dokuczliwy lub silny: ${Format.days(rec.painDays)}",
                            uk = "; помірний або сильний біль: ${Format.days(rec.painDays)}",
                            es = "; dolor molesto o fuerte: ${Format.days(rec.painDays)}",
                            de = "; mäßige oder starke Schmerzen: ${Format.days(rec.painDays)}",
                        )
                    } else {
                        ""
                    }
                    Bullet(
                        tr(
                            fr = "Début le ${Format.dayMonth(rec.start)} : cycle de ${Format.days(rec.length)}$periodPart$painPart",
                            en = "Started ${Format.dayMonth(rec.start)}: ${Format.days(rec.length)} cycle$periodPart$painPart",
                            pl = "Początek ${Format.dayMonth(rec.start)}: długość cyklu ${Format.days(rec.length)}$periodPart$painPart",
                            uk = "Початок ${Format.dayMonth(rec.start)}: тривалість циклу ${Format.days(rec.length)}$periodPart$painPart",
                            es = "Inicio el ${Format.dayMonth(rec.start)}: ciclo de ${Format.days(rec.length)}$periodPart$painPart",
                            de = "Beginn ${Format.dayMonth(rec.start)}: Zykluslänge ${Format.days(rec.length)}$periodPart$painPart",
                        ),
                    )
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
                SectionTitle(
                    tr(
                        fr = "Repères douleurs & endométriose (90 derniers jours)",
                        en = "Pain & endometriosis indicators (last 90 days)",
                        pl = "Wskaźniki bólu i endometriozy (ostatnie 90 dni)",
                        uk = "Ознаки болю та ендометріозу (останні 90 днів)",
                        es = "Indicadores de dolor y endometriosis (últimos 90 días)",
                        de = "Schmerzen und Endometriose-Anhaltspunkte (letzte 90 Tage)",
                    ),
                )
                LunaCard {
                    if (severePainDays > 0 || moderatePainDays > 0) {
                        val totalPainDays = severePainDays + moderatePainDays
                        Bullet(
                            tr(
                                fr = "Douleur gênante ou forte : ${Format.days(totalPainDays)} (dont ${Format.days(severePainDays)} sévère(s))",
                                en = "Bothersome or severe pain: ${Format.days(totalPainDays)} (including ${Format.days(severePainDays)} severe)",
                                pl = "Ból dokuczliwy lub silny: ${Format.days(totalPainDays)} (w tym silny: ${Format.days(severePainDays)})",
                                uk = "Помірний або сильний біль: ${Format.days(totalPainDays)} (з них сильний: ${Format.days(severePainDays)})",
                                es = "Dolor molesto o fuerte: ${Format.days(totalPainDays)} (de los cuales, fuerte: ${Format.days(severePainDays)})",
                                de = "Mäßige oder starke Schmerzen: ${Format.days(totalPainDays)} (davon stark: ${Format.days(severePainDays)})",
                            ),
                        )
                    }
                    if (crampsDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painCramps(), ReportText.loggedDays(crampsDays)))
                    }
                    if (pelvicPainDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painPelvic(), ReportText.loggedDays(pelvicPainDays)))
                    }
                    if (dyspareuniaDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painDyspareunia(), ReportText.loggedDays(dyspareuniaDays)))
                    }
                    if (digestivePainDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painDigestive(), ReportText.loggedDays(digestivePainDays)))
                    }
                    if (urinaryPainDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painUrinary(), ReportText.loggedDays(urinaryPainDays)))
                    }
                    if (radiatingPainDays > 0) {
                        Bullet(ReportText.labelValue(ReportText.painRadiating(), ReportText.loggedDays(radiatingPainDays)))
                    }
                    Text(
                        ReportText.painGuidelines(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (ReportSection.SYMPTOMS_AND_MOODS in selectedSections && report.phasePatterns.isNotEmpty()) {
            SectionTitle(ReportText.symptomsTitle())
            LunaCard {
                report.phasePatterns.forEach { p ->
                    val items = p.symptoms.map { "${ReportText.inline(it.first.label)} (${it.second}/${p.cycles})" } +
                        p.moods.map { "${ReportText.inline(it.first.label)} (${it.second}/${p.cycles})" }
                    Bullet(ReportText.labelValue(Format.phaseName(p.phase), items.joinToString(", ")))
                }
            }
        }

        if (ReportSection.MEDICATIONS in selectedSections) {
            val pillTakenCount = logsLast90Days.count { it.pillStatus == PillStatus.TAKEN || it.medicationTaken }
            val pillMissedCount = logsLast90Days.count { it.pillStatus == PillStatus.MISSED }
            val medsCount = logsLast90Days.flatMap { it.medications }.groupingBy { it }.eachCount()
            if (pillTakenCount > 0 || pillMissedCount > 0 || medsCount.isNotEmpty()) {
                SectionTitle(
                    tr(
                        fr = "Contraception & traitements notés (90 j)",
                        en = "Contraception & medications (90 d)",
                        pl = "Antykoncepcja i leki, zapisy (90 dni)",
                        uk = "Контрацепція та ліки, записи (90 днів)",
                        es = "Anticoncepción y tratamientos registrados (90 días)",
                        de = "Verhütung und erfasste Medikamente (90 Tage)",
                    ),
                )
                LunaCard {
                    val medLabel = state.settings.reminders.medicationLabel.trim().ifBlank { ReportText.defaultMedicationLabel() }
                    if (pillTakenCount > 0) Bullet(ReportText.labelValue(medLabel, ReportText.pillTaken(pillTakenCount)))
                    if (pillMissedCount > 0) Bullet(ReportText.labelValue(medLabel, ReportText.pillMissed(pillMissedCount)))
                    medsCount.forEach { (kind, count) ->
                        Bullet(ReportText.labelValue(kind.label, ReportText.loggedDays(count)))
                    }
                }
            }
        }

        if (ReportSection.WEIGHT in selectedSections) {
            val weights = logsLast90Days.filter { it.weight != null }.sortedByDescending { it.date }
            if (weights.isNotEmpty()) {
                SectionTitle(
                    tr(
                        fr = "Suivi du poids",
                        en = "Weight tracking",
                        pl = "Kontrola wagi",
                        uk = "Контроль ваги",
                        es = "Seguimiento del peso",
                        de = "Gewichtsverlauf",
                    ),
                )
                LunaCard {
                    val latest = weights.first()
                    val latestKg = "${ReportText.decimal(latest.weight!!.toDouble())} ${ReportText.kg()}"
                    Bullet(
                        tr(
                            fr = "Dernier poids noté le ${Format.dayMonth(latest.date)} : $latestKg",
                            en = "Last weight on ${Format.dayMonth(latest.date)}: $latestKg",
                            pl = "Ostatnia zapisana waga (${Format.dayMonth(latest.date)}): $latestKg",
                            uk = "Остання записана вага (${Format.dayMonth(latest.date)}): $latestKg",
                            es = "Último peso registrado el ${Format.dayMonth(latest.date)}: $latestKg",
                            de = "Zuletzt erfasstes Gewicht am ${Format.dayMonth(latest.date)}: $latestKg",
                        ),
                    )
                    if (weights.size > 1) {
                        val min = weights.minOf { it.weight!! }
                        val max = weights.maxOf { it.weight!! }
                        Bullet(ReportText.labelValue(ReportText.weightRange(), ReportText.weightRangeValue(min.toDouble(), max.toDouble(), weights.size)))
                    }
                }
            }
        }

        if (ReportSection.OVULATION_SIGNS in selectedSections) {
            val mucusDays = logsLast90Days.filter { it.mucus != null }
            val tempDays = logsLast90Days.filter { it.basalTemperature != null }
            if (mucusDays.isNotEmpty() || tempDays.isNotEmpty()) {
                SectionTitle(
                    tr(
                        fr = "Signes d'ovulation (glaire et température)",
                        en = "Ovulation signs (mucus & temp)",
                        pl = "Objawy owulacji (śluz szyjkowy i temperatura)",
                        uk = "Ознаки овуляції (цервікальний слиз і температура)",
                        es = "Signos de ovulación (moco cervical y temperatura)",
                        de = "Ovulationszeichen (Zervixschleim und Temperatur)",
                    ),
                )
                LunaCard {
                    if (mucusDays.isNotEmpty()) {
                        Bullet(ReportText.labelValue(ReportText.mucusObservations(), Format.days(mucusDays.size)))
                    }
                    if (tempDays.isNotEmpty()) {
                        val avg = tempDays.map { it.basalTemperature!! }.average()
                        Bullet(ReportText.labelValue(ReportText.basalTemperature(), ReportText.basalTemperatureValue(tempDays.size, avg)))
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
                SectionTitle(
                    tr(
                        fr = "Vie intime notée (90 derniers jours)",
                        en = "Logged intimacy (last 90 days)",
                        pl = "Życie intymne, zapisy (ostatnie 90 dni)",
                        uk = "Інтимне життя, записи (останні 90 днів)",
                        es = "Vida íntima registrada (últimos 90 días)",
                        de = "Erfasste Intimität (letzte 90 Tage)",
                    ),
                )
                LunaCard {
                    if (protectedCount > 0) Bullet(ReportText.labelValue(ReportText.intimacyProtected(), protectedCount.toString()))
                    if (unprotectedCount > 0) Bullet(ReportText.labelValue(ReportText.intimacyUnprotected(), unprotectedCount.toString()))
                    if (nonPenetrativeCount > 0) Bullet(ReportText.labelValue(ReportText.intimacyNonPenetrative(), nonPenetrativeCount.toString()))
                }
            }
        }

        if (ReportSection.NOTES in selectedSections) {
            val recentNotes = logsLast90Days.filter { it.note.isNotBlank() }.sortedByDescending { it.date }.take(5)
            if (recentNotes.isNotEmpty()) {
                SectionTitle(
                    tr(
                        fr = "Notes personnelles récentes",
                        en = "Recent personal notes",
                        pl = "Ostatnie notatki osobiste",
                        uk = "Останні особисті нотатки",
                        es = "Notas personales recientes",
                        de = "Aktuelle persönliche Notizen",
                    ),
                )
                LunaCard {
                    recentNotes.forEach { log ->
                        Bullet("${Format.dayMonth(log.date)} : ${log.note}")
                    }
                }
            }
        }

        if (ReportSection.ALERTS in selectedSections && report.alerts.isNotEmpty()) {
            SectionTitle(ReportText.alertsTitle())
            LunaCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                report.alerts.forEach { alert ->
                    Bullet("${alert.title} : ${alert.message}")
                }
            }
        }

        LunaButton(
            if (exporting) {
                tr(
                    fr = "Création du PDF...",
                    en = "Creating the PDF...",
                    pl = "Tworzenie pliku PDF...",
                    uk = "Створення PDF...",
                    es = "Creando el PDF...",
                    de = "PDF wird erstellt...",
                )
            } else {
                tr(fr = "Exporter en PDF", en = "Export as PDF", pl = "Eksportuj do PDF", uk = "Експортувати в PDF", es = "Exportar a PDF", de = "Als PDF exportieren")
            },
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
                            val message = tr(
                                fr = "Impossible de créer le PDF.",
                                en = "Could not create the PDF.",
                                pl = "Nie udało się utworzyć pliku PDF.",
                                uk = "Не вдалося створити PDF.",
                                es = "No se pudo crear el PDF.",
                                de = "Das PDF konnte nicht erstellt werden.",
                            )
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        } finally {
                            exporting = false
                        }
                    }
                }
            },
            icon = Icons.Filled.Share,
            enabled = !exporting,
        )
        Muted(
            tr(
                fr = "Ce résumé est généré sur votre téléphone pour faciliter l'échange avec un professionnel de santé. Il ne constitue pas un diagnostic médical.",
                en = "This summary is generated on your phone to help discuss with a healthcare professional. It is not a medical diagnosis.",
                pl = "To podsumowanie jest tworzone na Twoim telefonie, aby ułatwić rozmowę z pracownikiem ochrony zdrowia. Nie stanowi diagnozy medycznej.",
                uk = "Цей підсумок створюється на вашому телефоні, щоб полегшити розмову з медичним працівником. Він не є медичним діагнозом.",
                es = "Este resumen se genera en tu teléfono para facilitar la conversación con un profesional sanitario. No constituye un diagnóstico médico.",
                de = "Diese Zusammenfassung wird auf deinem Smartphone erstellt und erleichtert das Gespräch mit medizinischem Fachpersonal. Sie ist keine medizinische Diagnose.",
            ),
        )
    }
}

/** Ouvre le sélecteur de partage pour le PDF, lu par les autres applis via le FileProvider de Lunaison. */
private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fichiers", file)
    val subject = tr(
        fr = "Bilan de cycle Lunaison",
        en = "Lunaison cycle health report",
        pl = "Raport o cyklu z Lunaison",
        uk = "Звіт про цикл із Lunaison",
        es = "Informe del ciclo de Lunaison",
        de = "Zyklusbericht von Lunaison",
    )
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    send.clipData = ClipData.newRawUri(file.name, uri)
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    } catch (_: ActivityNotFoundException) {
        val message = tr(
            fr = "Aucune appli pour partager ce PDF.",
            en = "No app available to share this PDF.",
            pl = "Brak aplikacji do udostępnienia tego pliku PDF.",
            uk = "Немає застосунку, щоб поділитися цим PDF.",
            es = "No hay ninguna aplicación para compartir este PDF.",
            de = "Keine App zum Teilen dieses PDFs verfügbar.",
        )
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
