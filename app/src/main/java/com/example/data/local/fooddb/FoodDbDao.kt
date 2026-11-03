package com.example.data.local.fooddb

import androidx.room.Dao
import androidx.room.Query

/**
 * Risultato intermedio tipizzato per le query nutrizionali su FoodDb.db.
 */
data class FoodSearchResult(
    val name: String,
    val englishName: String?,
    val category: String?,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

/**
 * Data Access Object rigorosamente di SOLA LETTURA per FoodDb.db.
 * Nessuna operazione di INSERT, UPDATE o DELETE è definita o permessa.
 */
@Dao
interface FoodDbDao {

    // ==========================================
    // Query su tabella primaria (principal_food_tables)
    // ==========================================

    @Query("""
        SELECT name, english_name AS englishName, category,
               CAST(COALESCE(energy_kcal, 0) AS REAL) AS calories,
               COALESCE(proteins, 0.0) AS protein,
               COALESCE(available_carbohydrates, 0.0) AS carbs,
               COALESCE(lipids, 0.0) AS fat
        FROM principal_food_tables
        WHERE name LIKE '%' || :query || '%'
        ORDER BY length(name) ASC
        LIMIT :limit
    """)
    suspend fun searchPrincipalByName(query: String, limit: Int = 40): List<FoodSearchResult>

    @Query("""
        SELECT name, english_name AS englishName, category,
               CAST(COALESCE(energy_kcal, 0) AS REAL) AS calories,
               COALESCE(proteins, 0.0) AS protein,
               COALESCE(available_carbohydrates, 0.0) AS carbs,
               COALESCE(lipids, 0.0) AS fat
        FROM principal_food_tables
        WHERE english_name LIKE '%' || :query || '%'
        ORDER BY length(english_name) ASC
        LIMIT :limit
    """)
    suspend fun searchPrincipalByEnglishName(query: String, limit: Int = 40): List<FoodSearchResult>

    @Query("""
        SELECT name, english_name AS englishName, category,
               CAST(COALESCE(energy_kcal, 0) AS REAL) AS calories,
               COALESCE(proteins, 0.0) AS protein,
               COALESCE(available_carbohydrates, 0.0) AS carbs,
               COALESCE(lipids, 0.0) AS fat
        FROM principal_food_tables
        WHERE name LIKE '%' || :query || '%' OR english_name LIKE '%' || :query || '%'
        ORDER BY CASE WHEN name LIKE '%' || :query || '%' THEN 0 ELSE 1 END, length(name) ASC
        LIMIT :limit
    """)
    suspend fun searchPrincipalOr(query: String, limit: Int = 40): List<FoodSearchResult>

    // ==========================================
    // Query su tabella di fallback (food_back_archive)
    // ==========================================

    @Query("""
        SELECT name, english_name AS englishName, NULL AS category,
               COALESCE(Kcal, 0.0) AS calories,
               COALESCE(Proteins, 0.0) AS protein,
               COALESCE(Carbohydrates, 0.0) AS carbs,
               COALESCE(Fat, 0.0) AS fat
        FROM food_back_archive
        WHERE name LIKE '%' || :query || '%'
        ORDER BY length(name) ASC
        LIMIT :limit
    """)
    suspend fun searchBackByName(query: String, limit: Int = 40): List<FoodSearchResult>

    @Query("""
        SELECT name, english_name AS englishName, NULL AS category,
               COALESCE(Kcal, 0.0) AS calories,
               COALESCE(Proteins, 0.0) AS protein,
               COALESCE(Carbohydrates, 0.0) AS carbs,
               COALESCE(Fat, 0.0) AS fat
        FROM food_back_archive
        WHERE english_name LIKE '%' || :query || '%'
        ORDER BY length(english_name) ASC
        LIMIT :limit
    """)
    suspend fun searchBackByEnglishName(query: String, limit: Int = 40): List<FoodSearchResult>

    @Query("""
        SELECT name, english_name AS englishName, NULL AS category,
               COALESCE(Kcal, 0.0) AS calories,
               COALESCE(Proteins, 0.0) AS protein,
               COALESCE(Carbohydrates, 0.0) AS carbs,
               COALESCE(Fat, 0.0) AS fat
        FROM food_back_archive
        WHERE name LIKE '%' || :query || '%' OR english_name LIKE '%' || :query || '%'
        ORDER BY CASE WHEN name LIKE '%' || :query || '%' THEN 0 ELSE 1 END, length(name) ASC
        LIMIT :limit
    """)
    suspend fun searchBackOr(query: String, limit: Int = 40): List<FoodSearchResult>
}
