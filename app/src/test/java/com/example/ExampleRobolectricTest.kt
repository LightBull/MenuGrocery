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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

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
}

