package com.example

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DatabaseMigrationManager
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationManagerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Pulisce file di test
        val testDbFile = context.getDatabasePath("test_migration_db")
        if (testDbFile.exists()) {
            testDbFile.delete()
        }
        val backupsDir = File(context.filesDir, "backups")
        if (backupsDir.exists()) {
            backupsDir.deleteRecursively()
        }
        DatabaseMigrationManager.clearLastMigrationSummary()
    }

    @Test
    fun testInspectDatabaseNonExistent() {
        val result = DatabaseMigrationManager.inspectDatabase(
            context = context,
            dbName = "non_existent_db",
            targetVersion = 3
        )
        assertFalse(result.dbExists)
        assertFalse(result.needsMigration)
        assertEquals(0, result.existingVersion)
    }

    @Test
    fun testInspectDatabaseDifferentVersionTriggersMigration() {
        val dbName = "test_migration_db"
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()

        // Crea un database SQLite simulato con versione 1
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.version = 1
        db.execSQL("CREATE TABLE meal_plans (id TEXT PRIMARY KEY, title TEXT, isActive INTEGER, createdAt INTEGER);")
        db.execSQL("INSERT INTO meal_plans VALUES ('plan_1', 'Piano V1', 1, 123456789);")
        db.close()

        val result = DatabaseMigrationManager.inspectDatabase(
            context = context,
            dbName = dbName,
            targetVersion = 3
        )

        assertTrue(result.dbExists)
        assertTrue(result.needsMigration)
        assertEquals(1, result.existingVersion)
        assertEquals(3, result.targetVersion)
    }

    @Test
    fun testCreateBackupJsonAndRestoreWithNewFields() {
        val dbName = "test_migration_db"
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()

        // 1. Crea database versione 1 con dati
        val oldDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        oldDb.version = 1
        oldDb.execSQL("CREATE TABLE sample_items (id TEXT PRIMARY KEY, name TEXT);")
        oldDb.execSQL("INSERT INTO sample_items VALUES ('item_1', 'Pasta al Pomodoro');")
        oldDb.execSQL("INSERT INTO sample_items VALUES ('item_2', 'Petto di Pollo');")
        oldDb.execSQL("INSERT INTO sample_items VALUES ('item_3', 'Insalata Mista');")
        oldDb.close()

        // 2. Crea il backup JSON
        val backupFile = DatabaseMigrationManager.createBackupJson(context, dbName)
        assertNotNull(backupFile)
        assertTrue(backupFile!!.exists())

        // Verifica contenuto backup
        val json = JSONObject(backupFile.readText())
        assertEquals(1, json.getInt("sourceVersion"))
        val tables = json.getJSONObject("tables")
        val itemsArray = tables.getJSONArray("sample_items")
        assertEquals(3, itemsArray.length())

        // 3. Simula aggiornamento dello schema alla versione 2/3 con NUOVI CAMPI:
        // - 'notes' (nuovo campo testuale -> deve essere vuoto "")
        // - 'sortOrder' (nuovo campo d'ordine -> deve ricevere progressivo 1, 2, 3...)
        dbFile.delete() // reset per ricreare con nuovo schema

        val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE sample_items (" +
                                "id TEXT PRIMARY KEY, " +
                                "name TEXT, " +
                                "notes TEXT, " +
                                "sortOrder INTEGER" +
                                ");"
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val newDb = helper.writableDatabase

        // 4. Ripristina i dati dal backup nel nuovo schema
        val summary = DatabaseMigrationManager.restoreDataFromBackup(newDb, backupFile)
        assertNotNull(summary)
        assertEquals(1, summary!!.restoredTablesCount)
        assertEquals(3, summary.restoredRowsCount)

        // 5. Verifica i record ripristinati e i valori assegnati ai nuovi campi
        val cursor = newDb.query("SELECT id, name, notes, sortOrder FROM sample_items ORDER BY sortOrder ASC")
        var rowCount = 0
        while (cursor.moveToNext()) {
            rowCount++
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val notes = cursor.getString(2)
            val sortOrder = cursor.getInt(3)

            // Il nome originale deve essere preservato
            when (id) {
                "item_1" -> assertEquals("Pasta al Pomodoro", name)
                "item_2" -> assertEquals("Petto di Pollo", name)
                "item_3" -> assertEquals("Insalata Mista", name)
            }

            // Il nuovo campo testuale 'notes' deve essere vuoto come da specifica utente
            assertEquals("", notes)

            // Il nuovo campo d'ordine 'sortOrder' deve avere il progressivo in base alla posizione (1, 2, 3)
            assertEquals(rowCount, sortOrder)
        }
        cursor.close()
        assertEquals(3, rowCount)
        newDb.close()
    }

    @Test
    fun testIsOrderOrSequenceColumnDetection() {
        assertTrue(DatabaseMigrationManager.isOrderOrSequenceColumn("sortOrder"))
        assertTrue(DatabaseMigrationManager.isOrderOrSequenceColumn("position"))
        assertTrue(DatabaseMigrationManager.isOrderOrSequenceColumn("itemIndex"))
        assertTrue(DatabaseMigrationManager.isOrderOrSequenceColumn("sequence_no"))
        assertTrue(DatabaseMigrationManager.isOrderOrSequenceColumn("rank"))

        assertFalse(DatabaseMigrationManager.isOrderOrSequenceColumn("name"))
        assertFalse(DatabaseMigrationManager.isOrderOrSequenceColumn("calories"))
        assertFalse(DatabaseMigrationManager.isOrderOrSequenceColumn("brand"))
    }
}
