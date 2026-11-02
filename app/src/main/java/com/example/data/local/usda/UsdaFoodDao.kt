package com.example.data.local.usda

import androidx.room.Dao
import androidx.room.Query

data class UsdaFoodSearchResult(
    val id: Long,
    val description: String,
    val categoryIt: String?,
    val categoryEn: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)

@Dao
interface UsdaFoodDao {

    @Query("""
        SELECT f.id, ft.description, c.name_it AS categoryIt, c.name_en AS categoryEn,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1008) AS calories,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1003) AS protein,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1005) AS carbs,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1004) AS fat
        FROM foods f
        JOIN food_translations ft ON f.id = ft.food_id
        LEFT JOIN categories c ON f.category_id = c.id
        WHERE ft.lang_code = :langCode
          AND (ft.description LIKE '%' || :query || '%' OR (ft.search_keywords IS NOT NULL AND ft.search_keywords LIKE '%' || :query || '%'))
        ORDER BY f.is_common DESC, length(ft.description) ASC
        LIMIT :limit
    """)
    suspend fun searchFoods(query: String, langCode: String, limit: Int = 40): List<UsdaFoodSearchResult>

    @Query("""
        SELECT f.id, ft.description, c.name_it AS categoryIt, c.name_en AS categoryEn,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1008) AS calories,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1003) AS protein,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1005) AS carbs,
            (SELECT amount FROM food_nutrients WHERE food_id = f.id AND nutrient_id = 1004) AS fat
        FROM foods f
        JOIN food_translations ft ON f.id = ft.food_id
        LEFT JOIN categories c ON f.category_id = c.id
        WHERE (ft.description LIKE '%' || :query || '%' OR (ft.search_keywords IS NOT NULL AND ft.search_keywords LIKE '%' || :query || '%'))
        ORDER BY CASE WHEN ft.lang_code = :langCode THEN 0 ELSE 1 END, f.is_common DESC, length(ft.description) ASC
        LIMIT :limit
    """)
    suspend fun searchFoodsAnyLang(query: String, langCode: String, limit: Int = 40): List<UsdaFoodSearchResult>
}
