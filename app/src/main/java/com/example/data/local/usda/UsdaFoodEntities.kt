package com.example.data.local.usda

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long? = null,
    @ColumnInfo(name = "name_en") val nameEn: String,
    @ColumnInfo(name = "name_it") val nameIt: String? = null
)

@Entity(
    tableName = "foods",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"]
        )
    ]
)
data class FoodEntity(
    @PrimaryKey val id: Long? = null,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    @ColumnInfo(name = "is_common", defaultValue = "1") val isCommon: Boolean? = true
)

@Entity(
    tableName = "food_translations",
    primaryKeys = ["food_id", "lang_code"],
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodTranslationEntity(
    @ColumnInfo(name = "food_id") val foodId: Long,
    @ColumnInfo(name = "lang_code") val langCode: String,
    val description: String,
    @ColumnInfo(name = "search_keywords") val searchKeywords: String? = null
)

@Entity(
    tableName = "nutrients",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class NutrientEntity(
    @PrimaryKey val id: Long? = null,
    val code: String? = null,
    @ColumnInfo(name = "name_en") val nameEn: String,
    @ColumnInfo(name = "name_it") val nameIt: String,
    @ColumnInfo(name = "unit_name") val unitName: String
)

@Entity(
    tableName = "food_nutrients",
    primaryKeys = ["food_id", "nutrient_id"],
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NutrientEntity::class,
            parentColumns = ["id"],
            childColumns = ["nutrient_id"]
        )
    ]
)
data class FoodNutrientEntity(
    @ColumnInfo(name = "food_id") val foodId: Long,
    @ColumnInfo(name = "nutrient_id") val nutrientId: Long,
    val amount: Double
)
