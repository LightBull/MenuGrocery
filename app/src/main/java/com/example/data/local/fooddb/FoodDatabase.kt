package com.example.data.local.fooddb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PrincipalFoodEntity::class,
        FoodBackArchiveEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDbDao(): FoodDbDao

    companion object {
        const val DATABASE_NAME = "FoodDb.db"

        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getInstance(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    DATABASE_NAME
                )
                    .createFromAsset("databases/FoodDb.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
