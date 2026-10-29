package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MealType
import com.example.data.model.QuantityUnit

class MealPlanConverters {
    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType = try {
        MealType.valueOf(value)
    } catch (_: Exception) {
        MealType.LUNCH
    }

    @TypeConverter
    fun fromQuantityUnit(value: QuantityUnit): String = value.name

    @TypeConverter
    fun toQuantityUnit(value: String): QuantityUnit = try {
        QuantityUnit.valueOf(value)
    } catch (_: Exception) {
        QuantityUnit.GRAMS
    }
}
