package fr.lunaison.core.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.Flow

/** Une journée notée. Les dates sont stockées au format ISO (2026-11-04), les listes séparées par des virgules. */
@Entity(tableName = "day_logs")
data class DayLogEntity(
    @PrimaryKey val date: String,
    val flow: String?,
    val pain: String?,
    val symptoms: String,
    val moods: String,
    val note: String,
    val medicationTaken: Boolean = false,
    val mucus: String? = null,
    val basalTemperature: Float? = null,
    val pillStatus: String? = null,
    val medications: String = "",
    val weight: Float? = null,
    val intimacy: String = "",
    val libido: String? = null,
)

/** Réglages : une seule ligne. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val onboarded: Boolean,
    val ageGroup: String,
    val lastPeriodStart: String?,
    val usualCycleLength: Int?,
    val usualPeriodLength: Int?,
    val periodReminder: Boolean,
    val periodReminderDaysBefore: Int,
    val logReminder: String,
    val logReminderTime: String,
    val partnerName: String?,
    val partnerPhone: String?,
    val partnerChannel: String?,
    val dismissedAlerts: String,
    /** Début du cycle dont le bilan a déjà été lu (format ISO), pour ne pas le remontrer. */
    val recapSeen: String? = null,
    val medicationReminder: Boolean = false,
    val medicationReminderTime: String = "09:00",
    val medicationLabel: String = "",
    val pausePredictions: Boolean = false,
    val showFertility: Boolean = false,
    val themePalette: String = "PLUM",
    val pinHash: String? = null,
    val followedPartnerSnapshot: String? = null,
    val myShareName: String = "",
    val trackOvulationSigns: Boolean = false,
    val irregularCyclesMode: Boolean = false,
    val reviewPromptDismissed: Boolean = false,
)

@Dao
interface LunaisonDao {
    @Query("SELECT * FROM day_logs ORDER BY date")
    fun logs(): Flow<List<DayLogEntity>>

    @Query("SELECT * FROM day_logs ORDER BY date")
    suspend fun logsOnce(): List<DayLogEntity>

    @Upsert
    suspend fun upsertLog(log: DayLogEntity)

    @Query("DELETE FROM day_logs WHERE date = :date")
    suspend fun deleteLog(date: String)

    @Query("SELECT * FROM settings WHERE id = 0")
    fun settings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 0")
    suspend fun settingsOnce(): SettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("DELETE FROM day_logs")
    suspend fun deleteLogs()

    @Query("DELETE FROM settings")
    suspend fun deleteSettings()
}

@Database(entities = [DayLogEntity::class, SettingsEntity::class], version = 7, exportSchema = true)
@ConstructedBy(LunaisonDatabaseConstructor::class)
abstract class LunaisonDatabase : RoomDatabase() {
    abstract fun dao(): LunaisonDao

    companion object {
        /**
         * Toutes les migrations, de la version 1 à la version 7, sous forme de requêtes SQL.
         * Chaque étape se contente d'ajouter des colonnes : aucune donnée existante n'est touchée.
         */
        internal val MIGRATIONS: List<SqlMigration> = listOf(
            // Version 2 : mémorise le bilan de cycle déjà lu.
            SqlMigration(
                1, 2,
                "ALTER TABLE settings ADD COLUMN recapSeen TEXT",
            ),
            // Version 3 : suivi de contraception/traitement, pause des prévisions, fenêtre fertile, thème et verrou PIN.
            SqlMigration(
                2, 3,
                "ALTER TABLE day_logs ADD COLUMN medicationTaken INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE settings ADD COLUMN medicationReminder INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE settings ADD COLUMN medicationReminderTime TEXT NOT NULL DEFAULT '09:00'",
                "ALTER TABLE settings ADD COLUMN medicationLabel TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE settings ADD COLUMN pausePredictions INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE settings ADD COLUMN showFertility INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE settings ADD COLUMN themePalette TEXT NOT NULL DEFAULT 'PLUM'",
                "ALTER TABLE settings ADD COLUMN pinHash TEXT",
            ),
            // Version 4 : partage décentralisé par QR Code / SMS avec un ou une partenaire.
            SqlMigration(
                3, 4,
                "ALTER TABLE settings ADD COLUMN followedPartnerSnapshot TEXT",
                "ALTER TABLE settings ADD COLUMN myShareName TEXT NOT NULL DEFAULT ''",
            ),
            // Version 5 : suivi symptothermique facultatif (glaire cervicale, température) et mode cycles irréguliers / périménopause.
            SqlMigration(
                4, 5,
                "ALTER TABLE day_logs ADD COLUMN mucus TEXT",
                "ALTER TABLE day_logs ADD COLUMN basalTemperature REAL",
                "ALTER TABLE settings ADD COLUMN trackOvulationSigns INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE settings ADD COLUMN irregularCyclesMode INTEGER NOT NULL DEFAULT 0",
            ),
            // Version 6 : mémorise si la demande d'avis discrète a été masquée ou consultée.
            SqlMigration(
                5, 6,
                "ALTER TABLE settings ADD COLUMN reviewPromptDismissed INTEGER NOT NULL DEFAULT 0",
            ),
            // Version 7 : suivi de pilule (statut précis), prise de médicaments, poids, activité sexuelle et libido.
            SqlMigration(
                6, 7,
                "ALTER TABLE day_logs ADD COLUMN pillStatus TEXT",
                "ALTER TABLE day_logs ADD COLUMN medications TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE day_logs ADD COLUMN weight REAL",
                "ALTER TABLE day_logs ADD COLUMN intimacy TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE day_logs ADD COLUMN libido TEXT",
            ),
        )
    }
}

/** Une migration faite uniquement de requêtes SQL, rejouable aussi sur la JVM par les tests. */
internal class SqlMigration(startVersion: Int, endVersion: Int, vararg val statements: String) :
    Migration(startVersion, endVersion) {
    override fun migrate(connection: SQLiteConnection) {
        statements.forEach { connection.execSQL(it) }
    }
}

/** Room génère l'implémentation de chaque plateforme. */
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object LunaisonDatabaseConstructor : RoomDatabaseConstructor<LunaisonDatabase> {
    override fun initialize(): LunaisonDatabase
}

/** Réglages communs aux plateformes : migrations, puis moteur propre à chacune. */
internal fun RoomDatabase.Builder<LunaisonDatabase>.withMigrations(): RoomDatabase.Builder<LunaisonDatabase> =
    addMigrations(*LunaisonDatabase.MIGRATIONS.toTypedArray())
