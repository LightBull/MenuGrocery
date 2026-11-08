package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MealPlanEntity::class,
        MealEntity::class,
        MealFoodItemEntity::class,
        ShoppingListItemEntity::class,
        AppSettingEntity::class,
        FoodEmbeddingEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(MealPlanConverters::class)
abstract class MealPlanDatabase : RoomDatabase() {
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealDao(): MealDao
    abstract fun mealFoodItemDao(): MealFoodItemDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun foodEmbeddingDao(): FoodEmbeddingDao

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "mealplan_database"

        @Volatile
        private var INSTANCE: MealPlanDatabase? = null

        fun getDatabase(context: Context): MealPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                // Controlla se sul telefono è già presente un database e in caso di versione diversa crea il backup preventivo
                DatabaseMigrationManager.prepareMigrationIfNeeded(
                    context = appContext,
                    dbName = DATABASE_NAME,
                    targetVersion = DATABASE_VERSION
                )

                val instance = Room.databaseBuilder(
                    appContext,
                    MealPlanDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            DatabaseMigrationManager.checkAndRestorePendingMigration(appContext, db)
                        }

                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            DatabaseMigrationManager.checkAndRestorePendingMigration(appContext, db)
                        }
                    })
                    .build()

                // Forza l'apertura e l'esecuzione sincrona dei callback di migrazione/ripristino prima delle query
                try {
                    instance.openHelper.writableDatabase
                } catch (e: Exception) {
                    android.util.Log.e("MealPlanDatabase", "Errore apertura database: ${e.message}", e)
                }

                INSTANCE = instance
                instance
            }
        }
    }
}
