package fr.lunaison.core.content

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.CycleRecord
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Pain
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentTest {

    private fun allTexts(): List<String> =
        Articles.all.flatMap { article ->
            listOf(article.title, article.summary) + article.sections.flatMap { listOf(it.heading) + it.paragraphs }
        } +
            PhaseGuides.all.flatMap { listOf(it.name, it.whatHappens) + it.tips.map { tip -> tip.text } } +
            AlertKind.entries.flatMap { listOf(it.title, it.message) } +
            ShareKind.entries.map { ShareMessages.text(it, CycleState.Unknown) }

    @Test
    fun `aucun tiret cadratin dans les textes`() {
        val offending = allTexts().filter { '—' in it }
        assertTrue(offending.isEmpty(), "Tirets cadratins : $offending")
    }

    @Test
    fun `chaque fiche de sante cite au moins une source`() {
        val unsourced = Articles.all.filter { it.sources.isEmpty() && it.id != "entourage" }
        assertTrue(unsourced.isEmpty(), "Fiches sans source : ${unsourced.map { it.id }}")
    }

    @Test
    fun `les identifiants de fiches sont uniques`() {
        assertEquals(Articles.all.size, Articles.all.map { it.id }.toSet().size)
    }

    @Test
    fun `chaque signal et chaque phase renvoient vers une fiche existante`() {
        AlertKind.entries.forEach { assertNotNull(Articles.byId(it.articleId), "Fiche manquante pour ${it.name}") }
        Phase.entries.forEach { assertNotNull(Articles.byId(PhaseGuides.forPhase(it).articleId), "Fiche manquante pour $it") }
    }

    @Test
    fun `toutes les sources sont des liens https`() {
        Sources.all.forEach { assertTrue(it.url.startsWith("https://"), it.label) }
        val cited = (Articles.all.flatMap { it.sources } + PhaseGuides.all.flatMap { g -> g.tips.flatMap { it.sources } }).toSet()
        assertTrue(Sources.all.toSet().containsAll(cited), "Une source citée manque à la liste générale")
    }

    @Test
    fun `chaque etat propose des messages de partage`() {
        assertTrue(ShareMessages.suggestions(CycleState.Unknown).isNotEmpty())
    }

    @Test
    fun `chaque conseil cite une source ou se declare sans etude`() {
        val unsourced = PhaseGuides.all.flatMap { it.tips }.filter { it.sources.isEmpty() && !it.withoutStudy }
        assertTrue(unsourced.isEmpty(), "Conseils sans source : ${unsourced.map { it.text }}")
    }

    @Test
    fun `cinq conseils au moins par phase du cycle`() {
        listOf(Phase.MENSTRUAL, Phase.FOLLICULAR, Phase.OVULATION, Phase.LUTEAL).forEach { phase ->
            assertTrue(PhaseGuides.forPhase(phase).tips.size >= 5, "Pas assez de conseils pour $phase")
        }
    }

    @Test
    fun `le conseil du jour change chaque jour et ne revient qu'apres tous les autres`() {
        val guide = PhaseGuides.menstrual
        val start = LocalDate.of(2026, 11, 1)
        val week = (0 until guide.tips.size).map { DailyTip.pick(guide, start.plusDays(it.toLong()), null) }
        assertEquals(guide.tips.toSet(), week.toSet())
        assertEquals(DailyTip.pick(guide, start, null), DailyTip.pick(guide, start, DayLog(start)))
    }

    @Test
    fun `une douleur forte notee fait passer un conseil sur la douleur`() {
        val today = LocalDate.of(2026, 11, 1)
        val log = DayLog(today, pain = Pain.SEVERE)
        (0 until 10).forEach { offset ->
            val date = today.plusDays(offset.toLong())
            val tip = assertNotNull(DailyTip.pick(PhaseGuides.menstrual, date, log.copy(date = date)))
            assertTrue(Pain.SEVERE in tip.pain, tip.text)
        }
    }

    @Test
    fun `phrases du bilan sans tiret cadratin et avec les bons comptes`() {
        val recap = CycleRecap(
            record = CycleRecord(LocalDate.of(2026, 10, 1), length = 32, periodLength = 5, painDays = 2, expectedLength = 29),
            usualLength = 28,
            withinReferences = true,
            usualPainDays = 1,
        )
        val lines = InsightTexts.recapLines(recap)
        assertEquals("Il a duré 32 jours, 4 jours de plus que d'habitude (28).", lines[0])
        assertEquals("Vos règles sont arrivées 3 jours après la date estimée.", lines[2])
        assertTrue(lines.none { '—' in it })
        assertTrue('—' !in InsightTexts.HOW_IT_WORKS && '—' !in InsightTexts.NOTHING_YET)
    }
}
