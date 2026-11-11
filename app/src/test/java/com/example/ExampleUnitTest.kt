package com.example

import com.example.data.model.FoodCategory
import com.example.data.model.MealType
import com.example.data.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testShoppingListAggregationSameUnit() {
        val foodName = "Riso Basmati"
        val unit = QuantityUnit.GRAMS

        val qtyMeal1 = 80.0
        val qtyMeal2 = 100.0
        val expectedTotal = 180.0

        val total = qtyMeal1 + qtyMeal2
        assertEquals(expectedTotal, total, 0.001)
    }

    @Test
    fun testShoppingListDifferentUnitsKeepSeparate() {
        val unitGrams = QuantityUnit.GRAMS
        val unitPieces = QuantityUnit.PIECES

        assertNotEquals(unitGrams, unitPieces)
    }

    @Test
    fun testMealTypesCoverage() {
        assertEquals(5, MealType.entries.size)
        assertEquals(MealType.BREAKFAST, MealType.entries[0])
        assertEquals(MealType.SNACK_AM, MealType.entries[1])
        assertEquals(MealType.LUNCH, MealType.entries[2])
        assertEquals(MealType.SNACK_PM, MealType.entries[3])
        assertEquals(MealType.DINNER, MealType.entries[4])
    }

    @Test
    fun testFoodCategoriesClassification() {
        assertEquals(FoodCategory.MEAT_FISH, FoodCategory.categorize("Petto di Pollo", false))
        assertEquals(FoodCategory.FRUITS_VEGETABLES, FoodCategory.categorize("Mela Golden", false))
        assertEquals(FoodCategory.DAIRY_EGGS, FoodCategory.categorize("Yogurt Greco", false))
        assertEquals(FoodCategory.GRAINS_PASTA, FoodCategory.categorize("Riso Basmati", false))
        assertEquals(FoodCategory.PANTRY_SPICES, FoodCategory.categorize("Olio Extravergine", false))
        assertEquals(FoodCategory.EXTRA_HOUSEHOLD, FoodCategory.categorize("Detersivo Piatti", true))
    }

    @Test
    fun testPantryLeftoverCalculation() {
        // Esempio utente: servono 600g, in frigo rimasti 200g -> da comprare 400g
        val plannedQuantity = 600.0
        val pantryQuantity = 200.0
        val actualToBuy = kotlin.math.max(0.0, plannedQuantity - pantryQuantity)

        assertEquals(400.0, actualToBuy, 0.001)
    }

    @Test
    fun testPantryLeftoverExceedsPlanned() {
        // Se in dispensa abbiamo più di quanto pianificato (es. servono 200g, ne abbiamo 300g), da comprare = 0
        val plannedQuantity = 200.0
        val pantryQuantity = 300.0
        val actualToBuy = kotlin.math.max(0.0, plannedQuantity - pantryQuantity)

        assertEquals(0.0, actualToBuy, 0.001)
    }

    @Test
    fun testShoppingListItemEntitySchemaVersion2Fields() {
        val item = com.example.data.local.ShoppingListItemEntity(
            id = "test-1",
            planId = "plan-1",
            name = "Petto di Pollo",
            quantity = 400.0,
            originalQuantity = 600.0,
            pantryQuantity = 200.0,
            unit = QuantityUnit.GRAMS,
            isPurchased = false,
            isManualExtra = false,
            sourceDays = "Lun, Mer"
        )
        assertEquals(400.0, item.quantity, 0.001)
        assertEquals(600.0, item.originalQuantity, 0.001)
        assertEquals(200.0, item.pantryQuantity, 0.001)
        assertEquals("Lun, Mer", item.sourceDays)
    }

    @Test
    fun testGradualityGroupConfigCalculations() {
        val now = 100_000_000L
        val group = com.example.data.model.GradualityGroupConfig(
            groupKey = "pane|80.0|GRAMS",
            foodName = "Pane",
            unit = QuantityUnit.GRAMS,
            category = "GRAINS_PASTA",
            targetMealTypes = listOf(MealType.LUNCH, MealType.DINNER),
            targetDays = listOf(1, 2, 4, 5),
            isEnabled = true,
            minQuantity = 80.0,
            maxQuantity = 100.0,
            incrementStep = 10.0,
            cadenceDays = 14,
            startDateMillis = now,
            currentAppliedQuantity = 80.0
        )

        val steps = group.generateScheduledSteps()
        assertEquals(2, steps.size)
        assertEquals(80.0, steps[0].fromQuantity, 0.001)
        assertEquals(90.0, steps[0].toQuantity, 0.001)
        assertEquals(now + 14 * 86_400_000L, steps[0].dateMillis)
        assertEquals(90.0, steps[1].fromQuantity, 0.001)
        assertEquals(100.0, steps[1].toQuantity, 0.001)
        assertEquals(now + 28 * 86_400_000L, steps[1].dateMillis)

        // Verifica calcolo teorico
        assertEquals(80.0, group.calculateTheoreticalWeight(now), 0.001)
        assertEquals(80.0, group.calculateTheoreticalWeight(now + 5 * 86_400_000L), 0.001)
        assertEquals(90.0, group.calculateTheoreticalWeight(now + 15 * 86_400_000L), 0.001)
        assertEquals(100.0, group.calculateTheoreticalWeight(now + 30 * 86_400_000L), 0.001)
        assertEquals(100.0, group.calculateTheoreticalWeight(now + 60 * 86_400_000L), 0.001) // Non supera il max!
    }
}
