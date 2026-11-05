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

    private val sampleZipPath = "/home/d07pwn3d/Téléchargements/periodCalendarData2026-11-05_20-00-47582.zip"
    private val samplePassword = "RY5RxFImUjq2MXMH"

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

    @Test
    fun detectsEncryptedZip() {
        val file = File(sampleZipPath)
        if (!file.exists()) return // Skip if file not on this host
        val bytes = file.readBytes()
        assertTrue(PeriodCalendarImporter.isZip(bytes))
        assertTrue(PeriodCalendarImporter.isEncryptedZip(bytes))
    }

    @Test
    fun failsWithoutPasswordOnEncryptedZip() {
        val file = File(sampleZipPath)
        if (!file.exists()) return
        val bytes = file.readBytes()
        assertFailsWith<PasswordRequiredException> {
            PeriodCalendarImporter.importFromZip(bytes, null)
        }
    }

    @Test
    fun failsWithWrongPasswordOnEncryptedZip() {
        val file = File(sampleZipPath)
        if (!file.exists()) return
        val bytes = file.readBytes()
        assertFailsWith<InvalidPasswordException> {
            PeriodCalendarImporter.importFromZip(bytes, "badpassword123")
        }
    }

    @Test
    fun importsPeriodCalendarSampleZipSuccessfully() {
        val file = File(sampleZipPath)
        if (!file.exists()) return
        val bytes = file.readBytes()
        val result = PeriodCalendarImporter.importFromZip(bytes, samplePassword)

        assertEquals(4, result.cyclesCount)
        assertEquals(40, result.dayLogs.size)

        val logsByDate = result.dayLogs.associateBy { it.date }

        // Vérification des cycles reconstitués
        // Cycle 1 : 2026-02-20 au 2026-02-24 (tous avec flow)
        for (d in listOf(
            LocalDate.of(2026, 2, 20),
            LocalDate.of(2026, 2, 21),
            LocalDate.of(2026, 2, 22),
            LocalDate.of(2026, 2, 23),
            LocalDate.of(2026, 2, 24),
        )) {
            val log = logsByDate[d]
            assertNotNull(log, "La date $d devrait être présente dans l'import")
            assertEquals(Flow.MEDIUM, log.flow)
        }

        // Jour avec poids et symptômes (2026-04-24)
        val d24 = logsByDate[LocalDate.of(2026, 4, 24)]
        assertNotNull(d24)
        assertEquals(62.0f, d24.weight) // 136.69 lbs converti en 62.0 kg
        assertTrue(Symptom.ACNE in d24.symptoms)

        // Jour avec note (2026-05-12)
        val dMay12 = logsByDate[LocalDate.of(2026, 5, 12)]
        assertNotNull(dMay12)
        assertEquals("Prise de la pillule du lendemain", dMay12.note)
        assertTrue(IntimacyKind.UNPROTECTED in dMay12.intimacy)

        // Jour avec pilule et flow (2026-05-24)
        val dMay24 = logsByDate[LocalDate.of(2026, 5, 24)]
        assertNotNull(dMay24)
        assertEquals(PillStatus.TAKEN, dMay24.pillStatus)
        assertTrue(dMay24.medicationTaken)
        assertEquals(Flow.MEDIUM, dMay24.flow)

        // Vérification du Baseline reconstitué
        assertNotNull(result.baseline)
        assertEquals(31, result.baseline.usualCycleLength)
        assertEquals(6, result.baseline.usualPeriodLength)
        assertEquals(LocalDate.of(2026, 5, 24), result.baseline.lastPeriodStart)
    }
}
