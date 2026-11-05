package fr.lunaison.core.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
abstract class LunaisonDatabase : RoomDatabase() {
    abstract fun dao(): LunaisonDao

    companion object {
        fun create(context: Context): LunaisonDatabase =
            Room.databaseBuilder(context.applicationContext, LunaisonDatabase::class.java, "lunaison.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()

        /** Version 2 : mémorise le bilan de cycle déjà lu. Une colonne ajoutée, aucune donnée touchée. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN recapSeen TEXT")
            }
        }

        /** Version 3 : suivi de contraception/traitement, pause des prévisions, fenêtre fertile, thème et verrou PIN. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE day_logs ADD COLUMN medicationTaken INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN medicationReminder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN medicationReminderTime TEXT NOT NULL DEFAULT '09:00'")
                db.execSQL("ALTER TABLE settings ADD COLUMN medicationLabel TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE settings ADD COLUMN pausePredictions INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN showFertility INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN themePalette TEXT NOT NULL DEFAULT 'PLUM'")
                db.execSQL("ALTER TABLE settings ADD COLUMN pinHash TEXT")
            }
        }

        /** Version 4 : partage décentralisé par QR Code / SMS avec un ou une partenaire. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN followedPartnerSnapshot TEXT")
                db.execSQL("ALTER TABLE settings ADD COLUMN myShareName TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Version 5 : suivi symptothermique facultatif (glaire cervicale, température) et mode cycles irréguliers / périménopause. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE day_logs ADD COLUMN mucus TEXT")
                db.execSQL("ALTER TABLE day_logs ADD COLUMN basalTemperature REAL")
                db.execSQL("ALTER TABLE settings ADD COLUMN trackOvulationSigns INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN irregularCyclesMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Version 6 : mémorise si la demande d'avis discrète a été masquée ou consultée. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN reviewPromptDismissed INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Version 7 : suivi de pilule (statut précis), prise de médicaments, poids, activité sexuelle et libido. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE day_logs ADD COLUMN pillStatus TEXT")
                db.execSQL("ALTER TABLE day_logs ADD COLUMN medications TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE day_logs ADD COLUMN weight REAL")
                db.execSQL("ALTER TABLE day_logs ADD COLUMN intimacy TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE day_logs ADD COLUMN libido TEXT")
            }
        }
    }
}
