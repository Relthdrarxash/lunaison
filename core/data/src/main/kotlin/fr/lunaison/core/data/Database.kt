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

@Database(entities = [DayLogEntity::class, SettingsEntity::class], version = 2, exportSchema = true)
abstract class LunaisonDatabase : RoomDatabase() {
    abstract fun dao(): LunaisonDao

    companion object {
        fun create(context: Context): LunaisonDatabase =
            Room.databaseBuilder(context.applicationContext, LunaisonDatabase::class.java, "lunaison.db")
                .addMigrations(MIGRATION_1_2)
                .build()

        /** Version 2 : mémorise le bilan de cycle déjà lu. Une colonne ajoutée, aucune donnée touchée. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN recapSeen TEXT")
            }
        }
    }
}
