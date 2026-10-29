package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MealType
import com.example.data.model.QuantityUnit

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val isActive: Boolean, // BR-01: solo 1 attivo
    val createdAt: Long,
    val notes: String? = null
)

@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class MealEntity(
    @PrimaryKey
    val id: String,
    val planId: String,
    val dayOfWeek: Int, // 1 = Lun, 7 = Dom
    val mealType: MealType
)

@Entity(
    tableName = "meal_food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mealId"])]
)
data class MealFoodItemEntity(
    @PrimaryKey
    val id: String,
    val mealId: String,
    val name: String,
    val brand: String? = null,
    val quantity: Double,
    val unit: QuantityUnit,
    val toDefrost: Boolean = false, // BR-03
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val imageUrl: String? = null,
    val category: String = ""
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey
    val id: String,
    val planId: String?,
    val name: String,
    val quantity: Double,
    val originalQuantity: Double = quantity,
    val pantryQuantity: Double = 0.0,
    val unit: QuantityUnit,
    val isPurchased: Boolean = false,
    val isManualExtra: Boolean = false,
    val sourceDays: String = "", // es: "LUN, MAR"
    val category: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
