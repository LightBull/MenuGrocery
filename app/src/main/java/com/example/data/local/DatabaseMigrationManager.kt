package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestore automatico per la verifica, il backup e il ripristino del database SQLite/Room.
 * Controlla se sul dispositivo è già presente un database e, in caso di versione differente:
 * 1. Crea un backup integrale JSON con timestamp nella memoria interna protetta
 * 2. Permette a Room di aggiornare la struttura (schema)
 * 3. Ripristina i dati dal backup nel nuovo schema
 * 4. Valorizza i nuovi campi mancanti con stringhe vuote per i testi e progressivi
 *    (1, 2, 3...) per i campi di ordine o sequenza.
 */
object DatabaseMigrationManager {

    private const val TAG = "DbMigrationManager"
    const val DB_NAME = "mealplan_database"
    const val TARGET_VERSION = 3

    private const val PENDING_RESTORE_FILE_NAME = "pending_migration_restore.json"

    data class MigrationInspectionResult(
        val dbExists: Boolean,
        val existingVersion: Int,
        val targetVersion: Int,
        val needsMigration: Boolean
    )

    data class MigrationSummary(
        val sourceVersion: Int,
        val targetVersion: Int,
        val backupFilePath: String,
        val restoredTablesCount: Int,
        val restoredRowsCount: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var _lastMigrationSummary: MigrationSummary? = null
    val lastMigrationSummary: MigrationSummary?
        get() = _lastMigrationSummary

    fun clearLastMigrationSummary() {
        _lastMigrationSummary = null
    }

    /**
     * Verifica la presenza e la versione del database SQLite sul dispositivo.
     */
    fun inspectDatabase(
        context: Context,
        dbName: String = DB_NAME,
        targetVersion: Int = TARGET_VERSION
    ): MigrationInspectionResult {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            return MigrationInspectionResult(
                dbExists = false,
                existingVersion = 0,
                targetVersion = targetVersion,
                needsMigration = false
            )
        }

        var existingVersion = 0
        try {
            val sqliteDb = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            existingVersion = sqliteDb.version
            sqliteDb.close()
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante la lettura della versione del database SQLite: ${e.message}", e)
        }

        val needsMigration = existingVersion > 0 && existingVersion != targetVersion
        return MigrationInspectionResult(
            dbExists = true,
            existingVersion = existingVersion,
            targetVersion = targetVersion,
            needsMigration = needsMigration
        )
    }

    /**
     * Esegue il backup di tutte le tabelle del database SQLite in un file JSON con timestamp.
     */
    fun createBackupJson(
        context: Context,
        dbName: String = DB_NAME
    ): File? {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return null

        val backupDir = File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateFormatted = sdf.format(Date(timestamp))

        var sqliteDb: SQLiteDatabase? = null
        try {
            sqliteDb = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            val oldVersion = sqliteDb.version

            val rootJson = JSONObject().apply {
                put("sourceVersion", oldVersion)
                put("targetVersion", TARGET_VERSION)
                put("timestamp", timestamp)
                put("date", dateFormatted)
            }

            val tablesJson = JSONObject()

            // Estrae tutte le tabelle utente escludendo quelle di sistema
            val tablesCursor = sqliteDb.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' " +
                        "AND name NOT LIKE 'sqlite_%' " +
                        "AND name NOT LIKE 'room_master_table' " +
                        "AND name NOT LIKE 'android_metadata'",
                null
            )
            val tableNames = mutableListOf<String>()
            while (tablesCursor.moveToNext()) {
                tableNames.add(tablesCursor.getString(0))
            }
            tablesCursor.close()

            for (tableName in tableNames) {
                val rowsArray = JSONArray()
                val cursor = sqliteDb.rawQuery("SELECT * FROM `$tableName`", null)
                val colNames = cursor.columnNames
                while (cursor.moveToNext()) {
                    val rowObj = JSONObject()
                    for (i in colNames.indices) {
                        val col = colNames[i]
                        when (cursor.getType(i)) {
                            Cursor.FIELD_TYPE_NULL -> rowObj.put(col, JSONObject.NULL)
                            Cursor.FIELD_TYPE_INTEGER -> rowObj.put(col, cursor.getLong(i))
                            Cursor.FIELD_TYPE_FLOAT -> rowObj.put(col, cursor.getDouble(i))
                            Cursor.FIELD_TYPE_STRING -> rowObj.put(col, cursor.getString(i))
                            Cursor.FIELD_TYPE_BLOB -> {
                                val blob = cursor.getBlob(i)
                                rowObj.put(col, Base64.encodeToString(blob, Base64.NO_WRAP))
                            }
                        }
                    }
                    rowsArray.put(rowObj)
                }
                cursor.close()
                tablesJson.put(tableName, rowsArray)
            }

            rootJson.put("tables", tablesJson)

            val backupFile = File(backupDir, "mealplan_backup_v${oldVersion}_$dateFormatted.json")
            backupFile.writeText(rootJson.toString(2))
            Log.i(TAG, "Backup salvato con successo in: ${backupFile.absolutePath}")
            return backupFile
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante la creazione del backup JSON: ${e.message}", e)
            return null
        } finally {
            sqliteDb?.close()
        }
    }

    /**
     * Prepara il backup preventivo se viene rilevata una versione diversa.
     * Salva anche una copia puntatore `pending_migration_restore.json` per guidare il ripristino
     * post-aggiornamento dello schema.
     */
    fun prepareMigrationIfNeeded(
        context: Context,
        dbName: String = DB_NAME,
        targetVersion: Int = TARGET_VERSION
    ): File? {
        val inspection = inspectDatabase(context, dbName, targetVersion)
        if (!inspection.needsMigration) {
            return null
        }

        Log.w(TAG, "Rilevata versione differente (v${inspection.existingVersion} vs attesa v$targetVersion). Avvio procedura di backup preventivo...")
        val backupFile = createBackupJson(context, dbName) ?: return null

        try {
            val pendingFile = File(File(context.filesDir, "backups"), PENDING_RESTORE_FILE_NAME)
            backupFile.copyTo(pendingFile, overwrite = true)
        } catch (e: Exception) {
            Log.e(TAG, "Impossibile creare il file di ripristino pendente: ${e.message}", e)
        }

        return backupFile
    }

    /**
     * Ripristina i dati da un file JSON di backup nella nuova struttura del database SQLite/Room.
     * Gestisce i campi mancanti:
     * - progressivi per campi d'ordine (1, 2, 3...)
     * - stringhe vuote "" per campi testuali
     * - valori default o 0 per campi numerici
     */
    fun restoreDataFromBackup(
        db: SupportSQLiteDatabase,
        backupFile: File
    ): MigrationSummary? {
        if (!backupFile.exists()) return null

        try {
            val jsonContent = backupFile.readText()
            val rootJson = JSONObject(jsonContent)
            val sourceVersion = rootJson.optInt("sourceVersion", 0)
            val targetVersion = rootJson.optInt("targetVersion", TARGET_VERSION)
            val tablesJson = rootJson.optJSONObject("tables") ?: return null

            var restoredTablesCount = 0
            var restoredRowsCount = 0

            // Disattiva temporaneamente i vincoli di foreign key per inserire in sicurezza
            db.execSQL("PRAGMA foreign_keys = OFF;")

            // Ordine preferenziale per le tabelle con chiavi esterne
            val preferredOrder = listOf(
                "meal_plans",
                "meals",
                "meal_food_items",
                "shopping_list_items",
                "app_settings"
            )

            val allTableKeys = mutableListOf<String>()
            val keysIterator = tablesJson.keys()
            while (keysIterator.hasNext()) {
                allTableKeys.add(keysIterator.next())
            }

            // Ordina prima le tabelle note, poi eventuali tabelle extra
            val sortedTables = preferredOrder.filter { allTableKeys.contains(it) } +
                    allTableKeys.filterNot { preferredOrder.contains(it) }

            for (tableName in sortedTables) {
                val rowsArray = tablesJson.optJSONArray(tableName) ?: continue
                if (rowsArray.length() == 0) continue

                // Legge i metadati delle colonne dello schema attuale
                val currentColumns = getTableColumnInfo(db, tableName)
                if (currentColumns.isEmpty()) {
                    Log.w(TAG, "Tabella '$tableName' non trovata nello schema corrente, skip.")
                    continue
                }

                restoredTablesCount++

                for (rowIndex in 0 until rowsArray.length()) {
                    val rowObj = rowsArray.getJSONObject(rowIndex)
                    val contentValues = ContentValues()

                    for (col in currentColumns) {
                        val colName = col.name
                        val colType = col.type.uppercase(Locale.ROOT)

                        if (rowObj.has(colName) && !rowObj.isNull(colName)) {
                            // Il campo era già presente nel backup: ripristina il valore originale
                            when {
                                colType.contains("INT") -> contentValues.put(colName, rowObj.optLong(colName))
                                colType.contains("REAL") || colType.contains("FLOA") || colType.contains("DOUB") ->
                                    contentValues.put(colName, rowObj.optDouble(colName))
                                colType.contains("BLOB") -> {
                                    val b64 = rowObj.optString(colName, "")
                                    if (b64.isNotEmpty()) {
                                        contentValues.put(colName, Base64.decode(b64, Base64.NO_WRAP))
                                    } else {
                                        contentValues.putNull(colName)
                                    }
                                }
                                else -> contentValues.put(colName, rowObj.optString(colName))
                            }
                        } else {
                            // NUOVO CAMPO non presente nel backup!
                            if (isOrderOrSequenceColumn(colName)) {
                                // Assegna progressivo basato sulla posizione nel backup (1, 2, 3...)
                                val incrementalOrder = (rowIndex + 1).toLong()
                                if (colType.contains("REAL") || colType.contains("FLOA") || colType.contains("DOUB")) {
                                    contentValues.put(colName, incrementalOrder.toDouble())
                                } else {
                                    contentValues.put(colName, incrementalOrder)
                                }
                                Log.d(TAG, "Nuovo campo d'ordine '$colName' valorizzato con progressivo $incrementalOrder per riga $rowIndex in '$tableName'")
                            } else if (colType.contains("TEXT") || colType.contains("CHAR") || colType.contains("CLOB")) {
                                // Campi testuali lasciati di default vuoti come richiesto
                                contentValues.put(colName, "")
                            } else if (colType.contains("INT")) {
                                val dflt = col.defaultValue?.toLongOrNull() ?: 0L
                                contentValues.put(colName, dflt)
                            } else if (colType.contains("REAL") || colType.contains("FLOA") || colType.contains("DOUB")) {
                                val dflt = col.defaultValue?.toDoubleOrNull() ?: 0.0
                                contentValues.put(colName, dflt)
                            } else {
                                if (col.notNull) {
                                    contentValues.put(colName, "")
                                } else {
                                    contentValues.putNull(colName)
                                }
                            }
                        }
                    }

                    // Inserimento o sostituzione
                    db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, contentValues)
                    restoredRowsCount++
                }
            }

            db.execSQL("PRAGMA foreign_keys = ON;")

            val summary = MigrationSummary(
                sourceVersion = sourceVersion,
                targetVersion = targetVersion,
                backupFilePath = backupFile.absolutePath,
                restoredTablesCount = restoredTablesCount,
                restoredRowsCount = restoredRowsCount
            )
            _lastMigrationSummary = summary
            Log.i(TAG, "Ripristino completato con successo: $restoredRowsCount record ripristinati in $restoredTablesCount tabelle.")
            return summary
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante il ripristino dei dati dal backup: ${e.message}", e)
            try {
                db.execSQL("PRAGMA foreign_keys = ON;")
            } catch (_: Exception) {}
            return null
        }
    }

    /**
     * Controlla e ripristina la migrazione pendente se presente.
     * Viene invocato al termine della creazione/apertura del database Room.
     */
    fun checkAndRestorePendingMigration(context: Context, db: SupportSQLiteDatabase): MigrationSummary? {
        val pendingFile = File(File(context.filesDir, "backups"), PENDING_RESTORE_FILE_NAME)
        if (!pendingFile.exists()) return null

        Log.i(TAG, "Rilevato ripristino pendente in: ${pendingFile.absolutePath}. Avvio ripristino dati...")
        val summary = restoreDataFromBackup(db, pendingFile)

        // Rimuove il file pendente dopo il ripristino
        try {
            pendingFile.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Impossibile rimuovere il file di ripristino pendente: ${e.message}")
        }

        return summary
    }

    /**
     * Restituisce la lista di tutti i file di backup disponibili nella memoria protetta.
     */
    fun listBackups(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles { file ->
            file.isFile && file.name.endsWith(".json") && file.name != PENDING_RESTORE_FILE_NAME
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private data class ColumnInfo(
        val name: String,
        val type: String,
        val notNull: Boolean,
        val defaultValue: String?
    )

    private fun getTableColumnInfo(db: SupportSQLiteDatabase, tableName: String): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()
        val cursor = db.query("PRAGMA table_info(`$tableName`)")
        val nameIndex = cursor.getColumnIndex("name")
        val typeIndex = cursor.getColumnIndex("type")
        val notNullIndex = cursor.getColumnIndex("notnull")
        val dfltIndex = cursor.getColumnIndex("dflt_value")

        while (cursor.moveToNext()) {
            val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
            val type = if (typeIndex >= 0) cursor.getString(typeIndex) else ""
            val notNull = if (notNullIndex >= 0) cursor.getInt(notNullIndex) == 1 else false
            val dfltValue = if (dfltIndex >= 0 && !cursor.isNull(dfltIndex)) cursor.getString(dfltIndex) else null
            if (name.isNotEmpty()) {
                columns.add(ColumnInfo(name, type, notNull, dfltValue))
            }
        }
        cursor.close()
        return columns
    }

    /**
     * Determina se una colonna rappresenta un campo di ordinamento, sequenza o posizione.
     */
    fun isOrderOrSequenceColumn(columnName: String): Boolean {
        val name = columnName.lowercase(Locale.ROOT)
        val patterns = listOf(
            "order",
            "sort",
            "position",
            "pos",
            "index",
            "idx",
            "sequence",
            "seq",
            "rank",
            "priority"
        )
        return patterns.any { p ->
            name == p || name.contains(p)
        }
    }
}
