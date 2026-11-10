package fr.lunaison.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.InsightTexts
import fr.lunaison.core.cycle.CycleInsights
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.cycle.PhasePatterns
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

/** « Mes cycles » : ce que les données notées disent, calculé sur le téléphone. */
@Composable
fun InsightsScreen(
    state: AppState,
    onBack: () -> Unit,
    onArticle: (String) -> Unit,
    onHealthReport: () -> Unit = {},
) {
    val cycle = state.cycle as? CycleState.Known
    val ageGroup = state.settings.baseline.ageGroup
    val overview = remember(state.logs, cycle) { cycle?.let { CycleInsights.overview(state.logs, it, ageGroup) } }
    val onsets = remember(state.logs, cycle) { cycle?.let { CycleInsights.onsets(state.logs, it) }.orEmpty() }
    val duringPeriod = remember(state.logs, cycle) { cycle?.let { PhasePatterns.compute(state.logs, it, Phase.MENSTRUAL) } }
    val phasePatterns = remember(state.logs, cycle) { cycle?.let { CycleInsights.allPhasePatterns(state.logs, it) }.orEmpty() }
    val context = LocalContext.current

    Page {
        PageTitle(tr("Mes cycles", "My cycles", pl = "Moje cykle", uk = "Мої цикли", es = "Mis ciclos", de = "Meine Zyklen"), onBack = onBack)

        if (cycle == null || overview == null) {
            LunaCard { Text(InsightTexts.NOTHING_YET, style = MaterialTheme.typography.bodyMedium) }
            Muted(InsightTexts.HOW_IT_WORKS)
            return@Page
        }

        LunaButton(
            tr("Bilan de santé pour un soignant", "Health report for a clinician", pl = "Raport medyczny dla personelu medycznego", uk = "Медичний звіт для медичного працівника", es = "Informe médico para el profesional sanitario", de = "Arztbericht für medizinisches Fachpersonal"),
            onClick = onHealthReport,
            style = LunaButtonStyle.Outlined,
        )

        SectionTitle(tr("Durée de vos cycles", "Length of your cycles", pl = "Długość Twoich cykli", uk = "Тривалість ваших циклів", es = "Duración de tus ciclos", de = "Länge deiner Zyklen"))
        LunaCard {
            InsightTexts.overviewLines(overview).forEach { Bullet(it) }
            CycleBars(overview)
            if (!overview.withinReferences) {
                TextButton(onClick = { onArticle("consulter") }) { Text(tr("Quand consulter", "When to see a doctor", pl = "Kiedy udać się do lekarza", uk = "Коли звертатися до лікаря", es = "Cuándo consultar", de = "Wann zum Arzt?")) }
            }
        }

        SectionTitle(tr("Vos prévisions", "Your forecasts", pl = "Twoje prognozy", uk = "Ваші прогнози", es = "Tus previsiones", de = "Deine Vorhersagen"))
        LunaCard {
            Text(InsightTexts.predictionLine(overview), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { onArticle("previsions") }) { Text(tr("Ce que les prévisions peuvent dire", "What predictions can tell you", pl = "Co mówią przewidywania", uk = "Що можуть підказати прогнози", es = "Qué dicen las previsiones", de = "Was Prognosen leisten können")) }
        }

        if (onsets.isNotEmpty()) {
            SectionTitle(tr("Avant vos règles", "Before your period", pl = "Przed miesiączką", uk = "Перед менструацією", es = "Antes de la regla", de = "Vor der Periode"))
            LunaCard { onsets.forEach { Bullet(InsightTexts.onsetLine(it)) } }
        }

        if (duringPeriod != null && !duringPeriod.isEmpty) {
            SectionTitle(tr("Pendant vos règles", "During your period", pl = "Podczas miesiączki", uk = "Під час менструації", es = "Durante la regla", de = "Während der Periode"))
            LunaCard {
                Text(tr("Lors de vos dernières règles, vous avez souvent noté :", "During your recent periods, you often logged:", pl = "Podczas ostatnich miesiączek często zapisywano:", uk = "Під час ваших останніх менструацій ви часто записували:", es = "En tus últimas reglas has anotado con frecuencia:", de = "Bei deinen letzten Perioden hast du häufig eingetragen:"), style = MaterialTheme.typography.bodyMedium)
                duringPeriod.symptoms.forEach { (s, n) -> Bullet(tr("${s.label} (${n} cycles sur ${duringPeriod.cycles})", "${s.label} (${n} of ${duringPeriod.cycles} cycles)", pl = "${s.label} (cykle: ${n} z ${duringPeriod.cycles})", uk = "${s.label} (цикли: ${n} з ${duringPeriod.cycles})", es = "${s.label} (${n} de ${duringPeriod.cycles} ciclos)", de = "${s.label} (${n} von ${duringPeriod.cycles} Zyklen)")) }
                duringPeriod.moods.forEach { (m, n) -> Bullet(tr("${m.label} (${n} cycles sur ${duringPeriod.cycles})", "${m.label} (${n} of ${duringPeriod.cycles} cycles)", pl = "${m.label} (cykle: ${n} z ${duringPeriod.cycles})", uk = "${m.label} (цикли: ${n} з ${duringPeriod.cycles})", es = "${m.label} (${n} de ${duringPeriod.cycles} ciclos)", de = "${m.label} (${n} von ${duringPeriod.cycles} Zyklen)")) }
            }
        }

        if (phasePatterns.isNotEmpty()) {
            SectionTitle(tr("Votre cycle, phase par phase", "Your cycle, phase by phase", pl = "Twój cykl, faza po fazie", uk = "Ваш цикл, фаза за фазою", es = "Tu ciclo, fase por fase", de = "Dein Zyklus, Phase für Phase"))
            phasePatterns.forEach { pattern ->
                LunaCard {
                    Text(Format.phaseName(pattern.phase), style = MaterialTheme.typography.titleMedium)
                    Text(
                        tr("Revenu lors de vos derniers cycles pendant cette phase :", "Observed across your recent cycles during this phase:", pl = "Pojawiało się w ostatnich cyklach w tej fazie:", uk = "Траплялося в останніх циклах під час цієї фази:", es = "Se ha repetido en tus últimos ciclos durante esta fase:", de = "In deinen letzten Zyklen in dieser Phase aufgetreten:"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    pattern.symptoms.forEach { (s, n) ->
                        Bullet(tr("${s.label} ($n cycles sur ${pattern.cycles})", "${s.label} ($n of ${pattern.cycles} cycles)", pl = "${s.label} (cykle: $n z ${pattern.cycles})", uk = "${s.label} (цикли: $n з ${pattern.cycles})", es = "${s.label} ($n de ${pattern.cycles} ciclos)", de = "${s.label} ($n von ${pattern.cycles} Zyklen)"))
                    }
                    pattern.moods.forEach { (m, n) ->
                        Bullet(tr("Humeur : ${m.label.lowercase()} ($n cycles sur ${pattern.cycles})", "Mood: ${m.label.lowercase()} ($n of ${pattern.cycles} cycles)", pl = "Nastrój: ${m.label.lowercase()} (cykle: $n z ${pattern.cycles})", uk = "Настрій: ${m.label.lowercase()} (цикли: $n з ${pattern.cycles})", es = "Estado de ánimo: ${m.label.lowercase()} ($n de ${pattern.cycles} ciclos)", de = "Stimmung: ${m.label.lowercase()} ($n von ${pattern.cycles} Zyklen)"))
                    }
                }
            }
        }

        val painful = overview.records.filter { it.periodLength != null }
        if (painful.any { it.painDays > 0 }) {
            SectionTitle(tr("Douleurs pendant les règles", "Pain during your period", pl = "Ból podczas miesiączki", uk = "Біль під час менструації", es = "Dolor durante la regla", de = "Schmerzen während der Periode"))
            LunaCard {
                Text(tr("Jours de douleur gênante ou forte, des règles les plus anciennes aux plus récentes :", "Days of bothersome or severe pain, from the oldest period to the most recent:", pl = "Dni z bólem dokuczliwym lub silnym, od najstarszej do najnowszej miesiączki:", uk = "Дні з помірним або сильним болем, від найдавніших менструацій до найновіших:", es = "Días de dolor molesto o fuerte, de la regla más antigua a la más reciente:", de = "Tage mit mäßigen oder starken Schmerzen, von der ältesten bis zur neuesten Periode:"), style = MaterialTheme.typography.bodyMedium)
                Text(painful.joinToString("  ·  ") { tr("${Format.dayMonth(it.start)} : ${it.painDays}", "${Format.dayMonth(it.start)}: ${it.painDays}", pl = "${Format.dayMonth(it.start)}: ${it.painDays}", uk = "${Format.dayMonth(it.start)}: ${it.painDays}", es = "${Format.dayMonth(it.start)}: ${it.painDays}", de = "${Format.dayMonth(it.start)}: ${it.painDays}") }, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { onArticle("douleurs") }) { Text(tr("Soulager les douleurs", "Relieving pain", pl = "Łagodzenie bólu miesiączkowego", uk = "Полегшення менструального болю", es = "Aliviar el dolor menstrual", de = "Regelschmerzen lindern")) }
            }
        }

        Muted(InsightTexts.HOW_IT_WORKS)
        TextButton(onClick = { openLink(context, Links.SOURCES) }) { Text(tr("Comment c'est calculé", "How it is calculated", pl = "Jak to jest obliczane", uk = "Як це обчислюється", es = "Cómo se calcula", de = "So wird es berechnet")) }
    }
}

/** Une barre par cycle, proportionnelle à sa durée. Chaque barre est lue par le lecteur d'écran. */
@Composable
private fun CycleBars(overview: CycleOverview) {
    val max = overview.longest.toFloat()
    overview.records.forEach { record ->
        Row(
            Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = tr("Cycle du ${Format.dayMonth(record.start)} : ${Format.days(record.length)}", "Cycle from ${Format.dayMonth(record.start)}: ${Format.days(record.length)}", pl = "Cykl od ${Format.dayMonth(record.start)}: ${Format.days(record.length)}", uk = "Цикл від ${Format.dayMonth(record.start)}: ${Format.days(record.length)}", es = "Ciclo del ${Format.dayMonth(record.start)}: ${Format.days(record.length)}", de = "Zyklus ab ${Format.dayMonth(record.start)}: ${Format.days(record.length)}") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(Format.dayMonth(record.start), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(84.dp))
            Box(Modifier.weight(1f).height(14.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(record.length / max)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(7.dp)),
                )
            }
            Text(tr("${record.length} j", "${record.length} d", pl = "${record.length} dn.", uk = "${record.length} дн.", es = "${record.length} d", de = "${record.length} T"), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
        }
    }
}
