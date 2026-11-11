package fr.lunaison.core.model

/*
 * Briques d'encodage du partage hors ligne (QR Code, SMS), écrites en Kotlin commun pour que
 * Android et iOS produisent et acceptent exactement les mêmes codes `LUNA1:...`.
 */

/** Base64 « URL » sans remplissage (`-` et `_`), comme `java.util.Base64.getUrlEncoder().withoutPadding()`. */
internal object Base64Url {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    fun encode(bytes: ByteArray): String {
        val out = StringBuilder((bytes.size * 4 + 2) / 3)
        var i = 0
        while (i + 2 < bytes.size) {
            val n = ((bytes[i].toInt() and 0xFF) shl 16) or ((bytes[i + 1].toInt() and 0xFF) shl 8) or (bytes[i + 2].toInt() and 0xFF)
            out.append(ALPHABET[(n shr 18) and 63]).append(ALPHABET[(n shr 12) and 63])
                .append(ALPHABET[(n shr 6) and 63]).append(ALPHABET[n and 63])
            i += 3
        }
        when (bytes.size - i) {
            1 -> {
                val n = (bytes[i].toInt() and 0xFF) shl 16
                out.append(ALPHABET[(n shr 18) and 63]).append(ALPHABET[(n shr 12) and 63])
            }
            2 -> {
                val n = ((bytes[i].toInt() and 0xFF) shl 16) or ((bytes[i + 1].toInt() and 0xFF) shl 8)
                out.append(ALPHABET[(n shr 18) and 63]).append(ALPHABET[(n shr 12) and 63]).append(ALPHABET[(n shr 6) and 63])
            }
        }
        return out.toString()
    }

    /**
     * Décode un texte Base64 « URL » sans remplissage. Lève [IllegalArgumentException] pour un caractère
     * étranger à l'alphabet ou une longueur impossible (reste de 1 caractère), comme le décodeur de Java.
     */
    fun decode(text: String): ByteArray {
        require(text.length % 4 != 1) { "Longueur Base64 invalide" }
        val out = ByteArray(text.length * 3 / 4)
        var size = 0
        var buffer = 0
        var bits = 0
        for (c in text) {
            val v = ALPHABET.indexOf(c)
            require(v >= 0) { "Caractère Base64 invalide" }
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out[size++] = ((buffer shr bits) and 0xFF).toByte()
            }
        }
        return out.copyOf(size)
    }
}

/** CRC32 (polynôme IEEE 802.3), identique à `java.util.zip.CRC32`. */
internal object Crc32 {
    private val TABLE = LongArray(256) { n ->
        var c = n.toLong()
        repeat(8) { c = if (c and 1L != 0L) (c ushr 1) xor 0xEDB88320L else c ushr 1 }
        c
    }

    fun of(bytes: ByteArray): Long {
        var crc = 0xFFFFFFFFL
        for (b in bytes) {
            crc = TABLE[((crc xor b.toLong()) and 0xFF).toInt()] xor (crc ushr 8)
        }
        return crc xor 0xFFFFFFFFL
    }
}

/** Nombre de points de code Unicode (une paire de substitution compte pour un seul). */
internal fun String.codePointTotal(): Int {
    var count = 0
    var i = 0
    while (i < length) {
        i += if (this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) 2 else 1
        count++
    }
    return count
}

/** Les [count] premiers points de code (jamais une paire coupée en deux). */
internal fun String.takeCodePoints(count: Int): String {
    var i = 0
    var taken = 0
    while (i < length && taken < count) {
        i += if (this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) 2 else 1
        taken++
    }
    return substring(0, i)
}

/** Retire le dernier point de code (une paire de substitution entière, jamais la moitié). */
internal fun String.dropLastCodePoint(): String {
    if (isEmpty()) return this
    val cut = if (length >= 2 && this[length - 1].isLowSurrogate() && this[length - 2].isHighSurrogate()) 2 else 1
    return substring(0, length - cut)
}

/**
 * Catégories Unicode « format » (Cf) situées hors du plan de base : `Char.category` ne voit que
 * les unités UTF-16, donc on les liste (Unicode 15).
 */
private fun isSupplementaryFormat(cp: Int): Boolean = cp == 0x110BD || cp == 0x110CD ||
    cp in 0x13430..0x1343F || cp in 0x1BCA0..0x1BCA3 || cp in 0x1D173..0x1D17A ||
    cp == 0xE0001 || cp in 0xE0020..0xE007F

/**
 * Retire les caractères de contrôle, de format, de séparation de ligne ou de paragraphe et les
 * substituts isolés, et remplace `|` par une espace (le séparateur casserait le format du code).
 */
internal fun String.withoutInvisibleCharacters(): String {
    val kept = StringBuilder(length)
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
            val cp = 0x10000 + ((c.code - 0xD800) shl 10) + (this[i + 1].code - 0xDC00)
            if (!isSupplementaryFormat(cp)) kept.append(c).append(this[i + 1])
            i += 2
            continue
        }
        when (c.category) {
            CharCategory.CONTROL, CharCategory.FORMAT, CharCategory.SURROGATE,
            CharCategory.LINE_SEPARATOR, CharCategory.PARAGRAPH_SEPARATOR,
            -> Unit
            else -> kept.append(if (c == '|') ' ' else c)
        }
        i++
    }
    return kept.toString()
}
