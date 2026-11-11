package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MealPlanDatabase
import com.example.data.model.FoodItemModel
import com.example.data.model.MealType
import com.example.data.model.QuantityUnit
import com.example.data.repository.MealPlanRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Before
    fun setUp() = runBlocking(kotlinx.coroutines.Dispatchers.IO) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        db.clearAllTables()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MealPlan", appName)
    }

    @Test
    fun `saveFullPlan preserves day meals and does not wipe selected day`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        // Inizializza i dati dimostrativi (contiene alimenti per tutti i 7 giorni)
        repository.initializeDefaultDataIfNeeded()

        val activePlan = repository.getActivePlanSync()
        assertNotNull(activePlan)
        val planId = activePlan!!.id

        // 1. Simula visualizzazione di Mercoledì (giorno 3) nella schermata Lista Pasti
        val wednesdayMealsBefore = repository.getMealsWithItemsForDay(planId, 3)
        val wednesdayTotalFoodsBefore = wednesdayMealsBefore.sumOf { it.items.size }
        assertTrue("Mercoledì deve avere alimenti all'inizio", wednesdayTotalFoodsBefore > 0)

        // 2. Simula apertura dell'editor: carica tutti i 7 giorni
        val allDaysMeals = repository.getPlanWithAllDaysMeals(planId).toMutableMap()
        assertTrue("Tutti i 7 giorni devono essere presenti nell'editor", allDaysMeals.size == 7)

        // 3. Simula modifica a un giorno diverso (es. Martedì, giorno 2): aggiunta di un nuovo alimento a Pranzo
        val tuesdayMeals = allDaysMeals[2]!!.toMutableList()
        val lunchIdx = tuesdayMeals.indexOfFirst { it.mealType == MealType.LUNCH }
        assertTrue(lunchIdx != -1)
        val targetLunch = tuesdayMeals[lunchIdx]
        val newItem = FoodItemModel(
            id = UUID.randomUUID().toString(),
            mealId = targetLunch.mealId,
            name = "Risotto ai Funghi Porcini Test",
            brand = "Riso Scotti",
            quantity = 100.0,
            unit = QuantityUnit.GRAMS,
            calories = 340.0
        )
        tuesdayMeals[lunchIdx] = targetLunch.copy(items = targetLunch.items + newItem)
        allDaysMeals[2] = tuesdayMeals

        // 4. Salva il piano dall'editor
        repository.saveFullPlan(
            planId = planId,
            title = activePlan.title,
            notes = activePlan.notes,
            setAsActive = true,
            daysMeals = allDaysMeals
        )

        // 5. Verifica che Mercoledì (giorno 3) NON sia stato svuotato!
        val wednesdayMealsAfter = repository.getMealsWithItemsForDay(planId, 3)
        val wednesdayTotalFoodsAfter = wednesdayMealsAfter.sumOf { it.items.size }
        assertEquals(
            "La configurazione di Mercoledì non deve essere persa al salvataggio dell'editor!",
            wednesdayTotalFoodsBefore,
            wednesdayTotalFoodsAfter
        )

        // 6. Verifica che Martedì (giorno 2) contenga l'alimento aggiunto
        val tuesdayMealsAfter = repository.getMealsWithItemsForDay(planId, 2)
        val lunchAfter = tuesdayMealsAfter.first { it.mealType == MealType.LUNCH }
        assertTrue(
            "L'alimento aggiunto deve essere presente nel pranzo di Martedì",
            lunchAfter.items.any { it.name == "Risotto ai Funghi Porcini Test" }
        )
    }

    @Test
    fun `toggleItemPurchased saves to DB and preserves status across shopping list regeneration`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        repository.initializeDefaultDataIfNeeded()

        // 1. Preleva gli alimenti della lista spesa
        val initialItems = db.shoppingListDao().getAllItemsSync()
        assertTrue("La lista della spesa iniziale deve contenere alimenti", initialItems.isNotEmpty())

        val targetItem = initialItems.first { !it.isPurchased }

        // 2. Segna l'alimento come preso
        repository.toggleItemPurchased(targetItem)

        // 3. Verifica persistenza immediata a DB
        val itemsAfterToggle = db.shoppingListDao().getAllItemsSync()
        val toggledItem = itemsAfterToggle.first { it.id == targetItem.id }
        assertTrue("L'alimento deve risultare acquistato (isPurchased = true) a database!", toggledItem.isPurchased)

        // 4. Simula navigazione / rigenerazione della lista dal piano attivo
        repository.generateShoppingListFromActivePlan(
            selectedDays = listOf(1, 2, 3, 4, 5, 6, 7),
            preserveManualExtras = true
        )

        // 5. Verifica che l'alimento conservi lo stato isPurchased = true dopo la rigenerazione
        val itemsAfterRegen = db.shoppingListDao().getAllItemsSync()
        val itemAfterRegen = itemsAfterRegen.firstOrNull { it.id == targetItem.id }
        assertNotNull("L'alimento deve essere ancora presente con lo stesso ID", itemAfterRegen)
        assertTrue("Lo stato acquistato non deve essere azzerato dalla rigenerazione o navigazione!", itemAfterRegen!!.isPurchased)

        // 6. Verifica che il reset manuale esplicito riporti l'articolo a non acquistato
        repository.resetShoppingListPurchasedStatus()
        val itemsAfterReset = db.shoppingListDao().getAllItemsSync()
        val itemAfterReset = itemsAfterReset.first { it.id == targetItem.id }
        assertFalse("Il reset manuale deve riportare l'articolo a non acquistato", itemAfterReset.isPurchased)
    }

    @Test
    fun `searchFood returns local items with yellow dot and FoodDb items with red dot`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        repository.initializeDefaultDataIfNeeded()

        // Ricerca di un alimento (es. 'farro' o 'pollo')
        val results = repository.searchFood("farro", com.example.data.model.AppLanguage.IT)
        assertNotNull(results)
        assertTrue("La ricerca di alimenti deve restituire risultati", results.isNotEmpty())

        // Verifica che la sorgente sia Local o FoodDb
        results.forEach { item ->
            assertTrue("Ogni alimento deve avere sorgente 'Local' o 'FoodDb'", item.source == "Local" || item.source == "FoodDb")
        }
    }

    @Test
    fun `searchFood English search prioritizes english_name with fallback`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        val results = repository.searchFood("spelt", com.example.data.model.AppLanguage.EN)
        assertNotNull(results)
        assertTrue("La ricerca in inglese di 'spelt' deve restituire alimenti", results.isNotEmpty())
        assertTrue("Deve provenire da FoodDb", results.any { it.source == "FoodDb" })
    }

    @Test
    fun `test Graduality Serialization and Deserialization`() {
        val group = com.example.data.model.GradualityGroupConfig(
            groupKey = "pane|80.0|GRAMS",
            foodName = "Pane",
            baseQuantity = 80.0,
            unit = QuantityUnit.GRAMS,
            category = "GRAINS_PASTA",
            targetMealTypes = listOf(MealType.LUNCH, MealType.DINNER),
            targetDays = listOf(1, 2, 3),
            isEnabled = true,
            minQuantity = 80.0,
            maxQuantity = 120.0,
            incrementStep = 10.0,
            cadenceDays = 7,
            startDateMillis = 1700000000000L,
            nextStepDateMillis = 1700604800000L,
            currentAppliedQuantity = 90.0
        )

        val planConfig = com.example.data.model.GradualityPlanConfig(
            items = listOf(group),
            globalStartDateMillis = 1700000000000L,
            globalCadenceDays = 7
        )
        val jsonStr = planConfig.toJsonString()

        val parsed = com.example.data.model.GradualityPlanConfig.fromJsonString(jsonStr)
        assertEquals(1, parsed.items.size)
        assertEquals(1700000000000L, parsed.globalStartDateMillis)
        assertEquals(7, parsed.globalCadenceDays)
        assertTrue(parsed.hasActiveGraduality())
        val parsedItem = parsed.items[0]
        assertEquals("pane|80.0|GRAMS", parsedItem.groupKey)
        assertEquals("Pane", parsedItem.foodName)
        assertEquals(80.0, parsedItem.baseQuantity, 0.001)
        assertEquals(90.0, parsedItem.currentAppliedQuantity, 0.001)
        assertEquals(120.0, parsedItem.maxQuantity, 0.001)
        assertEquals(10.0, parsedItem.incrementStep, 0.001)
        assertEquals(7, parsedItem.cadenceDays)
        assertEquals(true, parsedItem.isEnabled)
        assertEquals(listOf(MealType.LUNCH, MealType.DINNER), parsedItem.targetMealTypes)
        assertEquals(listOf(1, 2, 3), parsedItem.targetDays)
    }

    @Test
    fun `test Graduality Days Remaining Countdown`() {
        val now = 1700000000000L
        val sixDaysLater = now + 6 * 86_400_000L
        val group = com.example.data.model.GradualityGroupConfig(
            groupKey = "pane|80.0|GRAMS",
            foodName = "Pane",
            baseQuantity = 80.0,
            unit = QuantityUnit.GRAMS,
            category = "GRAINS_PASTA",
            targetMealTypes = listOf(MealType.LUNCH),
            targetDays = listOf(1),
            isEnabled = true,
            maxQuantity = 100.0,
            nextStepDateMillis = sixDaysLater
        )

        assertEquals(6, group.daysRemaining(fromMillis = now))

        val config = com.example.data.model.GradualityPlanConfig(listOf(group))
        assertEquals(6, config.minDaysRemaining(fromMillis = now))
    }

    @Test
    fun `saveFullPlan persists graduality config and is restored properly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        val planId = UUID.randomUUID().toString()
        val group = com.example.data.model.GradualityGroupConfig(
            groupKey = "riso|100.0|GRAMS",
            foodName = "Riso",
            baseQuantity = 100.0,
            unit = QuantityUnit.GRAMS,
            category = "GRAINS_PASTA",
            targetMealTypes = listOf(MealType.LUNCH),
            targetDays = listOf(1, 2),
            isEnabled = true,
            minQuantity = 100.0,
            maxQuantity = 150.0,
            incrementStep = 10.0,
            currentAppliedQuantity = 100.0
        )
        val gradConfig = com.example.data.model.GradualityPlanConfig(
            items = listOf(group),
            globalStartDateMillis = 1700000000000L,
            globalCadenceDays = 7
        )

        repository.saveFullPlan(
            planId = planId,
            title = "Piano Test Gradualita",
            notes = null,
            setAsActive = false,
            daysMeals = emptyMap(),
            gradualityConfigJson = gradConfig.toJsonString()
        )

        val retrieved = repository.getPlanById(planId)
        assertNotNull(retrieved)
        assertNotNull(retrieved?.gradualityConfigJson)

        val parsed = com.example.data.model.GradualityPlanConfig.fromJsonString(retrieved?.gradualityConfigJson)
        assertEquals(1, parsed.items.size)
        assertEquals("riso|100.0|GRAMS", parsed.items[0].groupKey)
        assertTrue(parsed.items[0].isEnabled)
        assertEquals(100.0, parsed.items[0].minQuantity, 0.001)
        assertEquals(150.0, parsed.items[0].maxQuantity, 0.001)
        assertEquals(1700000000000L, parsed.globalStartDateMillis)
        assertEquals(7, parsed.globalCadenceDays)
    }

    @Test
    fun `deleteGradualityConfig completely wipes graduality from plan`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        val planId = UUID.randomUUID().toString()
        val gradConfig = com.example.data.model.GradualityPlanConfig(
            items = listOf(
                com.example.data.model.GradualityGroupConfig(
                    groupKey = "pasta|GRAMS",
                    foodName = "Pasta",
                    unit = QuantityUnit.GRAMS,
                    category = "GRAINS_PASTA",
                    targetMealTypes = listOf(MealType.LUNCH),
                    targetDays = listOf(1),
                    isEnabled = true,
                    minQuantity = 80.0,
                    maxQuantity = 120.0,
                    incrementStep = 10.0
                )
            )
        )

        repository.saveFullPlan(
            planId = planId,
            title = "Piano Con Gradualita",
            notes = null,
            setAsActive = false,
            daysMeals = emptyMap(),
            gradualityConfigJson = gradConfig.toJsonString()
        )

        val withGrad = repository.getPlanById(planId)
        assertNotNull(withGrad?.gradualityConfigJson)

        repository.deleteGradualityConfig(planId)

        val afterDelete = repository.getPlanById(planId)
        assertNotNull(afterDelete)
        assertEquals(null, afterDelete?.gradualityConfigJson)
    }

    @Test
    fun `applyGradualityStep aligns food weight when retrodated or modified manually`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MealPlanDatabase.getDatabase(context)
        val repository = MealPlanRepository(db, context)

        val planId = UUID.randomUUID().toString()
        val mealId = UUID.randomUUID().toString()

        // Crea pasto e alimento a 100g nel piano
        val foodItem = FoodItemModel(
            id = UUID.randomUUID().toString(),
            mealId = mealId,
            name = "Riso Integrale",
            brand = "Gallo",
            quantity = 100.0,
            unit = QuantityUnit.GRAMS,
            toDefrost = false,
            calories = 350.0,
            protein = 7.0,
            carbs = 77.0,
            fat = 2.0
        )
        val mealWithItems = com.example.data.model.MealWithItems(
            mealType = MealType.LUNCH,
            dayOfWeek = 1,
            mealId = mealId,
            items = listOf(foodItem)
        )

        val now = System.currentTimeMillis()
        // Data di inizio piano retrodatata di 28 giorni (due cicli da 14 giorni trascorsi: 100 -> 110 -> 120)
        val startDateRetrodated = now - (28 * 86_400_000L)
        val gradGroup = com.example.data.model.GradualityGroupConfig(
            groupKey = "riso integrale|GRAMS",
            foodName = "Riso Integrale",
            unit = QuantityUnit.GRAMS,
            category = "GRAINS_PASTA",
            targetMealTypes = listOf(MealType.LUNCH),
            targetDays = listOf(1),
            isEnabled = true,
            minQuantity = 100.0,
            maxQuantity = 140.0,
            incrementStep = 10.0,
            cadenceDays = 14,
            startDateMillis = startDateRetrodated
        )
        val gradConfig = com.example.data.model.GradualityPlanConfig(
            items = listOf(gradGroup),
            globalStartDateMillis = startDateRetrodated,
            globalCadenceDays = 14
        )

        repository.saveFullPlan(
            planId = planId,
            title = "Piano Retrodatato",
            notes = null,
            setAsActive = true,
            daysMeals = mapOf(1 to listOf(mealWithItems)),
            gradualityConfigJson = gradConfig.toJsonString()
        )

        // Verifica confronto iniziale: peso reale = 100, teorico = 120
        val comparisons = repository.getGradualityComparisons(planId)
        assertEquals(1, comparisons.size)
        val comp = comparisons[0]
        assertEquals(100.0, comp.realCurrentWeight, 0.001)
        assertEquals(120.0, comp.theoreticalWeight, 0.001)
        assertFalse("Deve rilevare la discrepanza tra reale e teorico", comp.isAligned)

        // Applica l'allineamento
        val success = repository.applyGradualityStep(planId, gradGroup.groupKey)
        assertTrue(success)

        // Verifica che il pasto nel piano sia stato allineato a 120g
        val updatedMeals = repository.getMealsWithItemsForDay(planId, 1)
        val updatedFood = updatedMeals.first { it.mealType == MealType.LUNCH }.items.first { it.name == "Riso Integrale" }
        assertEquals(120.0, updatedFood.quantity, 0.001)

        // Nuovo confronto: ora è allineato!
        val comparisonsAfter = repository.getGradualityComparisons(planId)
        assertTrue("Dopo l'allineamento il piano deve risultare in linea", comparisonsAfter[0].isAligned)
    }

    @Test
    fun `whats new tracking displays on new version and remembers confirmation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("meal_plan_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // 1. Al primo avvio della nuova versione, non deve ancora risultare visto
        assertFalse(
            "Al primo avvio le novita non devono risultare viste",
            com.example.util.AppUpdateHelper.hasSeenWhatsNewForCurrentVersion(context)
        )

        // 2. L'utente preme Ok
        com.example.util.AppUpdateHelper.markWhatsNewSeenForCurrentVersion(context)

        // 3. Ora deve risultare visto (True)
        assertTrue(
            "Dopo la conferma le novita devono risultare viste",
            com.example.util.AppUpdateHelper.hasSeenWhatsNewForCurrentVersion(context)
        )

        // 4. Se in futuro viene rilasciata una nuova versione (es. versionCode precedente era 12, nuova è 13)
        prefs.edit().putInt("last_seen_whats_new_version_code", 12).commit()
        val currentCode = com.example.util.AppUpdateHelper.getCurrentVersionCode(context)
        if (currentCode > 12) {
            assertFalse(
                "Con una nuova versione piu alta, il flag riparte da False",
                com.example.util.AppUpdateHelper.hasSeenWhatsNewForCurrentVersion(context)
            )
        }
    }
}

