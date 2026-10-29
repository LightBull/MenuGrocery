package com.example.data.repository

import android.content.Context
import com.example.data.local.MealEntity
import com.example.data.local.MealFoodItemEntity
import com.example.data.local.MealPlanDatabase
import com.example.data.local.MealPlanEntity
import com.example.data.local.ShoppingListItemEntity
import com.example.data.model.DayOfWeekInfo
import com.example.data.model.FoodItemModel
import com.example.data.model.MealType
import com.example.data.model.MealWithItems
import com.example.data.model.NutritionalSummary
import com.example.data.model.QuantityUnit
import com.example.data.model.FoodCategory
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.remote.UsdaFoodDataApi
import com.example.notification.DefrostScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import java.util.UUID

class MealPlanRepository(
    private val database: MealPlanDatabase,
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val context: Context,
    private val usdaFoodDataApi: UsdaFoodDataApi = UsdaFoodDataApi.create()
) {
    private val mealPlanDao = database.mealPlanDao()
    private val mealDao = database.mealDao()
    private val mealFoodItemDao = database.mealFoodItemDao()
    private val shoppingListDao = database.shoppingListDao()

    // --- Piani Alimentari ---
    fun getAllPlans(): Flow<List<MealPlanEntity>> = mealPlanDao.getAllPlans()

    fun getActivePlan(): Flow<MealPlanEntity?> = mealPlanDao.getActivePlan()

    suspend fun getActivePlanSync(): MealPlanEntity? = mealPlanDao.getActivePlanSync()

    suspend fun createPlan(title: String, notes: String?, setAsActive: Boolean): String = withContext(Dispatchers.IO) {
        val planId = UUID.randomUUID().toString()
        val plan = MealPlanEntity(
            id = planId,
            title = title,
            isActive = setAsActive,
            createdAt = System.currentTimeMillis(),
            notes = notes
        )
        if (setAsActive) {
            mealPlanDao.deactivateAllPlans()
        }
        mealPlanDao.insertPlan(plan)

        // Crea le 5 righe pasto per tutti i 7 giorni per questo piano
        val mealsToCreate = mutableListOf<MealEntity>()
        for (day in 1..7) {
            for (type in MealType.entries) {
                mealsToCreate.add(
                    MealEntity(
                        id = UUID.randomUUID().toString(),
                        planId = planId,
                        dayOfWeek = day,
                        mealType = type
                    )
                )
            }
        }
        mealDao.insertMeals(mealsToCreate)
        planId
    }

    suspend fun activatePlan(planId: String) = withContext(Dispatchers.IO) {
        mealPlanDao.setActivePlan(planId)

        // Risincronizza gli allarmi di scongelamento per il nuovo piano attivo
        DefrostScheduler.resyncAllActiveDefrostReminders(context, database)

        // Rigenera la lista della spesa dal piano attivo
        generateShoppingListFromActivePlan(
            selectedDays = listOf(1, 2, 3, 4, 5, 6, 7),
            preserveManualExtras = true
        )
    }

    suspend fun deletePlan(plan: MealPlanEntity) = withContext(Dispatchers.IO) {
        mealPlanDao.deletePlan(plan)
        // Se il piano eliminato era attivo o per sicurezza generale, risincronizza gli allarmi
        DefrostScheduler.resyncAllActiveDefrostReminders(context, database)
    }

    // --- Pasti del Piano Attivo (BR-01) ---
    suspend fun getMealsWithItemsForDay(planId: String, dayOfWeek: Int): List<MealWithItems> = withContext(Dispatchers.IO) {
        var meals = mealDao.getMealsForPlanAndDaySync(planId, dayOfWeek)
        if (meals.isEmpty()) {
            // Se non ancora creati, creali al volo
            val missingMeals = MealType.entries.map { type ->
                MealEntity(
                    id = UUID.randomUUID().toString(),
                    planId = planId,
                    dayOfWeek = dayOfWeek,
                    mealType = type
                )
            }
            mealDao.insertMeals(missingMeals)
            meals = missingMeals
        }

        val allMealIds = meals.map { it.id }
        val allItems = mealFoodItemDao.getItemsForMealsSync(allMealIds)

        MealType.entries.map { type ->
            val matchingMeals = meals.filter { it.mealType == type }
            // Preferisci il pasto che ha cibi associati, altrimenti il primo disponibile
            val canonicalMeal = matchingMeals.firstOrNull { meal ->
                allItems.any { it.mealId == meal.id }
            } ?: matchingMeals.firstOrNull()

            val items = if (canonicalMeal != null) {
                allItems.filter { it.mealId == canonicalMeal.id }.map { it.toDomainModel() }
            } else {
                emptyList()
            }
            MealWithItems(
                mealType = type,
                dayOfWeek = dayOfWeek,
                mealId = canonicalMeal?.id ?: UUID.randomUUID().toString(),
                items = items
            )
        }
    }

    suspend fun addFoodItem(
        mealId: String,
        name: String,
        brand: String?,
        quantity: Double,
        unit: QuantityUnit,
        toDefrost: Boolean,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        imageUrl: String?,
        dayOfWeek: Int,
        mealType: MealType
    ): String = withContext(Dispatchers.IO) {
        val itemId = UUID.randomUUID().toString()
        val entity = MealFoodItemEntity(
            id = itemId,
            mealId = mealId,
            name = name,
            brand = brand,
            quantity = quantity,
            unit = unit,
            toDefrost = toDefrost,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            imageUrl = imageUrl
        )
        mealFoodItemDao.insertItem(entity)

        // BR-03: Se l'alimento è contrassegnato "Da scongelare", programma la notifica alle 20:00 del giorno prima
        if (toDefrost) {
            DefrostScheduler.scheduleDefrostReminder(
                context = context,
                foodItemId = itemId,
                foodName = name,
                mealDisplayName = mealType.displayName,
                dayOfWeek = dayOfWeek
            )
        }
        itemId
    }

    suspend fun updateFoodItem(
        item: FoodItemModel,
        dayOfWeek: Int,
        mealDisplayName: String
    ) = withContext(Dispatchers.IO) {
        val entity = MealFoodItemEntity(
            id = item.id,
            mealId = item.mealId,
            name = item.name,
            brand = item.brand,
            quantity = item.quantity,
            unit = item.unit,
            toDefrost = item.toDefrost,
            calories = item.calories,
            protein = item.protein,
            carbs = item.carbs,
            fat = item.fat,
            imageUrl = item.imageUrl
        )
        mealFoodItemDao.updateItem(entity)

        if (item.toDefrost) {
            DefrostScheduler.scheduleDefrostReminder(
                context = context,
                foodItemId = item.id,
                foodName = item.name,
                mealDisplayName = mealDisplayName,
                dayOfWeek = dayOfWeek
            )
        } else {
            DefrostScheduler.cancelDefrostReminder(context, item.id)
        }
    }

    suspend fun deleteFoodItem(itemId: String) = withContext(Dispatchers.IO) {
        mealFoodItemDao.deleteItemById(itemId)
        DefrostScheduler.cancelDefrostReminder(context, itemId)
    }

    // --- Lista della Spesa Intelligente (BR-01, BR-02) ---
    fun getShoppingList(): Flow<List<ShoppingListItemEntity>> = shoppingListDao.getAllItems()

    /**
     * Genera la lista della spesa leggendo ESCLUSIVAMENTE dal piano Attivo (BR-01)
     * e aggregando le quantità per stesso alimento e stessa unità di misura (BR-02).
     */
    suspend fun generateShoppingListFromActivePlan(
        selectedDays: List<Int>, // es: listOf(1, 2, 3, 4)
        preserveManualExtras: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val activePlan = mealPlanDao.getActivePlanSync() ?: return@withContext

        // Raggruppa e aggrega le quantità per (nome.trim().lowercase(), unit)
        // Regola BR-02: Se le unità differiscono (es. Pezzi vs Grammi), mantenere righe separate.
        data class AggregationKey(val normalizedName: String, val unit: QuantityUnit)

        val existingItems = shoppingListDao.getAllItemsSync()
        val existingPantryMap = existingItems.associate { 
            AggregationKey(it.name.trim().lowercase(), it.unit) to it.pantryQuantity 
        }

        // Mantieni eventuali extra manuali e ripristinali come "da prendere"
        val existingExtras = if (preserveManualExtras) {
            existingItems.filter { it.isManualExtra }.map { it.copy(isPurchased = false) }
        } else {
            emptyList()
        }

        // Recupera tutti i pasti del piano attivo per i giorni selezionati
        val allMeals = mealDao.getMealsForPlanSync(activePlan.id)
            .filter { it.dayOfWeek in selectedDays }

        val mealIds = allMeals.map { it.id }
        val items = mealFoodItemDao.getItemsForMealsSync(mealIds)

        val mealIdToDay = allMeals.associate { it.id to it.dayOfWeek }

        data class AggregatedFoodItem(
            val displayName: String,
            var totalQuantity: Double,
            var category: String,
            val days: MutableSet<Int> = mutableSetOf()
        )

        val aggregatedMap = mutableMapOf<AggregationKey, AggregatedFoodItem>()
        val dayNames = selectedDays.sorted().joinToString(", ") { DayOfWeekInfo.fromIndex(it).shortName }

        for (item in items) {
            val key = AggregationKey(item.name.trim().lowercase(), item.unit)
            val day = mealIdToDay[item.mealId]
            val cat = item.category.ifBlank { FoodCategory.categorize(item.name).displayName }
            val current = aggregatedMap[key]
            if (current == null) {
                val newEntry = AggregatedFoodItem(item.name.trim(), item.quantity, cat)
                if (day != null) newEntry.days.add(day)
                aggregatedMap[key] = newEntry
            } else {
                current.totalQuantity += item.quantity
                if (current.category.isBlank()) current.category = cat
                if (day != null) current.days.add(day)
            }
        }

        val generatedItems = aggregatedMap.map { (key, value) ->
            val pantryQty = existingPantryMap[key] ?: 0.0
            val actualQty = (value.totalQuantity - pantryQty).coerceAtLeast(0.0)
            val itemDaysStr = if (value.days.isNotEmpty()) {
                value.days.sorted().joinToString(", ") { DayOfWeekInfo.fromIndex(it).shortName }
            } else {
                dayNames
            }
            ShoppingListItemEntity(
                id = UUID.randomUUID().toString(),
                planId = activePlan.id,
                name = value.displayName,
                quantity = if (pantryQty > 0) actualQty else value.totalQuantity,
                originalQuantity = value.totalQuantity,
                pantryQuantity = pantryQty,
                unit = key.unit,
                isPurchased = false,
                isManualExtra = false,
                sourceDays = itemDaysStr,
                category = value.category
            )
        }

        shoppingListDao.clearAll()
        shoppingListDao.insertItems(generatedItems + existingExtras)
    }

    /**
     * Ripristina tutti gli articoli della lista della spesa come "da prendere" (isPurchased = false),
     * preservando le quantità da comprare e la dispensa.
     */
    suspend fun resetShoppingListPurchasedStatus() = withContext(Dispatchers.IO) {
        val items = shoppingListDao.getAllItemsSync()
        items.forEach { item ->
            if (item.isPurchased) {
                shoppingListDao.updateItem(item.copy(isPurchased = false))
            }
        }
    }

    suspend fun updateShoppingItemQuantities(
        id: String,
        actualQuantity: Double,
        pantryQuantity: Double
    ) = withContext(Dispatchers.IO) {
        val items = shoppingListDao.getAllItemsSync()
        val item = items.firstOrNull { it.id == id } ?: return@withContext
        shoppingListDao.updateItem(
            item.copy(
                quantity = actualQuantity,
                pantryQuantity = pantryQuantity
            )
        )
    }

    suspend fun toggleItemPurchased(item: ShoppingListItemEntity) = withContext(Dispatchers.IO) {
        shoppingListDao.updateItem(item.copy(isPurchased = !item.isPurchased))
    }

    suspend fun addManualExtra(name: String, quantity: Double, unit: QuantityUnit) = withContext(Dispatchers.IO) {
        val activePlan = mealPlanDao.getActivePlanSync()
        val item = ShoppingListItemEntity(
            id = UUID.randomUUID().toString(),
            planId = activePlan?.id,
            name = name.trim(),
            quantity = quantity,
            unit = unit,
            isPurchased = false,
            isManualExtra = true,
            sourceDays = "Extra",
            category = FoodCategory.EXTRA_HOUSEHOLD.displayName
        )
        shoppingListDao.insertItem(item)
    }

    suspend fun deleteShoppingItem(id: String) = withContext(Dispatchers.IO) {
        shoppingListDao.deleteItemById(id)
    }

    suspend fun clearShoppingList() = withContext(Dispatchers.IO) {
        shoppingListDao.clearAll()
    }

    suspend fun clearPurchasedShoppingItems() = withContext(Dispatchers.IO) {
        shoppingListDao.clearPurchased()
    }

    /**
     * BR-02: Merge di una lista esterna con la lista esistente.
     * Se l'alimento è già presente con la stessa unità di misura, le quantità vengono sommate.
     * Se le unità differiscono (es. "Pezzi" vs "Grammi"), mantenere righe separate.
     */
    suspend fun mergeShoppingList(
        importedItems: List<ShoppingListItemEntity>,
        replaceExisting: Boolean
    ) = withContext(Dispatchers.IO) {
        if (replaceExisting) {
            shoppingListDao.clearAll()
            shoppingListDao.insertItems(importedItems)
            return@withContext
        }

        // Esegui il merge intelligente
        val currentItems = shoppingListDao.getAllItemsSync().toMutableList()

        for (imported in importedItems) {
            val normalizedName = imported.name.trim().lowercase()
            val matchIndex = currentItems.indexOfFirst {
                it.name.trim().lowercase() == normalizedName && it.unit == imported.unit
            }

            if (matchIndex != -1) {
                val existing = currentItems[matchIndex]
                val updated = existing.copy(
                    quantity = existing.quantity + imported.quantity
                )
                currentItems[matchIndex] = updated
                shoppingListDao.updateItem(updated)
            } else {
                val newItem = imported.copy(id = UUID.randomUUID().toString())
                currentItems.add(newItem)
                shoppingListDao.insertItem(newItem)
            }
        }
    }

    // --- Ricerca Nutrizionale Ibrida: Ricerca Locale (precedenti inserimenti) + USDA + OpenFoodFacts ---
    suspend fun searchFood(query: String): List<FoodItemModel> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        // 1. Cerca prima tra gli alimenti già inseriti nel database (evita doppioni, raggruppati per descrizione)
        val localEntities = mealFoodItemDao.searchLocalPreviousFoods(cleanQuery)
        val localResults = localEntities.map { entity ->
            FoodItemModel(
                mealId = "",
                name = entity.name,
                brand = entity.brand,
                quantity = entity.quantity,
                unit = entity.unit,
                toDefrost = entity.toDefrost,
                calories = entity.calories,
                protein = entity.protein,
                carbs = entity.carbs,
                fat = entity.fat,
                imageUrl = entity.imageUrl,
                source = "Local", // Pallino giallo
                category = entity.category.ifBlank { FoodCategory.categorize(entity.name).displayName }
            )
        }.distinctBy { it.name.trim().lowercase() }

        // 2. Se non ci sono risultati precedenti, per ogni tentativo iterare la ricerca esterna fino a 5 volte
        // Non è obbligatorio che entrambi i servizi producano risultati
        val externalResults = mutableListOf<FoodItemModel>()
        val maxAttempts = if (localResults.isEmpty()) 5 else 1

        for (attempt in 1..maxAttempts) {
            val offDeferred = async {
                try {
                    val response = openFoodFactsApi.searchProducts(terms = cleanQuery)
                    response.products?.mapNotNull { product ->
                        val name = product.productName ?: return@mapNotNull null
                        val nutriments = product.nutriments
                        val calories = nutriments?.energyKcal100g ?: nutriments?.energyKcal ?: 0.0
                        FoodItemModel(
                            mealId = "",
                            name = name,
                            brand = product.brands,
                            quantity = 100.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = calories,
                            protein = nutriments?.proteins100g ?: 0.0,
                            carbs = nutriments?.carbohydrates100g ?: 0.0,
                            fat = nutriments?.fat100g ?: 0.0,
                            imageUrl = product.imageUrl,
                            source = "OpenFoodFacts", // Pallino rosso
                            category = FoodCategory.categorize(name).displayName
                        )
                    } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val usdaDeferred = async {
                try {
                    val response = usdaFoodDataApi.searchFoods(query = cleanQuery)
                    response.foods?.mapNotNull { food ->
                        val name = food.description ?: return@mapNotNull null
                        val brand = food.brandOwner ?: food.brandName
                        val nutrients = food.foodNutrients ?: emptyList()

                        var calories = 0.0
                        var protein = 0.0
                        var carbs = 0.0
                        var fat = 0.0

                        for (n in nutrients) {
                            val nName = (n.nutrientName ?: "").lowercase()
                            val uName = (n.unitName ?: "").uppercase()
                            val value = n.value ?: 0.0
                            when {
                                nName.contains("energy") || uName == "KCAL" -> if (calories == 0.0) calories = value
                                nName.contains("protein") -> protein = value
                                nName.contains("carbohydrate") -> carbs = value
                                nName.contains("total lipid") || (nName.contains("fat") && !nName.contains("fatty")) -> fat = value
                            }
                        }

                        FoodItemModel(
                            mealId = "",
                            name = name,
                            brand = brand,
                            quantity = 100.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat,
                            imageUrl = null,
                            source = "USDA", // Pallino rosso
                            category = FoodCategory.categorize(name).displayName
                        )
                    } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val offList = offDeferred.await()
            val usdaList = usdaDeferred.await()
            val attemptList = (offList + usdaList).distinctBy { it.name.trim().lowercase() }

            if (attemptList.isNotEmpty()) {
                externalResults.addAll(attemptList)
                break
            } else if (attempt < maxAttempts) {
                delay(200)
            }
        }

        // Unione risultati: prima i locali (se ci sono), poi gli esterni, senza duplicati
        val combined = mutableListOf<FoodItemModel>()
        val seenNames = mutableSetOf<String>()

        for (item in localResults) {
            val key = item.name.trim().lowercase()
            if (seenNames.add(key)) {
                combined.add(item)
            }
        }

        for (item in externalResults) {
            val key = item.name.trim().lowercase()
            if (seenNames.add(key)) {
                combined.add(item)
            }
        }

        combined
    }

    suspend fun copyFoodItemToMeals(
        foodItem: FoodItemModel,
        targetDays: List<Int>,
        targetMealTypes: List<MealType>
    ) = withContext(Dispatchers.IO) {
        val activePlan = mealPlanDao.getActivePlanSync() ?: return@withContext
        for (day in targetDays) {
            var dayMeals = mealDao.getMealsForPlanAndDaySync(activePlan.id, day)
            for (mealType in targetMealTypes) {
                var meal = dayMeals.firstOrNull { it.mealType == mealType }
                if (meal == null) {
                    val newMeal = MealEntity(
                        id = UUID.randomUUID().toString(),
                        planId = activePlan.id,
                        dayOfWeek = day,
                        mealType = mealType
                    )
                    mealDao.insertMeal(newMeal)
                    meal = newMeal
                }

                val newEntity = MealFoodItemEntity(
                    id = UUID.randomUUID().toString(),
                    mealId = meal.id,
                    name = foodItem.name,
                    brand = foodItem.brand,
                    quantity = foodItem.quantity,
                    unit = foodItem.unit,
                    toDefrost = foodItem.toDefrost,
                    calories = foodItem.calories,
                    protein = foodItem.protein,
                    carbs = foodItem.carbs,
                    fat = foodItem.fat,
                    imageUrl = foodItem.imageUrl,
                    category = foodItem.category.ifBlank { FoodCategory.categorize(foodItem.name).displayName }
                )
                mealFoodItemDao.insertItem(newEntity)

                if (foodItem.toDefrost) {
                    DefrostScheduler.scheduleDefrostReminder(
                        context = context,
                        foodItemId = newEntity.id,
                        foodName = foodItem.name,
                        mealDisplayName = mealType.displayName,
                        dayOfWeek = day
                    )
                }
            }
        }
    }

    // --- Metodi Completi per Editor e Gestione Piani ---
    suspend fun getPlanWithAllDaysMeals(planId: String): Map<Int, List<MealWithItems>> = withContext(Dispatchers.IO) {
        val resultMap = mutableMapOf<Int, List<MealWithItems>>()
        for (day in 1..7) {
            resultMap[day] = getMealsWithItemsForDay(planId, day)
        }
        resultMap
    }

    suspend fun saveFullPlan(
        planId: String,
        title: String,
        notes: String?,
        setAsActive: Boolean,
        daysMeals: Map<Int, List<MealWithItems>>
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPlan = mealPlanDao.getPlanById(planId)
            val plan = if (existingPlan != null) {
                existingPlan.copy(
                    title = title,
                    notes = notes,
                    isActive = if (setAsActive) true else existingPlan.isActive
                )
            } else {
                MealPlanEntity(
                    id = planId,
                    title = title,
                    isActive = setAsActive,
                    createdAt = System.currentTimeMillis(),
                    notes = notes
                )
            }

            if (setAsActive) {
                mealPlanDao.deactivateAllPlans()
            }
            mealPlanDao.insertPlan(plan)

            // Recupera tutti i pasti esistenti per questo piano
            val existingMeals = mealDao.getMealsForPlanSync(planId)
            val existingMealIds = existingMeals.map { it.id }
            val existingItems = if (existingMealIds.isNotEmpty()) {
                mealFoodItemDao.getItemsForMealsSync(existingMealIds)
            } else {
                emptyList()
            }

            for (day in 1..7) {
                val dayMeals = daysMeals[day] ?: emptyList()
                for (type in MealType.entries) {
                    val mealData = dayMeals.firstOrNull { it.mealType == type }
                    val matchingExistingMeals = existingMeals.filter { it.dayOfWeek == day && it.mealType == type }

                    // Trova il pasto canonico: preferisci quello che aveva già cibi, oppure il mealId dei dati
                    val canonicalMealId = matchingExistingMeals.firstOrNull { m ->
                        existingItems.any { it.mealId == m.id }
                    }?.id
                        ?: matchingExistingMeals.firstOrNull()?.id
                        ?: mealData?.mealId
                        ?: UUID.randomUUID().toString()

                    mealDao.insertMeal(
                        MealEntity(
                            id = canonicalMealId,
                            planId = planId,
                            dayOfWeek = day,
                            mealType = type
                        )
                    )

                    // Cancella i vecchi cibi associati a questo pasto canonico
                    val oldItems = mealFoodItemDao.getItemsForMealSync(canonicalMealId)
                    for (oldItem in oldItems) {
                        mealFoodItemDao.deleteItem(oldItem)
                        DefrostScheduler.cancelDefrostReminder(context, oldItem.id)
                    }

                    // Se c'erano pasti orfani/duplicati per questo (day, type) nel DB, rimuovi cibi e pasto duplicato
                    val duplicates = matchingExistingMeals.filter { it.id != canonicalMealId }
                    for (dup in duplicates) {
                        val dupItems = mealFoodItemDao.getItemsForMealSync(dup.id)
                        for (dupItem in dupItems) {
                            mealFoodItemDao.deleteItem(dupItem)
                            DefrostScheduler.cancelDefrostReminder(context, dupItem.id)
                        }
                        mealDao.deleteMeal(dup)
                    }

                    // Inserisci i nuovi cibi
                    if (mealData != null) {
                        for (item in mealData.items) {
                            val entity = MealFoodItemEntity(
                                id = item.id.ifBlank { UUID.randomUUID().toString() },
                                mealId = canonicalMealId,
                                name = item.name,
                                brand = item.brand,
                                quantity = item.quantity,
                                unit = item.unit,
                                toDefrost = item.toDefrost,
                                calories = item.calories,
                                protein = item.protein,
                                carbs = item.carbs,
                                fat = item.fat,
                                imageUrl = item.imageUrl,
                                category = item.category.ifBlank { FoodCategory.categorize(item.name).displayName }
                            )
                            mealFoodItemDao.insertItem(entity)
                        }
                    }
                }
            }

            // Se il piano salvato è quello attivo (o impostato come attivo), risincronizza allarmi e lista spesa
            if (setAsActive || plan.isActive) {
                DefrostScheduler.resyncAllActiveDefrostReminders(context, database)
                generateShoppingListFromActivePlan(
                    selectedDays = listOf(1, 2, 3, 4, 5, 6, 7),
                    preserveManualExtras = true
                )
            }
        }
    }

    suspend fun duplicatePlan(sourcePlanId: String): String = withContext(Dispatchers.IO) {
        val sourcePlan = mealPlanDao.getPlanById(sourcePlanId) ?: return@withContext ""
        val newPlanId = UUID.randomUUID().toString()
        val newTitle = "${sourcePlan.title} (Copia)"

        val duplicatedPlan = MealPlanEntity(
            id = newPlanId,
            title = newTitle,
            isActive = false,
            createdAt = System.currentTimeMillis(),
            notes = sourcePlan.notes
        )
        mealPlanDao.insertPlan(duplicatedPlan)

        val sourceMeals = mealDao.getMealsForPlanSync(sourcePlanId)
        for (meal in sourceMeals) {
            val newMealId = UUID.randomUUID().toString()
            mealDao.insertMeal(
                MealEntity(
                    id = newMealId,
                    planId = newPlanId,
                    dayOfWeek = meal.dayOfWeek,
                    mealType = meal.mealType
                )
            )

            val sourceItems = mealFoodItemDao.getItemsForMealSync(meal.id)
            val newItems = sourceItems.map { item ->
                item.copy(
                    id = UUID.randomUUID().toString(),
                    mealId = newMealId
                )
            }
            mealFoodItemDao.insertItems(newItems)
        }
        newPlanId
    }

    // --- Inizializzazione Dati Dimostrativi ---
    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingPlans = mealPlanDao.getAllPlans().firstOrNull()
        if (!existingPlans.isNullOrEmpty()) return@withContext

        // Crea il piano predefinito attivo: "Piano Benessere Mediterraneo"
        val planId = UUID.randomUUID().toString()
        val defaultPlan = MealPlanEntity(
            id = planId,
            title = "Piano Benessere Mediterraneo",
            isActive = true, // BR-01: attivo
            createdAt = System.currentTimeMillis(),
            notes = "Dieta bilanciata ricca di verdure, pesce, cereali integrali e frutta di stagione."
        )
        mealPlanDao.insertPlan(defaultPlan)

        // Popola i pasti per la settimana con cibi autentici e flag scongelamento (BR-03)
        for (day in 1..7) {
            for (type in MealType.entries) {
                val mealId = UUID.randomUUID().toString()
                mealDao.insertMeal(
                    MealEntity(
                        id = mealId,
                        planId = planId,
                        dayOfWeek = day,
                        mealType = type
                    )
                )

                val items = when (type) {
                    MealType.BREAKFAST -> listOf(
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Yogurt Greco 0%",
                            brand = "Fage",
                            quantity = 170.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 97.0,
                            protein = 17.5,
                            carbs = 5.1,
                            fat = 0.0
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Fiocchi d'Avena Integrale",
                            brand = "Fiorentini",
                            quantity = 40.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 148.0,
                            protein = 5.2,
                            carbs = 26.0,
                            fat = 2.8
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Mirtilli Freschi",
                            brand = null,
                            quantity = 80.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 45.0,
                            protein = 0.6,
                            carbs = 11.6,
                            fat = 0.3
                        )
                    )
                    MealType.SNACK_AM -> listOf(
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Mela Golden",
                            brand = null,
                            quantity = 1.0,
                            unit = QuantityUnit.PIECES,
                            toDefrost = false,
                            calories = 72.0,
                            protein = 0.4,
                            carbs = 19.0,
                            fat = 0.2
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Mandorle Pelate",
                            brand = "Noberasco",
                            quantity = 20.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 116.0,
                            protein = 4.2,
                            carbs = 4.3,
                            fat = 10.0
                        )
                    )
                    MealType.LUNCH -> when (day % 2) {
                        0 -> listOf(
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Petto di Pollo",
                                brand = null,
                                quantity = 180.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = true, // BR-03: Scongelare!
                                calories = 216.0,
                                protein = 43.0,
                                carbs = 0.0,
                                fat = 3.6
                            ),
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Riso Basmati Integrale",
                                brand = "Gallo",
                                quantity = 80.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = false,
                                calories = 280.0,
                                protein = 6.8,
                                carbs = 60.0,
                                fat = 1.6
                            ),
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Zucchine Grigliate",
                                brand = null,
                                quantity = 200.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = false,
                                calories = 34.0,
                                protein = 2.4,
                                carbs = 6.2,
                                fat = 0.6
                            )
                        )
                        else -> listOf(
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Trancio di Salmone Selvaggio",
                                brand = null,
                                quantity = 160.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = true, // BR-03: Scongelare!
                                calories = 288.0,
                                protein = 32.0,
                                carbs = 0.0,
                                fat = 17.6
                            ),
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Patate Dolci al Forno",
                                brand = null,
                                quantity = 150.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = false,
                                calories = 129.0,
                                protein = 2.4,
                                carbs = 30.0,
                                fat = 0.2
                            ),
                            MealFoodItemEntity(
                                id = UUID.randomUUID().toString(),
                                mealId = mealId,
                                name = "Spinaci Novelli",
                                brand = "Bonduelle",
                                quantity = 150.0,
                                unit = QuantityUnit.GRAMS,
                                toDefrost = false,
                                calories = 35.0,
                                protein = 4.3,
                                carbs = 5.4,
                                fat = 0.6
                            )
                        )
                    }
                    MealType.SNACK_PM -> listOf(
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Gallette di Mais e Quinoa",
                            brand = "Fiorentini",
                            quantity = 3.0,
                            unit = QuantityUnit.PIECES,
                            toDefrost = false,
                            calories = 84.0,
                            protein = 2.1,
                            carbs = 18.0,
                            fat = 0.6
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Fesa di Tacchino",
                            brand = "Aequilibrium",
                            quantity = 60.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 64.0,
                            protein = 12.0,
                            carbs = 0.6,
                            fat = 0.9
                        )
                    )
                    MealType.DINNER -> listOf(
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Filetto di Merluzzo",
                            brand = "Findus",
                            quantity = 200.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = true, // BR-03: Scongelare!
                            calories = 164.0,
                            protein = 35.6,
                            carbs = 0.0,
                            fat = 1.4
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Broccoli al Vapore",
                            brand = null,
                            quantity = 220.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 75.0,
                            protein = 6.2,
                            carbs = 14.7,
                            fat = 0.8
                        ),
                        MealFoodItemEntity(
                            id = UUID.randomUUID().toString(),
                            mealId = mealId,
                            name = "Olio Extravergine d'Oliva",
                            brand = "Monini",
                            quantity = 10.0,
                            unit = QuantityUnit.GRAMS,
                            toDefrost = false,
                            calories = 88.0,
                            protein = 0.0,
                            carbs = 0.0,
                            fat = 10.0
                        )
                    )
                }
                mealFoodItemDao.insertItems(items)
            }
        }

        // Genera la lista della spesa iniziale per la settimana
        generateShoppingListFromActivePlan(selectedDays = listOf(1, 2, 3, 4, 5, 6, 7))

        // Aggiungi un paio di extra dimostrativi manuali
        addManualExtra("Detersivo Piatti Ecologico", 1.0, QuantityUnit.PIECES)
        addManualExtra("Sale Marino Integrale", 1.0, QuantityUnit.PIECES)

        // Risincronizza gli allarmi di scongelamento per il piano attivo
        DefrostScheduler.resyncAllActiveDefrostReminders(context, database)
    }

    suspend fun resetDatabaseToDefault() = withContext(Dispatchers.IO) {
        DefrostScheduler.cancelAllReminders(context)
        database.clearAllTables()
        initializeDefaultDataIfNeeded()
    }

    suspend fun resyncDefrostReminders(): Int = withContext(Dispatchers.IO) {
        DefrostScheduler.resyncAllActiveDefrostReminders(context, database)
    }

    suspend fun getDefrostDiagnosticItems(): List<com.example.data.model.DefrostDiagnosticItem> = withContext(Dispatchers.IO) {
        val activePlan = mealPlanDao.getActivePlanSync() ?: return@withContext emptyList()
        val meals = mealDao.getMealsForPlanSync(activePlan.id)
        val mealIds = meals.map { it.id }
        if (mealIds.isEmpty()) return@withContext emptyList()

        val foodItems = mealFoodItemDao.getItemsForMealsSync(mealIds)
        val defrostItems = foodItems.filter { it.toDefrost }

        val dayNames = mapOf(
            1 to "Lunedì", 2 to "Martedì", 3 to "Mercoledì", 4 to "Giovedì",
            5 to "Venerdì", 6 to "Sabato", 7 to "Domenica"
        )
        val reminderDayNames = mapOf(
            1 to "Domenica", 2 to "Lunedì", 3 to "Martedì", 4 to "Mercoledì",
            5 to "Giovedì", 6 to "Venerdì", 7 to "Sabato"
        )

        val trackedIds = DefrostScheduler.getTrackedItemIds(context)

        defrostItems.mapNotNull { item ->
            val meal = meals.firstOrNull { it.id == item.mealId } ?: return@mapNotNull null
            com.example.data.model.DefrostDiagnosticItem(
                foodItemId = item.id,
                foodName = item.name,
                dayOfWeek = meal.dayOfWeek,
                dayName = dayNames[meal.dayOfWeek] ?: "Giorno ${meal.dayOfWeek}",
                mealTypeDisplayName = meal.mealType.displayName,
                reminderDayName = reminderDayNames[meal.dayOfWeek] ?: "Giorno precedente",
                reminderTimeFormatted = "20:00",
                isScheduledInOs = trackedIds.contains(item.id)
            )
        }.sortedWith(compareBy({ it.dayOfWeek }, { it.mealTypeDisplayName }))
    }

    private fun MealFoodItemEntity.toDomainModel() = FoodItemModel(
        id = id,
        mealId = mealId,
        name = name,
        brand = brand,
        quantity = quantity,
        unit = unit,
        toDefrost = toDefrost,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        imageUrl = imageUrl,
        source = "Local",
        category = category.ifBlank { FoodCategory.categorize(name).displayName }
    )
}
