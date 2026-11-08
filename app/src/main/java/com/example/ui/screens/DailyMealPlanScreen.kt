package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.DefrostDiagnosticsBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItemModel
import com.example.data.model.MealWithItems
import com.example.data.model.QuantityUnit
import com.example.ui.components.DailyNutritionalCard
import com.example.ui.components.DaySelectorRow
import com.example.ui.components.DefrostBadge
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.viewmodel.MealPlanViewModel

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Schermata Piano Pasti: ora interamente di sola lettura per una consultazione rapida e pulita.
 * L'aggiunta e la modifica dei pasti avvengono all'interno della sezione "I Miei Piani".
 * Supporta la navigazione tra i giorni con swipe orizzontale a destra e sinistra.
 * Scorre automaticamente al prossimo pasto da consumare se a 2 ore o meno di distanza dall'orario consigliato.
 */
@Composable
fun DailyMealPlanScreen(
    viewModel: MealPlanViewModel,
    onNavigateToPlans: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePlan by viewModel.activePlan.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val mealsForDay by viewModel.mealsForSelectedDay.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    val listState = rememberLazyListState()
    var totalDragX by remember { mutableFloatStateOf(0f) }

    // Auto-scroll al prossimo pasto da consumare se entro 2 ore (<= 120 minuti) dall'orario tipico
    LaunchedEffect(mealsForDay) {
        if (mealsForDay.isNotEmpty()) {
            val now = LocalTime.now()
            var closestMealIndex = -1
            var minDiffMinutes = Long.MAX_VALUE

            // Gli item fissi prima dei pasti nella LazyColumn sono 3 (Intestazione piano = 0, DaySelectorRow = 1, DailyNutritionalCard = 2)
            mealsForDay.forEachIndexed { index, mealWithItems ->
                try {
                    val mealTime = LocalTime.parse(mealWithItems.mealType.typicalTime)
                    val diff = abs(ChronoUnit.MINUTES.between(now, mealTime))
                    // Pasto entro 2 ore (120 minuti)
                    if (diff <= 120 && diff < minDiffMinutes) {
                        minDiffMinutes = diff
                        closestMealIndex = index
                    }
                } catch (_: Exception) {}
            }

            if (closestMealIndex != -1) {
                // Indice del pasto nella LazyColumn (3 header items + closestMealIndex)
                val targetIndex = 3 + closestMealIndex
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(selectedDay) {
                val swipeThreshold = 50.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX < -swipeThreshold) {
                            // Swipe a sinistra -> passa al giorno successivo
                            if (selectedDay < 7) {
                                viewModel.selectDay(selectedDay + 1)
                            }
                        } else if (totalDragX > swipeThreshold) {
                            // Swipe a destra -> passa al giorno precedente
                            if (selectedDay > 1) {
                                viewModel.selectDay(selectedDay - 1)
                            }
                        }
                        totalDragX = 0f
                    },
                    onDragCancel = { totalDragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount
                    }
                )
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
        // Intestazione del Piano Attivo con possibilità di cambiarlo
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SagePrimaryContainer
                        ) {
                            Text(
                                text = com.example.ui.i18n.AppStrings.activePlanHeader(currentLanguage),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.ui.i18n.AppStrings.translatePlanTitle(
                                activePlan?.title ?: com.example.ui.i18n.AppStrings.noActivePlan(currentLanguage),
                                currentLanguage
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val planNotes = com.example.ui.i18n.AppStrings.translatePlanNotes(activePlan?.notes, currentLanguage)
                        if (!planNotes.isNullOrBlank()) {
                            Text(
                                text = planNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToPlans,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_switch_plan")
                    ) {
                        Text(com.example.ui.i18n.AppStrings.changePlan(currentLanguage), fontSize = 12.sp)
                    }
                }
            }
        }

        // Barra di selezione giorni della settimana (Lunedì - Domenica)
        item {
            DaySelectorRow(
                selectedDay = selectedDay,
                onDaySelected = { viewModel.selectDay(it) },
                lang = currentLanguage
            )
        }

        // Scheda riepilogo macronutrienti stimati del giorno
        item {
            DailyNutritionalCard(
                summary = dailySummary,
                lang = currentLanguage
            )
        }

        // 5 Sezioni Pasti di SOLA LETTURA
        items(mealsForDay) { mealWithItems ->
            ReadOnlyMealSectionCard(
                mealWithItems = mealWithItems,
                lang = currentLanguage,
                onTestDefrostClick = { item ->
                    viewModel.testDefrostNotification(item.name, mealWithItems.mealType)
                }
            )
        }
    }
}
}

@Composable
fun ReadOnlyMealSectionCard(
    mealWithItems: MealWithItems,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onTestDefrostClick: (FoodItemModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("meal_section_${mealWithItems.mealType.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Intestazione del pasto con orario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = SagePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = mealWithItems.mealType.getLocalizedName(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = com.example.ui.i18n.AppStrings.recommendedTime(mealWithItems.mealType.typicalTime, lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Elenco cibi del pasto in sola lettura
            if (mealWithItems.items.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = com.example.ui.i18n.AppStrings.noFoodPlannedInMeal(lang),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealWithItems.items.forEach { item ->
                        ReadOnlyFoodItemRow(
                            item = item,
                            lang = lang,
                            onTestDefrost = { onTestDefrostClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadOnlyFoodItemRow(
    item: FoodItemModel,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onTestDefrost: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format("%.1f", item.quantity)
                    Text(
                        text = com.example.ui.i18n.AppStrings.translateFoodName(item.name, lang),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($qtyStr ${item.unit.getLocalizedShortName(lang)})",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!item.brand.isNullOrBlank()) {
                    Text(
                        text = item.brand,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Badge nutrizionali calcolati e Categoria
                val factor = if (item.unit == QuantityUnit.GRAMS) item.quantity / 100.0 else item.quantity
                val itemCalories = (item.calories * factor).toInt()
                val itemProt = String.format("%.1f", item.protein * factor)
                val itemCarb = String.format("%.1f", item.carbs * factor)
                val itemFat = String.format("%.1f", item.fat * factor)
                val category = FoodCategory.fromCategoryString(item.category) ?: FoodCategory.categorize(item.name)

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$itemCalories kcal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SagePrimary)
                    Text("P: ${itemProt}g", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("C: ${itemCarb}g", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("G: ${itemFat}g", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${category.iconEmoji} ${category.getLocalizedName(lang)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Flag scongelamento
                if (item.toDefrost) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DefrostBadge(onClick = onTestDefrost)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onTestDefrost,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = com.example.ui.i18n.AppStrings.testDefrostNotification(lang),
                                tint = CartaDaZucchero,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


