package fr.lunaison.core.content

import fr.lunaison.core.cycle.AlertKind
import fr.lunaison.core.cycle.CycleOverview
import fr.lunaison.core.cycle.CycleRecap
import fr.lunaison.core.cycle.CycleRecord
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.cycle.Onset
import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.Symptom
import java.time.LocalDate
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

    private fun setLanguage(language: Language) {
        Locale.setDefault(Locale.forLanguageTag(language.code))
        assertEquals(language, Language.current())
    }

    /** Exécute [block] dans chacune des langues de l'appli. */
    private fun eachLanguage(block: (Language) -> Unit) {
        Language.entries.forEach { language ->
            setLanguage(language)
            block(language)
        }
    }

    private val recap = CycleRecap(
        record = CycleRecord(LocalDate.of(2026, 10, 1), length = 32, periodLength = 5, painDays = 2, expectedLength = 29),
        usualLength = 28,
        withinReferences = true,
        usualPainDays = 1,
    )

    private fun allTexts(): List<String> =
        Articles.all.flatMap { article ->
            listOf(article.title, article.summary) + article.sections.flatMap { listOf(it.heading) + it.paragraphs }
        } +
            PhaseGuides.all.flatMap { listOf(it.name, it.whatHappens) + it.tips.map { tip -> tip.text } } +
            AlertKind.entries.flatMap { listOf(it.title, it.message) } +
            ShareKind.entries.flatMap { listOf(it.label, ShareMessages.text(it, CycleState.Unknown)) } +
            Phase.entries.flatMap {
                val g = ShareMessages.partnerGuide(it)
                listOf(g.phaseTitle, g.whatPartnerShouldKnow) + g.supportTips
            } +
            Flow.entries.map { it.label } + Symptom.entries.map { it.label } + Mood.entries.map { it.label } +
            Pain.entries.flatMap { listOf(it.label, it.description) } +
            InsightTexts.recapLines(recap) + InsightTexts.HOW_IT_WORKS + InsightTexts.NOTHING_YET

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
    fun `chaque conseil et chaque phase sont traduits`() {
        PhaseGuides.all.forEach { guide ->
            guide.tips.forEach { assertNotEquals(it.fr, it.en, "Conseil non traduit : ${it.fr}") }
            assertTrue(guide.allTexts.none { it.isBlank() })
        }
        setLanguage(Language.FR)
        val frNames = PhaseGuides.all.map { it.name }
        setLanguage(Language.EN)
        assertTrue(frNames.zip(PhaseGuides.all.map { it.name }).none { (fr, en) -> fr == en })
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
    fun `toutes les langues ont les memes fiches, sections et sources`() {
        val fr = Articles.forLanguage(Language.FR)
        Language.entries.filter { it != Language.FR }.forEach { lang ->
            val other = Articles.forLanguage(lang)
            assertEquals(fr.map { it.id }, other.map { it.id }, "Fiches manquantes en $lang")
            fr.zip(other).forEach { (a, b) ->
                assertEquals(a.sources, b.sources, "Sources différentes pour ${a.id} en $lang")
                assertEquals(a.sections.map { it.paragraphs.size }, b.sections.map { it.paragraphs.size }, "Structure différente pour ${a.id} en $lang")
                assertNotEquals(a.title, b.title, "Fiche ${a.id} non traduite en $lang")
            }
        }
    }

    @Test
    fun `toutes les sources sont des liens https`() = eachLanguage {
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
        assertTrue(unsourced.isEmpty(), "Conseils sans source : ${unsourced.map { it.fr }}")
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
            assertTrue(Pain.SEVERE in tip.pain, tip.fr)
        }
    }

    @Test
    fun `phrases du bilan sans tiret cadratin et avec les bons comptes`() {
        setLanguage(Language.FR)
        val fr = InsightTexts.recapLines(recap)
        assertEquals("Il a duré 32 jours, 4 jours de plus que d'habitude (28).", fr[0])
        assertEquals("Vos règles sont arrivées 3 jours après la date estimée.", fr[2])
        setLanguage(Language.EN)
        val en = InsightTexts.recapLines(recap)
        assertEquals("It lasted 32 days, 4 days longer than usual (28).", en[0])
        assertEquals("Your period arrived 3 days after the estimated date.", en[2])
        assertTrue((fr + en).none { '—' in it })
    }

    /** Les nombres d'un texte, virgule ou point décimal ramenés au point. */
    private fun numbers(text: String): List<String> =
        Regex("""\d+(?:[.,]\d+)?""").findAll(text).map { it.value.replace(',', '.') }.sorted().toList()

    private val overview = CycleOverview(
        records = List(6) { CycleRecord(LocalDate.of(2026, 1 + it, 1), length = 26 + it, periodLength = 5, painDays = 1, expectedLength = 28) },
        usualLength = 28,
        shortest = 26,
        longest = 31,
        usualPeriodLength = 5,
        withinReferences = false,
        typicalPredictionError = 2,
        predictionsChecked = 5,
    )

    private fun insightLines(): List<String> =
        InsightTexts.recapLines(recap.copy(withinReferences = false)) +
            InsightTexts.overviewLines(overview) +
            InsightTexts.overviewLines(overview.copy(shortest = 28, longest = 28, withinReferences = true)) +
            InsightTexts.predictionLine(overview) +
            InsightTexts.predictionLine(overview.copy(typicalPredictionError = 0)) +
            InsightTexts.predictionLine(overview.copy(typicalPredictionError = null)) +
            InsightTexts.onsetLine(Onset("Label", daysBefore = 3, cycles = 4)) +
            InsightTexts.onsetLine(Onset("Label", daysBefore = 1, cycles = 4)) +
            Phase.entries.map { InsightTexts.phaseName(it) } +
            listOf(InsightTexts.NOTHING_YET, InsightTexts.HOW_IT_WORKS)

    @Test
    fun `les phrases des statistiques sont traduites et gardent les memes nombres que le francais`() {
        setLanguage(Language.FR)
        val french = insightLines()
        setLanguage(Language.EN)
        val english = insightLines()
        assertEquals(french.size, english.size)
        listOf(Language.PL, Language.UK, Language.ES, Language.DE).forEach { language ->
            try {
                setLanguage(language)
                val lines = insightLines()
                assertEquals(french.size, lines.size)
                lines.forEachIndexed { i, line ->
                    assertTrue(line.isNotBlank(), "$language phrase $i vide")
                    assertNotEquals(english[i], line, "$language phrase $i identique à l'anglais")
                    assertNotEquals(french[i], line, "$language phrase $i identique au français")
                    assertEquals(numbers(french[i]), numbers(line), "$language phrase $i : nombres différents du français")
                    assertTrue('—' !in line, "$language phrase $i : tiret cadratin")
                }
            } finally {
                Locale.setDefault(initialLocale)
            }
        }
    }

    /**
     * Les sources ne sont pas affichées dans l'appli : elles sont sur le site, généré depuis documentation/sources.md.
     * Ce test garde les deux alignés, à partir des textes français. En cas d'échec, compléter sources.md puis lancer
     * scripts/site-sources.py.
     */
    @Test
    fun `la page des sources liste chaque fiche et chaque conseil avec leurs sources`() {
        val doc = java.io.File("../../documentation/sources.md").readText()

        fun section(anchor: String): String {
            val start = doc.indexOf("{#$anchor}")
            assertTrue(start >= 0, "Ancre manquante dans sources.md : $anchor")
            val end = doc.indexOf("\n#", start).let { if (it < 0) doc.length else it }
            return doc.substring(start, end)
        }

        Articles.forLanguage(Language.FR).filter { it.sources.isNotEmpty() }.forEach { article ->
            val section = section("fiche-${article.id}")
            article.sources.forEach { assertTrue(it.url in section, "Fiche ${article.id} : source absente de sources.md, ${it.label}") }
        }
        PhaseGuides.all.flatMap { it.tips }.forEach { tip ->
            val row = doc.lines().firstOrNull { it.startsWith("| ${tip.fr} |") }
            assertNotNull(row, "Conseil absent de sources.md : ${tip.fr}")
            tip.sources.forEach { assertTrue(it.url in row, "Source absente pour : ${tip.fr}") }
            if (tip.sources.isEmpty()) assertTrue("Sans étude" in row, "Conseil sans étude non signalé : ${tip.fr}")
        }
    }
}
