package fr.lunaison.core.cycle

import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Garde-fou des alertes « à voir avec un professionnel de santé » : ce sont des seuils médicaux,
 * une traduction ne doit jamais en changer un chiffre. Le test compare, langue par langue,
 * les nombres du texte avec ceux du français, qui fait foi.
 */
class AlertsTranslationTest {

    private val initialLocale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(initialLocale)

    private val translated = listOf("pl", "uk", "es", "de")

    private fun <T> inLanguage(code: String, block: () -> T): T {
        Locale.setDefault(Locale.forLanguageTag(code))
        try {
            return block()
        } finally {
            Locale.setDefault(initialLocale)
        }
    }

    /** Les nombres d'un texte, avec la virgule ou le point décimal ramenés au point. */
    private fun numbers(text: String): Set<String> =
        Regex("""\d+(?:[.,]\d+)?""").findAll(text).map { it.value.replace(',', '.') }.toSet()

    private fun frenchTitle(kind: AlertKind) = inLanguage("fr") { kind.title }
    private fun frenchMessage(kind: AlertKind) = inLanguage("fr") { kind.message }
    private fun english(kind: AlertKind) = inLanguage("en") { kind.title to kind.message }

    @Test
    fun `les seuils des alertes sont bien ceux attendus en francais`() {
        assertEquals(setOf("8"), numbers(frenchTitle(AlertKind.LONG_PERIOD) + frenchMessage(AlertKind.LONG_PERIOD)))
        assertEquals(setOf("1", "2", "2.5"), numbers(frenchMessage(AlertKind.HEAVY_FLOW)))
        assertEquals(setOf("9"), numbers(frenchMessage(AlertKind.IRREGULAR)))
        assertEquals(setOf("90"), numbers(frenchMessage(AlertKind.NO_PERIOD)))
    }

    @Test
    fun `chaque alerte est traduite et ne se replie pas sur l'anglais`() {
        translated.forEach { code ->
            AlertKind.entries.forEach { kind ->
                val (enTitle, enMessage) = english(kind)
                val title = inLanguage(code) { kind.title }
                val message = inLanguage(code) { kind.message }
                assertTrue(title.isNotBlank(), "$code $kind : titre vide")
                assertTrue(message.isNotBlank(), "$code $kind : message vide")
                assertNotEquals(enTitle, title, "$code $kind : titre identique à l'anglais")
                assertNotEquals(enMessage, message, "$code $kind : message identique à l'anglais")
                assertNotEquals(frenchTitle(kind), title, "$code $kind : titre identique au français")
                assertNotEquals(frenchMessage(kind), message, "$code $kind : message identique au français")
            }
        }
    }

    @Test
    fun `chaque alerte garde les memes nombres que le francais`() {
        translated.forEach { code ->
            AlertKind.entries.forEach { kind ->
                val title = inLanguage(code) { kind.title }
                val message = inLanguage(code) { kind.message }
                assertEquals(numbers(frenchTitle(kind)), numbers(title), "$code $kind : nombres du titre")
                assertEquals(numbers(frenchMessage(kind)), numbers(message), "$code $kind : nombres du message")
            }
        }
    }

    @Test
    fun `l'anglais garde aussi les memes seuils que le francais`() {
        AlertKind.entries.forEach { kind ->
            val (enTitle, enMessage) = english(kind)
            assertEquals(numbers(frenchTitle(kind)), numbers(enTitle), "en $kind : nombres du titre")
            // L'anglais ajoute « (1 inch) » entre parenthèses : 1 est déjà dans « 1 à 2 heures ».
            assertEquals(numbers(frenchMessage(kind)), numbers(enMessage), "en $kind : nombres du message")
        }
    }
}
