package fr.lunaison.core.data

data class ImportSummary(val daysCount: Int, val cyclesCount: Int)

/**
 * Importe un fichier d'une autre appli de suivi (archive Zip, JSON ou CSV). Android seulement :
 * l'appli d'origine (Period Calendar) n'existe pas sur iOS, où l'appel échoue.
 */
expect suspend fun LunaisonRepository.importAuto(bytes: ByteArray, password: String? = null): ImportSummary
