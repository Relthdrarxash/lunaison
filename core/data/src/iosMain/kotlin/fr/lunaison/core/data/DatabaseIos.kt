package fr.lunaison.core.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Base iOS, dans le dossier « Application Support » de l'appli (jamais dans « Documents », visible dans Fichiers).
 * À faire avant toute publication : l'exclure des sauvegardes iCloud, comme la sauvegarde Android est désactivée.
 */
@OptIn(ExperimentalForeignApi::class)
private fun databasePath(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(directory?.path) { "Dossier de données introuvable" } + "/lunaison.db"
}

fun LunaisonDatabase.Companion.create(): LunaisonDatabase =
    Room.databaseBuilder<LunaisonDatabase>(name = databasePath())
        .withMigrations()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

fun LunaisonRepository.Companion.create(): LunaisonRepository =
    LunaisonRepository(LunaisonDatabase.create().dao())
