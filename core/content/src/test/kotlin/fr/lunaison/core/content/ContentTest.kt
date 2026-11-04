package fr.lunaison.core.content

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentTest {

    private val initialLocale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(initialLocale)

    /** Exécute [block] dans chacune des langues de l'appli. */
    private fun eachLanguage(block: (Language) -> Unit) {
        Language.entries.forEach { language ->
            Locale.setDefault(if (language == Language.FR) Locale.FRENCH else Locale.ENGLISH)
            assertEquals(language, Language.current())
            block(language)
        }
    }

    private fun allTexts(): List<String> =
        Articles.all.flatMap { article ->
            listOf(article.title, article.summary) + article.sections.flatMap { listOf(it.heading) + it.paragraphs }
        } +
            PhaseGuides.all.flatMap { listOf(it.name, it.whatHappens) + it.tips } +
            AlertKind.entries.flatMap { listOf(it.title, it.message) } +
            ShareKind.entries.flatMap { listOf(it.label, ShareMessages.text(it, CycleState.Unknown)) } +
            Flow.entries.map { it.label } + Symptom.entries.map { it.label } + Mood.entries.map { it.label } +
            Pain.entries.flatMap { listOf(it.label, it.description) }

    @Test
    fun `aucun tiret cadratin dans les textes`() = eachLanguage {
        val offending = allTexts().filter { '—' in it }
        assertTrue(offending.isEmpty(), "Tirets cadratins : $offending")
    }

    @Test
    fun `aucun texte vide`() = eachLanguage { language ->
        assertTrue(allTexts().none { it.isBlank() }, "Texte vide en $language")
    }

    @Test
    fun `chaque fiche de sante cite au moins une source`() = eachLanguage {
        val unsourced = Articles.all.filter { it.sources.isEmpty() && it.id != "entourage" }
        assertTrue(unsourced.isEmpty(), "Fiches sans source : ${unsourced.map { it.id }}")
    }

    @Test
    fun `les identifiants de fiches sont uniques`() = eachLanguage {
        assertEquals(Articles.all.size, Articles.all.map { it.id }.toSet().size)
    }

    @Test
    fun `chaque signal et chaque phase renvoient vers une fiche existante`() = eachLanguage {
        AlertKind.entries.forEach { assertNotNull(Articles.byId(it.articleId), "Fiche manquante pour ${it.name}") }
        Phase.entries.forEach { assertNotNull(Articles.byId(PhaseGuides.forPhase(it).articleId), "Fiche manquante pour $it") }
    }

    @Test
    fun `les deux langues ont les memes fiches, sections et sources`() {
        val fr = Articles.forLanguage(Language.FR)
        val en = Articles.forLanguage(Language.EN)
        assertEquals(fr.map { it.id }, en.map { it.id })
        fr.zip(en).forEach { (a, b) ->
            assertEquals(a.sources, b.sources, "Sources différentes pour ${a.id}")
            assertEquals(a.sections.map { it.paragraphs.size }, b.sections.map { it.paragraphs.size }, "Structure différente pour ${a.id}")
            assertNotEquals(a.title, b.title, "Fiche ${a.id} non traduite")
        }
        val frGuides = PhaseGuides.forLanguage(Language.FR)
        val enGuides = PhaseGuides.forLanguage(Language.EN)
        assertEquals(frGuides.map { it.phase to it.articleId }, enGuides.map { it.phase to it.articleId })
        assertEquals(frGuides.map { it.tips.size }, enGuides.map { it.tips.size })
    }

    @Test
    fun `toutes les sources sont des liens https`() = eachLanguage {
        Sources.all.forEach { assertTrue(it.url.startsWith("https://"), it.label) }
        val cited = Articles.all.flatMap { it.sources }.toSet()
        assertTrue(Sources.all.toSet().containsAll(cited), "Une source citée manque à la liste générale")
    }

    @Test
    fun `chaque etat propose des messages de partage`() {
        assertTrue(ShareMessages.suggestions(CycleState.Unknown).isNotEmpty())
    }
}
