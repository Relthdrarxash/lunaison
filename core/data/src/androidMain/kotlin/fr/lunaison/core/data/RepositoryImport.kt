package fr.lunaison.core.data

/*
 * Import des sauvegardes d'autres applis (archive Zip, JSON, CSV). Il s'appuie sur le décompresseur de la JVM :
 * il reste donc propre à Android, l'application d'origine (Period Calendar) n'existant pas sur iOS.
 */

/**
 * Importe des données depuis une archive Zip (Period Calendar / Calendrier) avec mot de passe optionnel.
 */
suspend fun LunaisonRepository.importPeriodCalendarZip(bytes: ByteArray, password: String? = null): PeriodCalendarImportResult {
    val result = PeriodCalendarImporter.importFromZip(bytes, password)
    for (log in result.dayLogs) {
        save(log)
    }
    if (result.baseline != null) {
        updateSettings { current ->
            current.copy(
                baseline = current.baseline.copy(
                    lastPeriodStart = current.baseline.lastPeriodStart ?: result.baseline.lastPeriodStart,
                    usualCycleLength = current.baseline.usualCycleLength ?: result.baseline.usualCycleLength,
                    usualPeriodLength = current.baseline.usualPeriodLength ?: result.baseline.usualPeriodLength,
                ),
            )
        }
    }
    return result
}

/**
 * Importe des données depuis du JSON brut (Period Calendar / Calendrier).
 */
suspend fun LunaisonRepository.importPeriodCalendarJson(jsonContent: String): PeriodCalendarImportResult {
    val result = PeriodCalendarImporter.importFromTexts(dayEntriesJson = jsonContent)
    for (log in result.dayLogs) {
        save(log)
    }
    return result
}

/**
 * Tente d'importer automatiquement n'importe quel fichier (Zip Calendrier, JSON ou CSV).
 */
actual suspend fun LunaisonRepository.importAuto(bytes: ByteArray, password: String?): ImportSummary {
    if (PeriodCalendarImporter.isZip(bytes)) {
        val result = importPeriodCalendarZip(bytes, password)
        return ImportSummary(daysCount = result.dayLogs.size, cyclesCount = result.cyclesCount)
    }
    val text = String(bytes, Charsets.UTF_8).trim()
    if (text.startsWith("{") || text.startsWith("[")) {
        val result = importPeriodCalendarJson(text)
        return ImportSummary(daysCount = result.dayLogs.size, cyclesCount = result.cyclesCount)
    }
    val count = importCsv(text)
    return ImportSummary(daysCount = count, cyclesCount = 0)
}
