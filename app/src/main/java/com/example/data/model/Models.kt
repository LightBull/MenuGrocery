package com.example.data.model

import java.util.UUID

enum class MealType(val displayName: String, val typicalTime: String) {
    BREAKFAST("Colazione", "08:00"),
    SNACK_AM("Spuntino Mattutino", "11:00"),
    LUNCH("Pranzo", "13:00"),
    SNACK_PM("Spuntino Pomeridiano", "16:30"),
    DINNER("Cena", "20:00")
}

enum class QuantityUnit(val displayName: String, val shortName: String) {
    GRAMS("Grammi", "g"),
    PIECES("Pezzi / Porzioni", "pz")
}

data class DayOfWeekInfo(
    val dayIndex: Int, // 1 = Lunedì, 7 = Domenica
    val name: String,
    val shortName: String
) {
    val fullName: String get() = name

    companion object {
        val ALL_DAYS = listOf(
            DayOfWeekInfo(1, "Lunedì", "LUN"),
            DayOfWeekInfo(2, "Martedì", "MAR"),
            DayOfWeekInfo(3, "Mercoledì", "MER"),
            DayOfWeekInfo(4, "Giovedì", "GIO"),
            DayOfWeekInfo(5, "Venerdì", "VEN"),
            DayOfWeekInfo(6, "Sabato", "SAB"),
            DayOfWeekInfo(7, "Domenica", "DOM")
        )

        fun fromIndex(index: Int): DayOfWeekInfo {
            return ALL_DAYS.firstOrNull { it.dayIndex == index } ?: ALL_DAYS[0]
        }
    }
}

data class NutritionalSummary(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

data class MealWithItems(
    val mealType: MealType,
    val dayOfWeek: Int,
    val mealId: String,
    val items: List<FoodItemModel>
)

data class FoodItemModel(
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val name: String,
    val brand: String? = null,
    val quantity: Double,
    val unit: QuantityUnit,
    val toDefrost: Boolean = false,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val imageUrl: String? = null,
    val source: String = "OpenFoodFacts",
    val category: String = ""
)
