package fr.lunaison.core.data

import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Rejoue toutes les migrations (v1 -> v5) sur un vrai moteur SQLite, à partir d'une base v1 remplie.
 *
 * Deux garanties :
 * - aucune journée ni aucun réglage existant n'est perdu ou modifié ;
 * - la base migrée est identique (colonnes, types, NOT NULL, clé primaire) à une base neuve créée
 *   par Room à partir du schéma exporté `schemas/.../5.json`. Si quelqu'un ajoute une colonne à une
 *   entité sans écrire la migration correspondante, ce test échoue.
 */
class DatabaseMigrationTest {

    private val latestSchema = File("schemas/fr.lunaison.core.data.LunaisonDatabase/6.json")

    @Test
    fun `migration de v1 a v6 sans perte de donnees`() = withDatabase { conn, db ->
        createV1(db)
        db.execSQL(
            "INSERT INTO day_logs (date, flow, pain, symptoms, moods, note) " +
                "VALUES ('2026-10-01', 'MEDIUM', 'MODERATE', 'FATIGUE,BLOATING', 'CALM', 'Premier jour')",
        )
        db.execSQL(
            "INSERT INTO settings (id, onboarded, ageGroup, lastPeriodStart, usualCycleLength, usualPeriodLength, " +
                "periodReminder, periodReminderDaysBefore, logReminder, logReminderTime, partnerName, partnerPhone, " +
                "partnerChannel, dismissedAlerts) VALUES (0, 1, 'ADULT', '2026-10-01', 29, 5, 1, 2, 'EVENING', " +
                "'20:30', 'Alex', NULL, 'SIGNAL', '')",
        )

        migrateToLatest(db)

        conn.query("SELECT * FROM day_logs WHERE date = '2026-10-01'") { rs ->
            assertTrue(rs.next())
            assertEquals("MEDIUM", rs.getString("flow"))
            assertEquals("MODERATE", rs.getString("pain"))
            assertEquals("FATIGUE,BLOATING", rs.getString("symptoms"))
            assertEquals("Premier jour", rs.getString("note"))
            assertEquals(0, rs.getInt("medicationTaken"))
            assertNull(rs.getString("mucus"))
        }
        conn.query("SELECT * FROM settings WHERE id = 0") { rs ->
            assertTrue(rs.next())
            assertEquals(1, rs.getInt("onboarded"))
            assertEquals(29, rs.getInt("usualCycleLength"))
            assertEquals("Alex", rs.getString("partnerName"))
            assertEquals("09:00", rs.getString("medicationReminderTime"))
            assertEquals("PLUM", rs.getString("themePalette"))
            assertEquals("", rs.getString("myShareName"))
            assertEquals(0, rs.getInt("irregularCyclesMode"))
            assertEquals(0, rs.getInt("reviewPromptDismissed"))
        }
    }

    @Test
    fun `la base migree correspond exactement au schema Room v6`() {
        val expected = withDatabase { conn, db ->
            createTablesSql(latestSchema).values.forEach(db::execSQL)
            tables(conn)
        }
        val migrated = withDatabase { conn, db ->
            createV1(db)
            migrateToLatest(db)
            tables(conn)
        }
        assertEquals(expected, migrated)
    }

    private fun migrateToLatest(db: SupportSQLiteDatabase) {
        listOf(
            LunaisonDatabase.MIGRATION_1_2,
            LunaisonDatabase.MIGRATION_2_3,
            LunaisonDatabase.MIGRATION_3_4,
            LunaisonDatabase.MIGRATION_4_5,
            LunaisonDatabase.MIGRATION_5_6,
        ).forEach { it.migrate(db) }
    }

    /** Schéma de la toute première version publiée. */
    private fun createV1(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `day_logs` (`date` TEXT NOT NULL, `flow` TEXT, `pain` TEXT, " +
                "`symptoms` TEXT NOT NULL, `moods` TEXT NOT NULL, `note` TEXT NOT NULL, PRIMARY KEY(`date`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `settings` (`id` INTEGER NOT NULL, `onboarded` INTEGER NOT NULL, " +
                "`ageGroup` TEXT NOT NULL, `lastPeriodStart` TEXT, `usualCycleLength` INTEGER, " +
                "`usualPeriodLength` INTEGER, `periodReminder` INTEGER NOT NULL, " +
                "`periodReminderDaysBefore` INTEGER NOT NULL, `logReminder` TEXT NOT NULL, " +
                "`logReminderTime` TEXT NOT NULL, `partnerName` TEXT, `partnerPhone` TEXT, " +
                "`partnerChannel` TEXT, `dismissedAlerts` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}

// Outils partagés, sans Robolectric : SQLite JDBC derrière l'interface SupportSQLiteDatabase de Room.

internal fun <T> withDatabase(block: (Connection, SupportSQLiteDatabase) -> T): T =
    DriverManager.getConnection("jdbc:sqlite::memory:").use { conn -> block(conn, conn.asSupportDb()) }

private fun Connection.asSupportDb(): SupportSQLiteDatabase =
    Proxy.newProxyInstance(
        SupportSQLiteDatabase::class.java.classLoader,
        arrayOf(SupportSQLiteDatabase::class.java),
    ) { _, method, args ->
        when (method.name) {
            "execSQL" -> createStatement().use { it.execute(args[0] as String) }.let { null }
            else -> throw UnsupportedOperationException("Non simulé dans le test : ${method.name}")
        }
    } as SupportSQLiteDatabase

internal fun <T> Connection.query(sql: String, block: (java.sql.ResultSet) -> T): T =
    createStatement().use { stmt -> stmt.executeQuery(sql).use(block) }

/** Pour chaque table : (nom, type, NOT NULL, rang dans la clé primaire) de chaque colonne, triés par nom. */
internal fun tables(conn: Connection): Map<String, List<String>> {
    val names = conn.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name") { rs ->
        buildList { while (rs.next()) add(rs.getString(1)) }
    }
    return names.associateWith { table ->
        conn.query("PRAGMA table_info(`$table`)") { rs ->
            buildList { while (rs.next()) add("${rs.getString("name")} ${rs.getString("type")} notnull=${rs.getInt("notnull")} pk=${rs.getInt("pk")}") }
        }.sorted()
    }
}

/** Les requêtes CREATE TABLE d'un schéma exporté par Room, par nom de table. */
internal fun createTablesSql(schema: File): Map<String, String> {
    assertTrue(schema.exists(), "Schéma Room absent : ${schema.path}. Lancer une compilation pour l'exporter.")
    val regex = Regex("\"tableName\":\\s*\"([^\"]+)\",\\s*\"createSql\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    return regex.findAll(schema.readText()).associate { match ->
        val table = match.groupValues[1]
        table to match.groupValues[2].replace("\\\"", "\"").replace("\${TABLE_NAME}", table)
    }
}
