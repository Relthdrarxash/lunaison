package fr.lunaison.ui

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.nio.charset.StandardCharsets

/**
 * Service d'émulation de carte NFC (Host Card Emulation).
 * Permet à deux téléphones d'échanger les estimations du cycle Lunaison
 * en se touchant dos à dos, sans connexion Internet ni serveur.
 */
class LunaisonHceService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return STATUS_FAILED

        if (isSelectAidApdu(commandApdu)) {
            val payload = currentPayload
            if (payload != null) {
                val payloadBytes = payload.toByteArray(StandardCharsets.UTF_8)
                val response = ByteArray(payloadBytes.size + 2)
                System.arraycopy(payloadBytes, 0, response, 0, payloadBytes.size)
                response[payloadBytes.size] = 0x90.toByte()
                response[payloadBytes.size + 1] = 0x00.toByte()
                return response
            }
        }
        return STATUS_FAILED
    }

    override fun onDeactivated(reason: Int) {
        // Fin de la session NFC (perte de champ ou liaison terminée)
    }

    companion object {
        /**
         * Identifiant d'application propriétaire ISO/IEC 7816-4 :
         * F0 (catégorie propriétaire) + 'L','U','N','A','1' (0x4C, 0x55, 0x4E, 0x41, 0x31) + 0x01
         */
        val AID: ByteArray = byteArrayOf(
            0xF0.toByte(),
            0x4C.toByte(),
            0x55.toByte(),
            0x4E.toByte(),
            0x41.toByte(),
            0x31.toByte(),
            0x01.toByte(),
        )

        const val AID_HEX: String = "F04C554E413101"

        private val SELECT_APDU_HEADER: ByteArray = byteArrayOf(
            0x00.toByte(), // CLA
            0xA4.toByte(), // INS : SELECT FILE
            0x04.toByte(), // P1 : sélection par AID
            0x00.toByte(), // P2 : première occurrence
            0x07.toByte(), // Lc : 7 octets d'AID
        )

        private val STATUS_FAILED: ByteArray = byteArrayOf(0x6F.toByte(), 0x00.toByte())

        @Volatile
        var currentPayload: String? = null

        fun isSelectAidApdu(apdu: ByteArray): Boolean {
            if (apdu.size < SELECT_APDU_HEADER.size + AID.size) return false
            for (i in SELECT_APDU_HEADER.indices) {
                if (apdu[i] != SELECT_APDU_HEADER[i]) return false
            }
            for (i in AID.indices) {
                if (apdu[SELECT_APDU_HEADER.size + i] != AID[i]) return false
            }
            return true
        }

        fun buildSelectAidApdu(): ByteArray {
            val apdu = ByteArray(SELECT_APDU_HEADER.size + AID.size + 1)
            System.arraycopy(SELECT_APDU_HEADER, 0, apdu, 0, SELECT_APDU_HEADER.size)
            System.arraycopy(AID, 0, apdu, SELECT_APDU_HEADER.size, AID.size)
            apdu[apdu.size - 1] = 0x00.toByte() // Le : attend jusqu'à 256 octets
            return apdu
        }
    }
}
