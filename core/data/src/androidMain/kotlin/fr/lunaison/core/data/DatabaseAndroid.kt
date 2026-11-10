package fr.lunaison.core.data

import android.content.Context
import androidx.room.Room

/**
 * Base Android : même fichier (`lunaison.db`) et même moteur SQLite du système qu'avant le passage
 * en multiplateforme, donc les données des utilisateurs actuels restent lisibles telles quelles.
 */
fun LunaisonDatabase.Companion.create(context: Context): LunaisonDatabase {
    val appContext = context.applicationContext
    return Room.databaseBuilder<LunaisonDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("lunaison.db").absolutePath,
    ).withMigrations().build()
}

fun LunaisonRepository.Companion.create(context: Context): LunaisonRepository =
    LunaisonRepository(LunaisonDatabase.create(context).dao())
