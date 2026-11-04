package fr.lunaison.core.content

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentTest {

    private fun allTexts(): List<String> =
        Articles.all.flatMap { article ->
            listOf(article.title, article.summary) + article.sections.flatMap { listOf(it.heading) + it.paragraphs }
        } +
            PhaseGuides.all.flatMap { listOf(it.name, it.whatHappens) + it.tips } +
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
        val cited = Articles.all.flatMap { it.sources }.toSet()
        assertTrue(Sources.all.toSet().containsAll(cited), "Une source citée manque à la liste générale")
    }

    @Test
    fun `chaque etat propose des messages de partage`() {
        assertTrue(ShareMessages.suggestions(CycleState.Unknown).isNotEmpty())
    }
}
