package fr.lunaison.core.data

actual suspend fun LunaisonRepository.importAuto(bytes: ByteArray, password: String?): ImportSummary =
    throw UnsupportedOperationException("L'import d'autres applis n'existe pas sur iOS")
