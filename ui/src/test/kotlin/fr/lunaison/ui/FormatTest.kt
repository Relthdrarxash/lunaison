package fr.lunaison.ui

import java.time.LocalDate
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    private val original = Locale.getDefault()

    @AfterTest
    fun restore() = Locale.setDefault(original)

    // Semaine du lundi 2 au dimanche 8 novembre 2026.
    private val week = (2..8).map { LocalDate.of(2026, 11, it) }

    private fun initials(tag: String): String {
        Locale.setDefault(Locale.forLanguageTag(tag))
        return week.joinToString("") { Format.narrowWeekday(it) }
    }

    @Test
    fun weekdayInitialsFollowTheLanguage() {
        assertEquals("LMMJVSD", initials("fr"))
        assertEquals("MTWTFSS", initials("en"))
        assertEquals("LMXJVSD", initials("es"))
        assertEquals("MDMDFSS", initials("de"))
    }
}
