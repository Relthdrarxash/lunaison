package fr.lunaison.core.data

import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import java.io.File
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PeriodCalendarImporterTest {


    @Test
    fun miniJsonParsesCorrectly() {
        val json = """
            {
                "name": "Test",
                "count": 42,
                "ratio": 3.14,
                "flag": true,
                "none": null,
                "list": [1, 2, "item"],
                "nested": { "key": "val" }
            }
        """.trimIndent()
        @Suppress("UNCHECKED_CAST")
        val parsed = MiniJson.parse(json) as Map<String, Any?>
        assertEquals("Test", parsed["name"])
        assertEquals(42L, parsed["count"])
        assertEquals(3.14, parsed["ratio"])
        assertEquals(true, parsed["flag"])
        assertEquals(null, parsed["none"])
        assertEquals(listOf(1L, 2L, "item"), parsed["list"])
    }

}
