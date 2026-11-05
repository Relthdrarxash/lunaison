package fr.lunaison.ui

import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LunaisonHceServiceTest {

    @Test
    fun `buildSelectAidApdu generates valid ISO 7816-4 SELECT command`() {
        val apdu = LunaisonHceService.buildSelectAidApdu()
        // Header: 00 A4 04 00 07
        assertEquals(0x00.toByte(), apdu[0])
        assertEquals(0xA4.toByte(), apdu[1])
        assertEquals(0x04.toByte(), apdu[2])
        assertEquals(0x00.toByte(), apdu[3])
        assertEquals(0x07.toByte(), apdu[4])

        // AID: F0 4C 55 4E 41 31 01
        val expectedAid = byteArrayOf(
            0xF0.toByte(),
            'L'.code.toByte(),
            'U'.code.toByte(),
            'N'.code.toByte(),
            'A'.code.toByte(),
            '1'.code.toByte(),
            0x01.toByte(),
        )
        for (i in expectedAid.indices) {
            assertEquals(expectedAid[i], apdu[5 + i])
        }

        // Le: 00
        assertEquals(0x00.toByte(), apdu[apdu.size - 1])
        assertTrue(LunaisonHceService.isSelectAidApdu(apdu))
    }

    @Test
    fun `isSelectAidApdu rejects invalid commands`() {
        assertFalse(LunaisonHceService.isSelectAidApdu(byteArrayOf()))
        assertFalse(LunaisonHceService.isSelectAidApdu(byteArrayOf(0x00, 0xA4.toByte(), 0x00, 0x00)))
        // Wrong AID
        val wrongAid = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
        assertFalse(LunaisonHceService.isSelectAidApdu(wrongAid))
    }

    @Test
    fun `service returns payload with SW 9000 when active`() {
        val service = LunaisonHceService()
        val testPayload = "LUNA1:dGVzdA"
        LunaisonHceService.currentPayload = testPayload

        try {
            val command = LunaisonHceService.buildSelectAidApdu()
            val response = service.processCommandApdu(command, null)

            // Response must end with 90 00
            assertTrue(response.size >= 2)
            assertEquals(0x90.toByte(), response[response.size - 2])
            assertEquals(0x00.toByte(), response[response.size - 1])

            // Response body must match payload
            val body = response.copyOfRange(0, response.size - 2)
            assertEquals(testPayload, String(body, StandardCharsets.UTF_8))
        } finally {
            LunaisonHceService.currentPayload = null
        }
    }

    @Test
    fun `service returns failure when payload is null`() {
        val service = LunaisonHceService()
        LunaisonHceService.currentPayload = null

        val command = LunaisonHceService.buildSelectAidApdu()
        val response = service.processCommandApdu(command, null)

        assertEquals(2, response.size)
        assertEquals(0x6F.toByte(), response[0])
        assertEquals(0x00.toByte(), response[1])
    }
}
