package fr.lunaison.core.data

import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Teste l'import d'une sauvegarde « Calendrier des règles » avec des données entièrement fabriquées.
 * L'archive est construite en mémoire : deflate (java.util.zip), puis chiffrement ZipCrypto
 * réimplémenté ici avec la seule JDK, côté chiffrement (l'importeur ne sait que déchiffrer).
 */
class PeriodCalendarImporterTest {

    private val password = "mdp-de-test-2025"

    private val dayEntriesJson = """
        {"entryList": [
          {"entrydatekey": 20250106, "flow": 3, "weight": 130, "symptoms": "fatigue,headaches", "pillTaken": 1},
          {"entrydatekey": 20250107},
          {"entrydatekey": 20250108, "flow": 1, "note": "Note de test", "weight": 60.5},
          {"entrydatekey": 20250109, "hadIntercourseProtected": 1},
          {"entrydatekey": 20250120, "flow": 0, "symptoms": "bloating"},
          {"entrydatekey": 20250215}
        ]}
    """.trimIndent()

    private val cyclesJson = """
        {"cycleList": [
          {"cycleStartDate": 20250106, "cycleEndDate": 20250110},
          {"cycleStartDate": 20250203, "cycleEndDate": 20250207}
        ]}
    """.trimIndent()

    private val settingsJson = """{"averageCLength": 28, "averagePLength": 5, "defaultCLength": 30}"""

    // --- MiniJson ---------------------------------------------------------------------------------

    @Test
    fun `MiniJson lit les valeurs simples`() {
        assertEquals("bonjour \"toi\"\n", MiniJson.parse("\"bonjour \\\"toi\\\"\\n\""))
        assertEquals("é", MiniJson.parse("\"\\u00e9\""))
        assertEquals(42L, MiniJson.parse("42"))
        assertEquals(-7L, MiniJson.parse("-7"))
        assertEquals(3.5, MiniJson.parse("3.5"))
        assertEquals(1500.0, MiniJson.parse("1.5e3"))
        assertEquals(true, MiniJson.parse("true"))
        assertEquals(false, MiniJson.parse("false"))
        assertNull(MiniJson.parse("null"))
    }

    @Test
    fun `MiniJson lit les listes et les objets imbriques`() {
        val parsed = MiniJson.parse(
            """{"a": [1, 2.5, "x", true, null, []], "b": {"c": {"d": [{"e": 1}]}}, "vide": {}}""",
        ) as Map<*, *>
        assertEquals(listOf(1L, 2.5, "x", true, null, emptyList<Any?>()), parsed["a"])
        val d = ((parsed["b"] as Map<*, *>)["c"] as Map<*, *>)["d"] as List<*>
        assertEquals(1L, (d[0] as Map<*, *>)["e"])
        assertEquals(emptyMap<String, Any?>(), parsed["vide"])
    }

    @Test
    fun `MiniJson refuse un JSON invalide`() {
        assertFailsWith<IllegalStateException> { MiniJson.parse("{\"a\" 1}") }
        assertFailsWith<IllegalStateException> { MiniJson.parse("[1 2]") }
        assertFailsWith<IllegalStateException> { MiniJson.parse("\"non termine") }
    }

    // --- Détection et mot de passe ----------------------------------------------------------------

    @Test
    fun `une archive chiffree est detectee et une archive claire ne l'est pas`() {
        val encrypted = buildArchive(password)
        val plain = buildArchive(null)
        assertTrue(PeriodCalendarImporter.isZip(encrypted))
        assertTrue(PeriodCalendarImporter.isEncryptedZip(encrypted))
        assertTrue(PeriodCalendarImporter.isZip(plain))
        assertFalse(PeriodCalendarImporter.isEncryptedZip(plain))
        assertFalse(PeriodCalendarImporter.isZip("pas une archive".toByteArray()))
        assertFalse(PeriodCalendarImporter.isEncryptedZip(ByteArray(0)))
    }

    @Test
    fun `l'import echoue sans mot de passe`() {
        val encrypted = buildArchive(password)
        assertFailsWith<PasswordRequiredException> { PeriodCalendarImporter.importFromZip(encrypted) }
        assertFailsWith<PasswordRequiredException> { PeriodCalendarImporter.importFromZip(encrypted, "") }
    }

    @Test
    fun `l'import echoue avec un mauvais mot de passe`() {
        val encrypted = buildArchive(password)
        for (wrong in listOf("mauvais-mdp", "pas-le-bon", "incorrect-2025")) {
            assertFailsWith<InvalidPasswordException>(wrong) { PeriodCalendarImporter.importFromZip(encrypted, wrong) }
        }
    }

    @Test
    fun `une archive non chiffree s'importe sans mot de passe`() {
        val result = PeriodCalendarImporter.importFromZip(buildArchive(null))
        assertEquals(2, result.cyclesCount)
        assertEquals(11, result.dayLogs.size)
    }

    // --- Import complet ---------------------------------------------------------------------------

    @Test
    fun `l'import d'une archive chiffree reconstitue les journees`() {
        val result = PeriodCalendarImporter.importFromZip(buildArchive(password), password)
        assertEquals(2, result.cyclesCount)
        // 10 jours de règles déclarés + le 20 janvier ; le 15 février, vide, est ignoré.
        assertEquals(11, result.dayLogs.size)
        assertEquals(result.dayLogs.map { it.date }.sorted(), result.dayLogs.map { it.date })
        assertFalse(result.dayLogs.any { it.date == LocalDate.of(2025, 2, 15) })

        val byDate = result.dayLogs.associateBy { it.date }

        val first = byDate.getValue(LocalDate.of(2025, 1, 6))
        assertEquals(Flow.HEAVY, first.flow)
        assertEquals(setOf(Symptom.FATIGUE, Symptom.HEADACHE), first.symptoms)
        assertTrue(first.medicationTaken)
        assertEquals(PillStatus.TAKEN, first.pillStatus)
        // 130 livres = 58,97 kg, arrondi au dixième.
        assertEquals(59.0f, first.weight)

        // Jour de règles sans flux renseigné : flux moyen déduit du cycle.
        assertEquals(Flow.MEDIUM, byDate.getValue(LocalDate.of(2025, 1, 7)).flow)

        val third = byDate.getValue(LocalDate.of(2025, 1, 8))
        assertEquals(Flow.LIGHT, third.flow)
        assertEquals("Note de test", third.note)
        assertEquals(60.5f, third.weight) // déjà en kilos

        assertEquals(setOf(IntimacyKind.PROTECTED), byDate.getValue(LocalDate.of(2025, 1, 9)).intimacy)

        val outside = byDate.getValue(LocalDate.of(2025, 1, 20))
        assertEquals(Flow.SPOTTING, outside.flow)
        assertEquals(setOf(Symptom.BLOATING), outside.symptoms)

        val baseline = assertNotNull(result.baseline)
        assertEquals(28, baseline.usualCycleLength)
        assertEquals(5, baseline.usualPeriodLength)
        assertEquals(LocalDate.of(2025, 2, 3), baseline.lastPeriodStart)
    }

    @Test
    fun `sans reglages la situation de depart est absente`() {
        val result = PeriodCalendarImporter.importFromTexts(dayEntriesJson, cyclesJson, null)
        assertNull(result.baseline)
        assertEquals(2, result.cyclesCount)
    }

    @Test
    fun `une archive sans fichier de journees est refusee`() {
        val bytes = zipEntries(listOf("settings_device.json" to settingsJson), null)
        assertFailsWith<IllegalArgumentException> { PeriodCalendarImporter.importFromZip(bytes) }
    }

    // --- Fabrication de l'archive -----------------------------------------------------------------

    private fun buildArchive(password: String?): ByteArray = zipEntries(
        listOf(
            "dayEntries_device.json" to dayEntriesJson,
            "cyclesData_device.json" to cyclesJson,
            "settings_device.json" to settingsJson,
        ),
        password,
    )

    /** Écrit des en-têtes locaux zip (méthode deflate), chiffrés en ZipCrypto si un mot de passe est fourni. */
    private fun zipEntries(entries: List<Pair<String, String>>, password: String?): ByteArray {
        val out = ByteArrayOutputStream()
        for ((name, content) in entries) {
            val raw = content.toByteArray(Charsets.UTF_8)
            val crc = CRC32().also { it.update(raw) }.value
            val deflated = deflate(raw)
            val payload = if (password == null) deflated else zipCryptoEncrypt(password, deflated, crc)
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val header = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
            header.putInt(0x04034B50)
            header.putShort(20.toShort()) // version nécessaire
            header.putShort((if (password == null) 0 else 1).toShort()) // bit 0 : chiffré, pas de descripteur de données
            header.putShort(8.toShort()) // deflate
            header.putShort(0.toShort()) // heure
            header.putShort(0x2A21.toShort()) // date (1er janvier 2025)
            header.putInt(crc.toInt())
            header.putInt(payload.size)
            header.putInt(raw.size)
            header.putShort(nameBytes.size.toShort())
            header.putShort(0.toShort()) // pas de champ supplémentaire
            out.write(header.array())
            out.write(nameBytes)
            out.write(payload)
        }
        return out.toByteArray()
    }

    private fun deflate(data: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true) // flux brut, comme dans un zip
        deflater.setInput(data)
        deflater.finish()
        val out = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        while (!deflater.finished()) out.write(buf, 0, deflater.deflate(buf))
        deflater.end()
        return out.toByteArray()
    }

    /** ZipCrypto : 12 octets d'en-tête (le dernier est l'octet de contrôle CRC) puis les données. */
    private fun zipCryptoEncrypt(password: String, data: ByteArray, crc: Long): ByteArray {
        val crcTable = IntArray(256) { i ->
            var c = i
            repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
            c
        }
        var key0 = 305419896
        var key1 = 591751049
        var key2 = 878082192
        fun crcStep(crc32: Int, b: Int) = (crc32 ushr 8) xor crcTable[(crc32 xor b) and 0xFF]
        fun update(b: Int) {
            key0 = crcStep(key0, b)
            key1 = (key1 + (key0 and 0xFF)) * 134775813 + 1
            key2 = crcStep(key2, (key1 ushr 24) and 0xFF)
        }
        fun encryptByte(plain: Int): Byte {
            val k = (key2 or 2) and 0xFFFF
            val magic = ((k * (k xor 1)) ushr 8) and 0xFF
            update(plain)
            return (plain xor magic).toByte()
        }
        password.toByteArray(Charsets.UTF_8).forEach { update(it.toInt() and 0xFF) }
        val header = ByteArray(12) { (it + 1).toByte() } // octets fixes : le test reste déterministe
        header[11] = ((crc ushr 24) and 0xFF).toByte()
        val out = ByteArray(12 + data.size)
        for (i in header.indices) out[i] = encryptByte(header[i].toInt() and 0xFF)
        for (i in data.indices) out[12 + i] = encryptByte(data[i].toInt() and 0xFF)
        return out
    }
}
