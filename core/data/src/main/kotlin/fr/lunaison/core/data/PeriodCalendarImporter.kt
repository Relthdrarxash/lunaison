package fr.lunaison.core.data

import fr.lunaison.core.model.AgeGroup
import fr.lunaison.core.model.Baseline
import fr.lunaison.core.model.CervicalMucus
import fr.lunaison.core.model.DayLog
import fr.lunaison.core.model.Flow
import fr.lunaison.core.model.IntimacyKind
import fr.lunaison.core.model.Libido
import fr.lunaison.core.model.Mood
import fr.lunaison.core.model.Pain
import fr.lunaison.core.model.PillStatus
import fr.lunaison.core.model.Symptom
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.util.zip.Inflater
import kotlin.math.roundToInt

class PasswordRequiredException(message: String = "Cette archive est protégée par un mot de passe") : Exception(message)
class InvalidPasswordException(message: String = "Mot de passe de sauvegarde incorrect") : Exception(message)

data class ImportSummary(val daysCount: Int, val cyclesCount: Int)

data class PeriodCalendarImportResult(
    val dayLogs: List<DayLog>,
    val cyclesCount: Int,
    val baseline: Baseline?,
)

/**
 * Importe les sauvegardes exportées par l'application « Calendrier des règles » (Period Calendar / Simple Design Ltd).
 * Gère le déchiffrement ZipCrypto sans dépendance externe et extrait les cycles, flux, symptômes, humeurs,
 * prises de pilule, rapports et notes.
 */
object PeriodCalendarImporter {

    private val CRC_TABLE = IntArray(256) { i ->
        var c = i
        repeat(8) {
            c = if ((c and 1) != 0) (c ushr 1) xor 0xEDB88320.toInt() else (c ushr 1)
        }
        c
    }

    private fun crc32Byte(crc: Int, b: Int): Int = (crc ushr 8) xor CRC_TABLE[(crc xor b) and 0xFF]

    fun isZip(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            bytes[2] == 0x03.toByte() &&
            bytes[3] == 0x04.toByte()

    fun isEncryptedZip(bytes: ByteArray): Boolean {
        if (!isZip(bytes) || bytes.size < 8) return false
        val flag = (bytes[6].toInt() and 0xFF) or ((bytes[7].toInt() and 0xFF) shl 8)
        return (flag and 1) != 0
    }

    /**
     * Décompresse et déchiffre (si nécessaire avec ZipCrypto) les fichiers texte d'une archive zip.
     */
    fun extractZip(bytes: ByteArray, password: String? = null): Map<String, String> {
        val files = mutableMapOf<String, String>()
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 0
        val total = bytes.size

        while (pos + 30 <= total) {
            buf.position(pos)
            val sig = buf.int
            if (sig != 0x04034B50) break // Signature d'en-tête local PK\x03\x04
            buf.short // version needed
            val flag = buf.short.toInt() and 0xFFFF
            val method = buf.short.toInt() and 0xFFFF
            val mtime = buf.short.toInt() and 0xFFFF
            buf.short // mdate
            val crc = buf.int.toLong() and 0xFFFFFFFFL
            val csize = buf.int.toLong() and 0xFFFFFFFFL
            buf.int // usize
            val fnlen = buf.short.toInt() and 0xFFFF
            val exlen = buf.short.toInt() and 0xFFFF

            val nameBytes = ByteArray(fnlen)
            buf.get(nameBytes)
            val fileName = String(nameBytes, Charsets.UTF_8)
            buf.position(buf.position() + exlen)

            val dataStart = buf.position()
            val dataLen = csize.toInt()
            if (dataStart + dataLen > total) break

            val entryData = ByteArray(dataLen)
            buf.get(entryData)
            pos = buf.position()

            val isEncrypted = (flag and 1) != 0
            val decompressedBytes = if (isEncrypted) {
                if (password.isNullOrEmpty()) {
                    throw PasswordRequiredException()
                }
                val decrypter = ZipCrypto(password.toByteArray(Charsets.UTF_8))
                val decrypted = decrypter.decrypt(entryData)
                if (decrypted.size < 12) throw InvalidPasswordException()

                // Vérification du mot de passe via le 12e octet d'en-tête
                val expectedCheck = if ((flag and 0x08) != 0) (mtime ushr 8) and 0xFF else ((crc ushr 24) and 0xFFL).toInt()
                val actualCheck = decrypted[11].toInt() and 0xFF
                if (actualCheck != expectedCheck) {
                    throw InvalidPasswordException()
                }

                val payload = decrypted.copyOfRange(12, decrypted.size)
                decompress(payload, method)
            } else {
                decompress(entryData, method)
            }

            files[fileName] = String(decompressedBytes, Charsets.UTF_8)
        }

        return files
    }

    private fun decompress(data: ByteArray, method: Int): ByteArray {
        return when (method) {
            0 -> data // Stored
            8 -> { // Deflated
                val inflater = Inflater(true) // nowrap = true pour flux brut sans zlib header
                inflater.setInput(data)
                val out = ByteArrayOutputStream()
                val tmp = ByteArray(4096)
                try {
                    while (!inflater.finished()) {
                        val count = inflater.inflate(tmp)
                        if (count == 0) {
                            if (inflater.needsInput() || inflater.needsDictionary()) break
                        } else {
                            out.write(tmp, 0, count)
                        }
                    }
                } catch (e: Exception) {
                    throw InvalidPasswordException("Données compressées invalides ou mot de passe erroné")
                } finally {
                    inflater.end()
                }
                out.toByteArray()
            }
            else -> data
        }
    }

    /**
     * Parse un zip Period Calendar et reconstruit les journées et paramètres.
     */
    fun importFromZip(bytes: ByteArray, password: String? = null): PeriodCalendarImportResult {
        val files = extractZip(bytes, password)
        val dayEntriesJson = files["dayEntries_device.json"] ?: files["dayEntries_backup.json"]
            ?: throw IllegalArgumentException("Fichier dayEntries introuvable dans l'archive")
        val cyclesJson = files["cyclesData_device.json"] ?: files["cyclesData_backup.json"]
        val settingsJson = files["settings_device.json"] ?: files["settings_backup.json"]

        return importFromTexts(dayEntriesJson = dayEntriesJson, cyclesJson = cyclesJson, settingsJson = settingsJson)
    }

    /**
     * Parse les chaînes JSON brutes issues de Period Calendar.
     */
    @Suppress("UNCHECKED_CAST")
    fun importFromTexts(
        dayEntriesJson: String,
        cyclesJson: String? = null,
        settingsJson: String? = null,
    ): PeriodCalendarImportResult {
        val dayEntriesObj = MiniJson.parse(dayEntriesJson) as? Map<String, Any?> ?: emptyMap()
        val cyclesObj = cyclesJson?.let { MiniJson.parse(it) as? Map<String, Any?> }
        val settingsObj = settingsJson?.let { MiniJson.parse(it) as? Map<String, Any?> }

        val cycleSpans = mutableListOf<ClosedRange<LocalDate>>()
        val rawCycleList = cyclesObj?.get("cycleList") as? List<Map<String, Any?>>
        if (rawCycleList != null) {
            for (c in rawCycleList) {
                val s = parseDateKey(c["cycleStartDate"]) ?: continue
                val e = parseDateKey(c["cycleEndDate"]) ?: s
                cycleSpans += s..e
            }
        }

        val entriesByDate = mutableMapOf<LocalDate, Map<String, Any?>>()
        val rawEntryList = dayEntriesObj["entryList"] as? List<Map<String, Any?>>
        if (rawEntryList != null) {
            for (entry in rawEntryList) {
                val date = parseDateKey(entry["entrydatekey"]) ?: continue
                entriesByDate[date] = entry
            }
        }

        // Compléter l'ensemble des dates : inclure chaque jour des cycles déclarés
        val allDates = mutableSetOf<LocalDate>()
        allDates.addAll(entriesByDate.keys)
        for (span in cycleSpans) {
            var cur = span.start
            while (!cur.isAfter(span.endInclusive)) {
                allDates.add(cur)
                cur = cur.plusDays(1)
            }
        }

        val dayLogs = mutableListOf<DayLog>()
        for (date in allDates.sorted()) {
            val entry = entriesByDate[date] ?: emptyMap()
            val inPeriodSpan = cycleSpans.any { date in it }
            val pStarted = (entry["pStartedToday"] as? Number)?.toInt() == 1
            val pEnded = (entry["pEndedToday"] as? Number)?.toInt() == 1

            val rawFlowNum = (entry["flow"] as? Number)?.toInt()
            val rawSymptoms = (entry["symptoms"] as? String).orEmpty()
            val rawMoods = (entry["moods"] as? String).orEmpty()

            var flow: Flow? = when (rawFlowNum) {
                1 -> Flow.LIGHT
                2 -> Flow.MEDIUM
                3 -> Flow.HEAVY
                0 -> Flow.SPOTTING
                else -> null
            }
            if (flow == null) {
                if (inPeriodSpan || pStarted || pEnded) {
                    flow = Flow.MEDIUM
                } else if ("spotting" in rawSymptoms) {
                    flow = Flow.SPOTTING
                } else if ("bleeding" in rawSymptoms) {
                    flow = Flow.MEDIUM
                }
            }

            val symptoms = mutableSetOf<Symptom>()
            var mucus: CervicalMucus? = null
            var libido: Libido? = null
            var pain: Pain? = null

            for (s in rawSymptoms.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) {
                when (s) {
                    "headaches", "migraines" -> symptoms += Symptom.HEADACHE
                    "acne", "cskin" -> symptoms += Symptom.ACNE
                    "dizziness" -> symptoms += Symptom.DIZZINESS
                    "acramps", "cramps" -> {
                        symptoms += Symptom.CRAMPS
                        if (pain == null) pain = Pain.MODERATE
                    }
                    "backaches", "baches" -> symptoms += Symptom.BACK_PAIN
                    "bsensitivity", "btenderness", "tbreast", "bengorgement", "lbreasts" -> symptoms += Symptom.BREAST_TENDERNESS
                    "mpain", "opain", "spain" -> {
                        symptoms += Symptom.PELVIC_PAIN
                        if (pain == null) pain = Pain.MILD
                    }
                    "nsweats" -> symptoms += Symptom.NIGHT_SWEATS
                    "hflashes", "hfever", "hfewer" -> symptoms += Symptom.HOT_FLASHES
                    "bloating" -> symptoms += Symptom.BLOATING
                    "gas", "constipation", "diarrhea", "areflux" -> symptoms += Symptom.DIGESTIVE
                    "cravings", "hunger" -> symptoms += Symptom.CRAVINGS
                    "nausea", "msickness" -> symptoms += Symptom.NAUSEA
                    "fatigue" -> symptoms += Symptom.FATIGUE
                    "insomnia", "sproblems" -> symptoms += Symptom.SLEEP
                    "confusion", "uconcentrate" -> symptoms += Symptom.BRAIN_FOG
                    "dry" -> mucus = CervicalMucus.DRY
                    "sticky" -> mucus = CervicalMucus.STICKY
                    "creamy" -> mucus = CervicalMucus.CREAMY
                    "watery", "ewhite" -> mucus = CervicalMucus.EGG_WHITE
                    "dlibido" -> libido = Libido.LOW
                    "nlibido" -> libido = Libido.NORMAL
                    "ilibido" -> libido = Libido.HIGH
                }
            }

            val moods = mutableSetOf<Mood>()
            for (m in rawMoods.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }) {
                when (m) {
                    "stressed", "stress", "tension" -> moods += Mood.STRESSED
                    "anxious", "anxiety", "worried", "panicky" -> moods += Mood.ANXIOUS
                    "tense", "irritable", "irritability", "frustrated", "angry", "grumpy", "cranky" -> moods += Mood.IRRITABLE
                    "sad", "depressed", "disappointed", "tormented", "lonely", "ashamed", "bored" -> moods += Mood.SAD
                    "emotional", "weird", "insecure" -> moods += Mood.SENSITIVE
                    "sleepy", "exhausted" -> moods += Mood.LOW_ENERGY
                    "peaceful", "relaxed" -> moods += Mood.CALM
                    "happy", "excited", "playful", "silly", "horny", "inlove" -> moods += Mood.HAPPY
                    "hyped" -> moods += Mood.ENERGETIC
                    "focused", "accomplished", "confident", "proud", "assertive" -> moods += Mood.MOTIVATED
                }
            }

            val intimacy = mutableSetOf<IntimacyKind>()
            if ((entry["hadIntercourseProtected"] as? Number)?.toInt() == 1) {
                intimacy += IntimacyKind.PROTECTED
            }
            if ((entry["hadIntercorse"] as? Number)?.toInt() == 1 || (entry["hadIntercourse"] as? Number)?.toInt() == 1) {
                intimacy += IntimacyKind.UNPROTECTED
            }
            if ((entry["masturbation"] as? Number)?.toInt() == 1) {
                intimacy += IntimacyKind.NON_PENETRATIVE
            }

            val pillTaken = (entry["pillTaken"] as? Number)?.toInt() == 1
            val pillStatus = if (pillTaken) PillStatus.TAKEN else null

            val note = (entry["note"] as? String).orEmpty().take(500)

            val rawWeight = (entry["weight"] as? Number)?.toFloat()
            val weight = rawWeight?.let { w ->
                if (w > 110f) {
                    // Poids stocké en livres (lbs) par Period Calendar : conversion en kg
                    ((w * 0.45359237f) * 10f).roundToInt() / 10f
                } else {
                    (w * 10f).roundToInt() / 10f
                }
            }

            val log = DayLog(
                date = date,
                flow = flow,
                pain = pain,
                symptoms = symptoms,
                moods = moods,
                note = note,
                medicationTaken = pillTaken,
                pillStatus = pillStatus,
                weight = weight,
                intimacy = intimacy,
                libido = libido,
                mucus = mucus,
            )
            if (!log.isEmpty) {
                dayLogs += log
            }
        }

        // Reconstitution des repères de base depuis les réglages
        var baseline: Baseline? = null
        if (settingsObj != null) {
            val usualCycle = (settingsObj["averageCLength"] as? Number)?.toInt()
                ?: (settingsObj["defaultCLength"] as? Number)?.toInt()
            val usualPeriod = (settingsObj["averagePLength"] as? Number)?.toInt()
                ?: (settingsObj["defaultPLength"] as? Number)?.toInt()
            val lastPeriodStart = cycleSpans.maxByOrNull { it.start }?.start
            if (usualCycle != null || usualPeriod != null || lastPeriodStart != null) {
                baseline = Baseline(
                    ageGroup = AgeGroup.ADULT,
                    lastPeriodStart = lastPeriodStart,
                    usualCycleLength = usualCycle,
                    usualPeriodLength = usualPeriod,
                )
            }
        }

        return PeriodCalendarImportResult(
            dayLogs = dayLogs,
            cyclesCount = cycleSpans.size,
            baseline = baseline,
        )
    }

    private fun parseDateKey(v: Any?): LocalDate? {
        val num = (v as? Number)?.toLong() ?: return null
        val str = num.toString()
        if (str.length != 8) return null
        val y = str.substring(0, 4).toIntOrNull() ?: return null
        val m = str.substring(4, 6).toIntOrNull() ?: return null
        val d = str.substring(6, 8).toIntOrNull() ?: return null
        return runCatching { LocalDate.of(y, m, d) }.getOrNull()
    }

    /**
     * Moteur de déchiffrement ZipCrypto (PKWARE Traditional Encryption).
     */
    private class ZipCrypto(password: ByteArray) {
        private var key0: Int = 305419896
        private var key1: Int = 591751049
        private var key2: Int = 878082192

        init {
            for (b in password) {
                updateKeys(b.toInt() and 0xFF)
            }
        }

        private fun updateKeys(b: Int) {
            key0 = crc32Byte(key0, b)
            key1 = ((key1 + (key0 and 0xFF)) * 134775813 + 1)
            key2 = crc32Byte(key2, (key1 ushr 24) and 0xFF)
        }

        fun decrypt(cipherData: ByteArray): ByteArray {
            val out = ByteArray(cipherData.size)
            for (i in cipherData.indices) {
                val k = (key2 or 2) and 0xFFFF
                val magic = ((k * (k xor 1)) ushr 8) and 0xFF
                val plain = (cipherData[i].toInt() and 0xFF) xor magic
                updateKeys(plain)
                out[i] = plain.toByte()
            }
            return out
        }
    }
}

/**
 * Parser JSON minimaliste et robuste sans aucune dépendance.
 */
internal object MiniJson {
    fun parse(text: String): Any? {
        val tokenizer = JsonTokenizer(text)
        return parseValue(tokenizer)
    }

    private fun parseValue(t: JsonTokenizer): Any? {
        t.skipWhitespace()
        return when (val ch = t.peek()) {
            '{' -> parseObject(t)
            '[' -> parseArray(t)
            '"' -> parseString(t)
            't', 'f' -> parseBoolean(t)
            'n' -> parseNull(t)
            null -> null
            else -> if (ch == '-' || ch in '0'..'9') parseNumber(t) else error("Caractère JSON inattendu : $ch à la position ${t.pos}")
        }
    }

    private fun parseObject(t: JsonTokenizer): Map<String, Any?> {
        t.expect('{')
        val map = mutableMapOf<String, Any?>()
        t.skipWhitespace()
        if (t.peek() == '}') {
            t.next()
            return map
        }
        while (true) {
            t.skipWhitespace()
            val key = parseString(t)
            t.skipWhitespace()
            t.expect(':')
            val value = parseValue(t)
            map[key] = value
            t.skipWhitespace()
            when (t.peek()) {
                ',' -> {
                    t.next()
                    continue
                }
                '}' -> {
                    t.next()
                    break
                }
                else -> error("Attendu ',' ou '}' dans l'objet à la position ${t.pos}")
            }
        }
        return map
    }

    private fun parseArray(t: JsonTokenizer): List<Any?> {
        t.expect('[')
        val list = mutableListOf<Any?>()
        t.skipWhitespace()
        if (t.peek() == ']') {
            t.next()
            return list
        }
        while (true) {
            list += parseValue(t)
            t.skipWhitespace()
            when (t.peek()) {
                ',' -> {
                    t.next()
                    continue
                }
                ']' -> {
                    t.next()
                    break
                }
                else -> error("Attendu ',' ou ']' dans le tableau à la position ${t.pos}")
            }
        }
        return list
    }

    private fun parseString(t: JsonTokenizer): String {
        t.expect('"')
        val sb = StringBuilder()
        while (true) {
            val ch = t.next() ?: error("Chaîne non terminée")
            if (ch == '"') break
            if (ch == '\\') {
                when (val esc = t.next()) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000C')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        val hex = StringBuilder()
                        repeat(4) { hex.append(t.next() ?: error("Séquence unicode incomplète")) }
                        sb.append(hex.toString().toInt(16).toChar())
                    }
                    else -> sb.append(esc)
                }
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun parseNumber(t: JsonTokenizer): Number {
        val start = t.pos
        if (t.peek() == '-') t.next()
        while (t.peek() != null && t.peek() in '0'..'9') t.next()
        var isFloat = false
        if (t.peek() == '.') {
            isFloat = true
            t.next()
            while (t.peek() != null && t.peek() in '0'..'9') t.next()
        }
        if (t.peek() == 'e' || t.peek() == 'E') {
            isFloat = true
            t.next()
            if (t.peek() == '+' || t.peek() == '-') t.next()
            while (t.peek() != null && t.peek() in '0'..'9') t.next()
        }
        val s = t.text.substring(start, t.pos)
        return if (isFloat) s.toDouble() else s.toLong()
    }

    private fun parseBoolean(t: JsonTokenizer): Boolean {
        if (t.text.startsWith("true", t.pos)) {
            t.pos += 4
            return true
        }
        if (t.text.startsWith("false", t.pos)) {
            t.pos += 5
            return false
        }
        error("Booléen invalide à la position ${t.pos}")
    }

    private fun parseNull(t: JsonTokenizer): Any? {
        if (t.text.startsWith("null", t.pos)) {
            t.pos += 4
            return null
        }
        error("Null invalide à la position ${t.pos}")
    }

    private class JsonTokenizer(val text: String) {
        var pos: Int = 0

        fun skipWhitespace() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }

        fun peek(): Char? = if (pos < text.length) text[pos] else null

        fun next(): Char? = if (pos < text.length) text[pos++] else null

        fun expect(ch: Char) {
            val c = next()
            if (c != ch) error("Attendu '$ch', trouvé '$c' à la position ${pos - 1}")
        }
    }
}
