package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    suspend fun getAllPlansSync(): List<MealPlanEntity>

    @Query("SELECT * FROM meal_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<MealPlanEntity?>

    @Query("SELECT * FROM meal_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlanSync(): MealPlanEntity?

    @Query("SELECT * FROM meal_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: String): MealPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MealPlanEntity)

    @Update
    suspend fun updatePlan(plan: MealPlanEntity)

    @Delete
    suspend fun deletePlan(plan: MealPlanEntity)

    @Query("UPDATE meal_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Query("UPDATE meal_plans SET isActive = 1 WHERE id = :planId")
    suspend fun markPlanActive(planId: String)

    @Query("UPDATE meal_plans SET gradualityConfigJson = :configJson WHERE id = :planId")
    suspend fun updateGradualityConfig(planId: String, configJson: String?)

    // BR-01: Isolamento del Piano - transazione che garantisce un solo piano attivo
    @Transaction
    suspend fun setActivePlan(planId: String) {
        deactivateAllPlans()
        markPlanActive(planId)
    }
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE planId = :planId AND dayOfWeek = :dayOfWeek")
    fun getMealsForPlanAndDay(planId: String, dayOfWeek: Int): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE planId = :planId AND dayOfWeek = :dayOfWeek")
    suspend fun getMealsForPlanAndDaySync(planId: String, dayOfWeek: Int): List<MealEntity>

    @Query("SELECT * FROM meals WHERE planId = :planId")
    fun getMealsForPlan(planId: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE planId = :planId")
    suspend fun getMealsForPlanSync(planId: String): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
}

@Dao
interface MealFoodItemDao {
    @Query("SELECT * FROM meal_food_items WHERE mealId = :mealId")
    fun getItemsForMeal(mealId: String): Flow<List<MealFoodItemEntity>>

    @Query("SELECT * FROM meal_food_items WHERE mealId = :mealId")
    suspend fun getItemsForMealSync(mealId: String): List<MealFoodItemEntity>

    @Query("SELECT * FROM meal_food_items WHERE mealId IN (:mealIds)")
    fun getItemsForMeals(mealIds: List<String>): Flow<List<MealFoodItemEntity>>

    @Query("SELECT * FROM meal_food_items WHERE mealId IN (:mealIds)")
    suspend fun getItemsForMealsSync(mealIds: List<String>): List<MealFoodItemEntity>

    @Query("SELECT * FROM meal_food_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): MealFoodItemEntity?

    @Query("SELECT * FROM meal_food_items WHERE name LIKE '%' || :query || '%' GROUP BY name ORDER BY name ASC")
    suspend fun searchLocalPreviousFoods(query: String): List<MealFoodItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MealFoodItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MealFoodItemEntity>)

    @Update
    suspend fun updateItem(item: MealFoodItemEntity)

    @Delete
    suspend fun deleteItem(item: MealFoodItemEntity)

    @Query("DELETE FROM meal_food_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("UPDATE meal_food_items SET quantity = :newQuantity WHERE id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Double)
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY isPurchased ASC, name ASC")
    fun getAllItems(): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items")
    suspend fun getAllItemsSync(): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_list_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdSync(id: String): ShoppingListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM shopping_list_items")
    suspend fun clearAll()

    @Query("DELETE FROM shopping_list_items WHERE isPurchased = 1")
    suspend fun clearPurchased()

    @Query("UPDATE shopping_list_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchasedStatus(id: String, isPurchased: Boolean)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}

@Dao
interface FoodEmbeddingDao {
    @Query("SELECT * FROM food_embeddings ORDER BY updatedAt DESC")
    fun getAllEmbeddings(): Flow<List<FoodEmbeddingEntity>>

    @Query("SELECT * FROM food_embeddings")
    suspend fun getAllEmbeddingsSync(): List<FoodEmbeddingEntity>

    @Query("SELECT * FROM food_embeddings WHERE foodName LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchEmbeddingsByName(query: String): List<FoodEmbeddingEntity>

    @Query("SELECT * FROM food_embeddings WHERE id = :id LIMIT 1")
    suspend fun getEmbeddingById(id: String): FoodEmbeddingEntity?

    @Query("SELECT * FROM food_embeddings WHERE foodName = :foodName LIMIT 1")
    suspend fun getEmbeddingByFoodName(foodName: String): FoodEmbeddingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(embedding: FoodEmbeddingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbeddings(embeddings: List<FoodEmbeddingEntity>)

    @Delete
    suspend fun deleteEmbedding(embedding: FoodEmbeddingEntity)

    @Query("DELETE FROM food_embeddings")
    suspend fun clearAll()
}

