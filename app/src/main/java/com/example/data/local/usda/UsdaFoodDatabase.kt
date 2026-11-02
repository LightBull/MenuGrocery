package com.example.data.local.usda

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntity::class,
        FoodEntity::class,
        FoodTranslationEntity::class,
        NutrientEntity::class,
        FoodNutrientEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UsdaFoodDatabase : RoomDatabase() {
    abstract fun usdaFoodDao(): UsdaFoodDao

    companion object {
        const val DATABASE_NAME = "usda_food.db"

        @Volatile
        private var INSTANCE: UsdaFoodDatabase? = null

        fun getInstance(context: Context): UsdaFoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UsdaFoodDatabase::class.java,
                    DATABASE_NAME
                )
                    .createFromAsset("databases/usda_food.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
