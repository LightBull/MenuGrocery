package com.example.data.local.fooddb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entità per la tabella primaria principal_food_tables in FoodDb.db.
 */
@Entity(
    tableName = "principal_food_tables",
    indices = [
        Index(value = ["name"], name = "idx_pft_name"),
        Index(value = ["english_name"], name = "idx_pft_eng")
    ]
)
data class PrincipalFoodEntity(
    @PrimaryKey
    @ColumnInfo(name = "food_code")
    val foodCode: String,
    val name: String,
    @ColumnInfo(name = "english_name")
    val englishName: String?,
    val category: String?,
    @ColumnInfo(name = "energy_kcal")
    val energyKcal: Int?,
    val proteins: Double?,
    @ColumnInfo(name = "available_carbohydrates")
    val availableCarbohydrates: Double?,
    val lipids: Double?
)

/**
 * Entità per la tabella secondaria food_back_archive in FoodDb.db.
 */
@Entity(
    tableName = "food_back_archive",
    indices = [
        Index(value = ["name"], name = "idx_fba_name"),
        Index(value = ["english_name"], name = "idx_fba_eng")
    ]
)
data class FoodBackArchiveEntity(
    @PrimaryKey
    val id: Int,
    val code: String?,
    val name: String,
    @ColumnInfo(name = "english_name")
    val englishName: String?,
    @ColumnInfo(name = "Proteins")
    val proteins: Double?,
    @ColumnInfo(name = "Carbohydrates")
    val carbs: Double?,
    @ColumnInfo(name = "Fat")
    val fat: Double?,
    @ColumnInfo(name = "Kcal")
    val calories: Double?,
    @ColumnInfo(name = "Salt")
    val salt: Double?,
    @ColumnInfo(name = "Fiber")
    val fiber: Double?,
    @ColumnInfo(name = "Sodium")
    val sodium: Double?,
    @ColumnInfo(name = "Sugar")
    val sugar: Double?,
    @ColumnInfo(name = "SaturatedFat")
    val saturatedFat: Double?
)
