package fr.lunaison.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformNeutralTest {

    private fun hex(bytes: ByteArray) = bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    @Test
    fun `sha256 donne les vecteurs de reference`() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hex(Sha256.digest(ByteArray(0))))
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hex(Sha256.digest("abc".encodeToByteArray())))
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            hex(Sha256.digest("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray())),
        )
    }

    @Test
    fun `l'empreinte du code PIN reste celle de la version Android d'origine`() {
        // SHA-256 de « 1234 », telle que l'ancienne version l'enregistrait.
        assertEquals(
            "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4",
            AppSettings.hashPin("1234"),
        )
    }

    @Test
    fun `une decimale avec un point`() {
        assertEquals("36.5", 36.45f.toOneDecimal())
        assertEquals("61.5", 61.5f.toOneDecimal())
        assertEquals("62.0", 62f.toOneDecimal())
        assertEquals("0.1", 0.05f.toOneDecimal())
    }
}
