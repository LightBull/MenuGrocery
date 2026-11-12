package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.example.data.model.GradualityPlanConfig
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.DisposableEffect
import kotlin.math.roundToInt
import com.example.data.model.DayOfWeekInfo
import com.example.data.model.FoodItemModel
import com.example.data.model.MealType
import com.example.data.model.MealWithItems
import com.example.data.model.FoodCategory
import com.example.data.model.QuantityUnit
import com.example.ui.components.CategorySelectorDropdown
import com.example.ui.components.CopyDayDialog
import com.example.ui.components.CopyFoodItemDialog
import com.example.ui.components.CopyMealDialog
import com.example.ui.components.StickyDaySelectorBar
import com.example.ui.components.DefrostBadge
import com.example.ui.components.FoodSourceDot
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.CartaDaZuccheroContainer
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.TerracottaAccent
import com.example.ui.viewmodel.MealPlanViewModel
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    planId: String?, // null se nuovo piano
    initialTitle: String = "",
    initialNotes: String? = null,
    initialIsActive: Boolean = false,
    initialGradualityConfigJson: String? = null,
    viewModel: MealPlanViewModel,
    onBack: () -> Unit,
    onNavigateToTab: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isDirty by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var stickyHeaderHeightPx by remember { mutableIntStateOf(0) }
    val isDaysBarPinned by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 1 }
    }

    val pendingNavigationTab by viewModel.pendingNavigationTab.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // Sincronizza lo stato di modifiche non salvate con il ViewModel
    LaunchedEffect(isDirty) {
        viewModel.setEditorDirty(isDirty)
    }

    DisposableEffect(Unit) {
        viewModel.setPlanEditorOpen(true)
        onDispose {
            viewModel.setPlanEditorOpen(false)
            viewModel.setEditorDirty(false)
            viewModel.clearPendingNavigationTab()
        }
    }

    // Intercetta reindirizzamento di navigazione proveniente dalla barra dei menu/tab
    LaunchedEffect(pendingNavigationTab) {
        val targetTab = pendingNavigationTab
        if (targetTab != null) {
            if (isDirty) {
                showUnsavedDialog = true
            } else {
                viewModel.clearPendingNavigationTab()
                if (onNavigateToTab != null) {
                    onNavigateToTab(targetTab)
                } else {
                    onBack()
                }
            }
        }
    }

    fun handleBackAttempt() {
        if (isDirty) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler { handleBackAttempt() }

    val resolvedPlanId = remember { planId ?: UUID.randomUUID().toString() }
    var planTitle by remember { mutableStateOf(initialTitle) }
    var planNotes by remember { mutableStateOf(initialNotes ?: "") }
    var setAsActive by remember { mutableStateOf(initialIsActive || planId == null) }
    var selectedDay by rememberSaveable { mutableIntStateOf(1) }

    // Mappa dei pasti per giorno (1..7 -> List<MealWithItems>)
    val daysMealsMap = remember { mutableStateMapOf<Int, List<MealWithItems>>() }
    var isLoaded by remember { mutableStateOf(false) }

    // Quale pasto ha attualmente il form di aggiunta inline aperto (null = nessuno)
    var inlineAddMealType by remember { mutableStateOf<MealType?>(null) }
    // Pasto correntemente attivo: attivato alla prima pressione di aggiunta, mantenuto finché non si cambia pasto
    var activeMealType by remember { mutableStateOf<MealType?>(null) }
    // Alimento attualmente in fase di modifica: Triple(dayOfWeek, mealType, foodItem)
    var editingFoodTarget by remember { mutableStateOf<Triple<Int, MealType, FoodItemModel>?>(null) }
    // Alimento per la copia multi-giorno
    var copyingFoodTarget by remember { mutableStateOf<FoodItemModel?>(null) }
    // Alimento per la conferma eliminazione via swipe a sinistra
    var deletingFoodTarget by remember { mutableStateOf<Triple<Int, MealType, FoodItemModel>?>(null) }
    // Dialog copia intero giorno
    var showCopyDayDialog by remember { mutableStateOf(false) }
    // Dialog copia intero pasto (Target Meal)
    var copyingMealTarget by remember { mutableStateOf<MealWithItems?>(null) }
    // Configurazione gradualità del piano
    var gradualityConfig by remember(initialGradualityConfigJson) {
        mutableStateOf(GradualityPlanConfig.fromJsonString(initialGradualityConfigJson))
    }
    var showGradualityScreen by remember { mutableStateOf(false) }

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Carica i pasti esistenti se stiamo modificando un piano, altrimenti crea la struttura vuota
    LaunchedEffect(resolvedPlanId) {
        if (planId != null) {
            val loadedMap = viewModel.loadPlanWithAllDaysMeals(planId)
            loadedMap.forEach { (day, meals) ->
                daysMealsMap[day] = meals
            }
            val existing = viewModel.getPlanById(planId)
            if (!existing?.gradualityConfigJson.isNullOrBlank()) {
                gradualityConfig = GradualityPlanConfig.fromJsonString(existing.gradualityConfigJson)
            }
        } else {
            for (day in 1..7) {
                daysMealsMap[day] = MealType.entries.map { type ->
                    MealWithItems(
                        mealType = type,
                        dayOfWeek = day,
                        mealId = UUID.randomUUID().toString(),
                        items = emptyList()
                    )
                }
            }
        }
        isLoaded = true
    }

    if (showGradualityScreen) {
        GradualityManagementScreen(
            daysMeals = daysMealsMap.toMap(),
            initialConfig = gradualityConfig,
            lang = currentLanguage,
            onBack = { showGradualityScreen = false },
            onSave = { updated ->
                gradualityConfig = updated
                isDirty = true
                showGradualityScreen = false
            },
            onDeleteGraduality = {
                gradualityConfig = GradualityPlanConfig(emptyList())
                isDirty = true
                showGradualityScreen = false
            }
        )
        return
    }

    // Dialog di conferma salvataggio modifiche all'uscita accidentale o cambio menu
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = {
                showUnsavedDialog = false
                viewModel.clearPendingNavigationTab()
            },
            title = {
                Text(
                    text = com.example.ui.i18n.AppStrings.unsavedChangesTitle(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = com.example.ui.i18n.AppStrings.unsavedChangesMessage(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        val targetTab = viewModel.pendingNavigationTab.value
                        viewModel.clearPendingNavigationTab()

                        if (planTitle.isNotBlank() && isLoaded) {
                            viewModel.saveFullPlan(
                                planId = resolvedPlanId,
                                title = planTitle,
                                notes = planNotes,
                                setAsActive = setAsActive,
                                daysMeals = daysMealsMap.toMap(),
                                gradualityConfigJson = if (gradualityConfig.hasActiveGraduality()) gradualityConfig.toJsonString() else null,
                                onComplete = {
                                    isDirty = false
                                    viewModel.setEditorDirty(false)
                                    if (targetTab != null && onNavigateToTab != null) {
                                        onNavigateToTab(targetTab)
                                    } else {
                                        onBack()
                                    }
                                }
                            )
                        } else {
                            isDirty = false
                            viewModel.setEditorDirty(false)
                            if (targetTab != null && onNavigateToTab != null) {
                                onNavigateToTab(targetTab)
                            } else {
                                onBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier.testTag("btn_unsaved_save_and_exit")
                ) {
                    Text(com.example.ui.i18n.AppStrings.saveAndExit(currentLanguage), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            viewModel.clearPendingNavigationTab()
                        },
                        modifier = Modifier.testTag("btn_unsaved_cancel")
                    ) {
                        Text(com.example.ui.i18n.AppStrings.cancel(currentLanguage))
                    }
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            val targetTab = viewModel.pendingNavigationTab.value
                            viewModel.clearPendingNavigationTab()
                            isDirty = false
                            viewModel.setEditorDirty(false)
                            if (targetTab != null && onNavigateToTab != null) {
                                onNavigateToTab(targetTab)
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("btn_unsaved_discard")
                    ) {
                        Text(com.example.ui.i18n.AppStrings.exitWithoutSaving(currentLanguage), color = TerracottaAccent)
                    }
                }
            }
        )
    }

    // Dialog Copia Intero Giorno
    if (showCopyDayDialog) {
        val currentDayName = DayOfWeekInfo.ALL_DAYS.firstOrNull { it.dayIndex == selectedDay }?.getLocalizedFullName(currentLanguage) ?: "Giorno $selectedDay"
        val currentDayMeals = daysMealsMap[selectedDay] ?: emptyList()
        val totalFoodItemsInDay = currentDayMeals.sumOf { it.items.size }

        CopyDayDialog(
            sourceDayIndex = selectedDay,
            sourceDayName = currentDayName,
            totalItemsCount = totalFoodItemsInDay,
            lang = currentLanguage,
            onDismiss = { showCopyDayDialog = false },
            onConfirmCopy = { targetDays ->
                showCopyDayDialog = false
                if (targetDays.isNotEmpty() && currentDayMeals.isNotEmpty()) {
                    targetDays.forEach { targetDay ->
                        val targetMeals = (daysMealsMap[targetDay] ?: MealType.entries.map { type ->
                            MealWithItems(mealType = type, dayOfWeek = targetDay, mealId = UUID.randomUUID().toString(), items = emptyList())
                        }).toMutableList()

                        currentDayMeals.forEach { sourceMeal ->
                            if (sourceMeal.items.isNotEmpty()) {
                                val mealIdx = targetMeals.indexOfFirst { it.mealType == sourceMeal.mealType }
                                val clonedItems = sourceMeal.items.map { item ->
                                    item.copy(id = UUID.randomUUID().toString())
                                }
                                if (mealIdx != -1) {
                                    val existingMeal = targetMeals[mealIdx]
                                    targetMeals[mealIdx] = existingMeal.copy(items = existingMeal.items + clonedItems)
                                } else {
                                    targetMeals.add(
                                        MealWithItems(
                                            mealType = sourceMeal.mealType,
                                            dayOfWeek = targetDay,
                                            mealId = UUID.randomUUID().toString(),
                                            items = clonedItems
                                        )
                                    )
                                }
                            }
                        }
                        daysMealsMap[targetDay] = targetMeals
                    }
                    isDirty = true
                    viewModel.setEditorDirty(true)
                    val feedback = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                        "Giorno $currentDayName copiato con successo su ${targetDays.size} giorno/i!"
                    else
                        "Day $currentDayName successfully copied to ${targetDays.size} day(s)!"
                    viewModel.postFeedbackMessage(feedback)
                }
            }
        )
    }

    // Dialog Copia Intero Pasto
    copyingMealTarget?.let { mealTarget ->
        val currentDayName = DayOfWeekInfo.ALL_DAYS.firstOrNull { it.dayIndex == selectedDay }?.getLocalizedFullName(currentLanguage) ?: "Giorno $selectedDay"
        CopyMealDialog(
            mealName = mealTarget.mealType.getLocalizedName(currentLanguage),
            dayName = currentDayName,
            totalItemsCount = mealTarget.items.size,
            initialMealType = mealTarget.mealType,
            lang = currentLanguage,
            onDismiss = { copyingMealTarget = null },
            onConfirmCopy = { targetDays, targetMealTypes ->
                copyingMealTarget = null
                if (targetDays.isNotEmpty() && targetMealTypes.isNotEmpty() && mealTarget.items.isNotEmpty()) {
                    targetDays.forEach { targetDay ->
                        val targetMeals = (daysMealsMap[targetDay] ?: MealType.entries.map { type ->
                            MealWithItems(mealType = type, dayOfWeek = targetDay, mealId = UUID.randomUUID().toString(), items = emptyList())
                        }).toMutableList()

                        targetMealTypes.forEach { targetType ->
                            val clonedItems = mealTarget.items.map { item ->
                                item.copy(id = UUID.randomUUID().toString())
                            }
                            val mealIdx = targetMeals.indexOfFirst { it.mealType == targetType }
                            if (mealIdx != -1) {
                                val existingMeal = targetMeals[mealIdx]
                                targetMeals[mealIdx] = existingMeal.copy(items = existingMeal.items + clonedItems)
                            } else {
                                targetMeals.add(
                                    MealWithItems(
                                        mealType = targetType,
                                        dayOfWeek = targetDay,
                                        mealId = UUID.randomUUID().toString(),
                                        items = clonedItems
                                    )
                                )
                            }
                        }
                        daysMealsMap[targetDay] = targetMeals
                    }
                    isDirty = true
                    viewModel.setEditorDirty(true)
                    val feedback = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                        "${mealTarget.mealType.getLocalizedName(currentLanguage)} copiato con successo!"
                    else
                        "${mealTarget.mealType.getLocalizedName(currentLanguage)} successfully copied!"
                    viewModel.postFeedbackMessage(feedback)
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar con Salva e Annulla
        TopAppBar(
            title = {
                Text(
                    text = if (planId != null) com.example.ui.i18n.AppStrings.editPlanHeaderTitle(currentLanguage) else com.example.ui.i18n.AppStrings.btnNewPlan(currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { handleBackAttempt() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = com.example.ui.i18n.AppStrings.back(currentLanguage))
                }
            },
            actions = {
                Button(
                    onClick = {
                        if (planTitle.isNotBlank() && isLoaded) {
                            viewModel.saveFullPlan(
                                planId = resolvedPlanId,
                                title = planTitle,
                                notes = planNotes,
                                setAsActive = setAsActive,
                                daysMeals = daysMealsMap.toMap(),
                                gradualityConfigJson = if (gradualityConfig.hasActiveGraduality()) gradualityConfig.toJsonString() else null,
                                onComplete = {
                                    isDirty = false
                                    onBack()
                                }
                            )
                        }
                    },
                    enabled = planTitle.isNotBlank() && isLoaded,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(42.dp)
                        .testTag("btn_save_full_plan")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = com.example.ui.i18n.AppStrings.save(currentLanguage),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (!isLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SagePrimary)
            }
            return
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Scheda Informazioni Generali Piano
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = planTitle,
                            onValueChange = {
                                planTitle = it
                                isDirty = true
                            },
                            label = { Text(com.example.ui.i18n.AppStrings.planNameInputLabel(currentLanguage)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_editor_plan_title"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = planNotes,
                            onValueChange = {
                                planNotes = it
                                isDirty = true
                            },
                            label = { Text(com.example.ui.i18n.AppStrings.planNotesInputLabel(currentLanguage)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = com.example.ui.i18n.AppStrings.setActiveCheckboxLabel(currentLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = setAsActive,
                                onCheckedChange = {
                                    setAsActive = it
                                    isDirty = true
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SagePrimary,
                                    checkedTrackColor = SagePrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Barra a schede per giorno della settimana bloccata in alto durante lo scorrimento
            stickyHeader(key = "sticky_days_header") {
                StickyDaySelectorBar(
                    selectedDay = selectedDay,
                    onDaySelected = {
                        selectedDay = it
                        inlineAddMealType = null // Chiudi eventuale form inline
                        activeMealType = null // Reset pasto attivo per il nuovo giorno
                    },
                    isPinned = isDaysBarPinned,
                    lang = currentLanguage,
                    onHeightMeasured = { stickyHeaderHeightPx = it }
                )
            }

            // Riga azioni giorno (scorre insieme ai pasti)
            item(key = "day_actions_row") {
                val currentDayInfo = DayOfWeekInfo.ALL_DAYS.firstOrNull { it.dayIndex == selectedDay }
                val currentDayMeals = daysMealsMap[selectedDay] ?: emptyList()
                val totalFoodsInDay = currentDayMeals.sumOf { it.items.size }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dayFullName = currentDayInfo?.getLocalizedFullName(currentLanguage) ?: "Giorno"
                    val foodsLabel = if (currentLanguage == com.example.data.model.AppLanguage.IT) "alimenti" else "foods"
                    Text(
                        text = "$dayFullName • $totalFoodsInDay $foodsLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showGradualityScreen = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_manage_graduality")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = if (currentLanguage == com.example.data.model.AppLanguage.IT) "Gestione gradualità" else "Graduality management",
                                tint = SagePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = { showCopyDayDialog = true },
                            enabled = totalFoodsInDay > 0,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_copy_entire_day")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (totalFoodsInDay > 0) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == com.example.data.model.AppLanguage.IT) "Copia Giorno" else "Copy Day",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (totalFoodsInDay > 0) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // 5 Pasti per il giorno selezionato
            val currentDayMeals = daysMealsMap[selectedDay] ?: emptyList()

            items(currentDayMeals) { mealWithItems ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Intestazione pasto con tasto copia pasto e tasto inline "+ Aggiungi"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SagePrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mealWithItems.mealType.getLocalizedName(currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    val countSuffix = if (currentLanguage == com.example.data.model.AppLanguage.IT) "alimenti configurati" else "configured foods"
                                    Text(
                                        text = "${mealWithItems.items.size} $countSuffix",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { copyingMealTarget = mealWithItems },
                                    enabled = mealWithItems.items.isNotEmpty(),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_copy_meal_${mealWithItems.mealType.name.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = if (currentLanguage == com.example.data.model.AppLanguage.IT) "Copia intero pasto" else "Copy entire meal",
                                        tint = if (mealWithItems.items.isNotEmpty()) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        val willOpen = inlineAddMealType != mealWithItems.mealType
                                        inlineAddMealType = if (willOpen) mealWithItems.mealType else null
                                        if (willOpen) {
                                            activeMealType = mealWithItems.mealType
                                            val mealIdx = currentDayMeals.indexOfFirst { it.mealType == mealWithItems.mealType }
                                            if (mealIdx >= 0) {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(
                                                        index = 3 + mealIdx,
                                                        scrollOffset = if (stickyHeaderHeightPx > 0) -stickyHeaderHeightPx else 0
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_inline_add_${mealWithItems.mealType.name.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = if (inlineAddMealType == mealWithItems.mealType) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = if (inlineAddMealType == mealWithItems.mealType)
                                            com.example.ui.i18n.AppStrings.close(currentLanguage)
                                        else
                                            com.example.ui.i18n.AppStrings.addExtraConfirm(currentLanguage),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Lista cibi già presenti nel pasto (con riordino manuale drag-and-drop)
                        if (mealWithItems.items.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ReorderableMealFoodList(
                                items = mealWithItems.items,
                                lang = currentLanguage,
                                onReorder = { fromIdx, toIdx ->
                                    val mutableList = mealWithItems.items.toMutableList()
                                    val movedItem = mutableList.removeAt(fromIdx)
                                    mutableList.add(toIdx, movedItem)
                                    val updatedMeals = currentDayMeals.map {
                                        if (it.mealType == mealWithItems.mealType) it.copy(items = mutableList) else it
                                    }
                                    daysMealsMap[selectedDay] = updatedMeals
                                    isDirty = true
                                },
                                onEdit = { foodItem ->
                                    editingFoodTarget = Triple(selectedDay, mealWithItems.mealType, foodItem)
                                },
                                onCopy = { foodItem ->
                                    copyingFoodTarget = foodItem
                                },
                                onDelete = { foodItem ->
                                    deletingFoodTarget = Triple(selectedDay, mealWithItems.mealType, foodItem)
                                }
                            )
                        } else {
                            // Stato vuoto amichevole: consente di svuotare completamente il pasto o il piano
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                                            "Nessun alimento in questo pasto. Tocca '+' per aggiungerne uno."
                                        else
                                            "No foods in this meal. Tap '+' to add one.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Componente Form Inline per Aggiunta Rapida Alimento (senza cambiare schermata!)
                        AnimatedVisibility(visible = inlineAddMealType == mealWithItems.mealType) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))
                                InlineAddFoodForm(
                                    mealType = mealWithItems.mealType,
                                    searchResults = searchResults,
                                    isSearching = isSearching,
                                    lang = currentLanguage,
                                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                    onConfirmAdd = { newItem ->
                                        val updatedList = mealWithItems.items + newItem
                                        val updatedMeals = currentDayMeals.map {
                                            if (it.mealType == mealWithItems.mealType) it.copy(items = updatedList) else it
                                        }
                                        daysMealsMap[selectedDay] = updatedMeals
                                        isDirty = true
                                        inlineAddMealType = null
                                        viewModel.onSearchQueryChanged("")

                                        // Riporta fluidamente in focus il pasto attivo corrente
                                        val targetMeal = activeMealType ?: mealWithItems.mealType
                                        val mealIdx = currentDayMeals.indexOfFirst { it.mealType == targetMeal }
                                        if (mealIdx >= 0) {
                                            coroutineScope.launch {
                                                delay(80)
                                                listState.animateScrollToItem(
                                                    index = 3 + mealIdx,
                                                    scrollOffset = if (stickyHeaderHeightPx > 0) -stickyHeaderHeightPx else 0
                                                )
                                            }
                                        }
                                    },
                                    onCancel = {
                                        inlineAddMealType = null
                                        viewModel.onSearchQueryChanged("")

                                        // Riporta fluidamente in focus il pasto attivo anche in caso di annullamento
                                        val targetMeal = activeMealType ?: mealWithItems.mealType
                                        val mealIdx = currentDayMeals.indexOfFirst { it.mealType == targetMeal }
                                        if (mealIdx >= 0) {
                                            coroutineScope.launch {
                                                delay(80)
                                                listState.animateScrollToItem(
                                                    index = 3 + mealIdx,
                                                    scrollOffset = if (stickyHeaderHeightPx > 0) -stickyHeaderHeightPx else 0
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog di modifica rapida dell'alimento
        editingFoodTarget?.let { (day, mealType, foodItem) ->
            EditFoodItemDialog(
                foodItem = foodItem,
                lang = currentLanguage,
                onDismiss = { editingFoodTarget = null },
                onConfirm = { updatedFood ->
                    val dayMeals = daysMealsMap[day] ?: emptyList()
                    val updatedMeals = dayMeals.map { meal ->
                        if (meal.mealType == mealType) {
                            meal.copy(items = meal.items.map { if (it.id == updatedFood.id) updatedFood else it })
                        } else {
                            meal
                        }
                    }
                    daysMealsMap[day] = updatedMeals
                    isDirty = true
                    editingFoodTarget = null
                }
            )
        }

        // Dialog per la copia dell'alimento su più giorni e pasti
        copyingFoodTarget?.let { foodItemToCopy ->
            CopyFoodItemDialog(
                foodName = foodItemToCopy.name,
                lang = currentLanguage,
                onDismiss = { copyingFoodTarget = null },
                onConfirmCopy = { targetDays, targetMealTypes ->
                    for (targetDay in targetDays) {
                        val dayMeals = daysMealsMap[targetDay] ?: emptyList()
                        val updatedMeals = dayMeals.map { meal ->
                            if (meal.mealType in targetMealTypes) {
                                val newItem = foodItemToCopy.copy(
                                    id = UUID.randomUUID().toString(),
                                    mealId = meal.mealId
                                )
                                meal.copy(items = meal.items + newItem)
                            } else {
                                meal
                            }
                        }
                        daysMealsMap[targetDay] = updatedMeals
                    }
                    isDirty = true
                    copyingFoodTarget = null
                }
            )
        }

        // Dialog di conferma eliminazione alimento invocata dallo swipe a sinistra
        deletingFoodTarget?.let { (day, mealType, foodItem) ->
            AlertDialog(
                onDismissRequest = { deletingFoodTarget = null },
                title = {
                    Text(
                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT) "Conferma eliminazione" else "Confirm deletion",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                            "Vuoi davvero eliminare l'alimento \"${com.example.ui.i18n.AppStrings.translateFoodName(foodItem.name, currentLanguage)}\" da ${mealType.getLocalizedName(currentLanguage)}?"
                        else
                            "Are you sure you want to delete \"${foodItem.name}\" from ${mealType.getLocalizedName(currentLanguage)}?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val dayMeals = daysMealsMap[day] ?: emptyList()
                            val updatedMeals = dayMeals.map { meal ->
                                if (meal.mealType == mealType) {
                                    meal.copy(items = meal.items.filter { it.id != foodItem.id })
                                } else {
                                    meal
                                }
                            }
                            daysMealsMap[day] = updatedMeals
                            isDirty = true
                            deletingFoodTarget = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                    ) {
                        Text(com.example.ui.i18n.AppStrings.deleteFood(currentLanguage), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingFoodTarget = null }) {
                        Text(com.example.ui.i18n.AppStrings.cancel(currentLanguage), color = Color.White)
                    }
                }
            )
        }
    }
}


/**
 * Componente per la lista di alimenti del pasto che supporta lo spostamento
 * puntuale e immediato tramite pulsanti freccia Su e Giù dedicati.
 */
@Composable
fun ReorderableMealFoodList(
    items: List<FoodItemModel>,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onEdit: (FoodItemModel) -> Unit,
    onCopy: (FoodItemModel) -> Unit,
    onDelete: (FoodItemModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { index, foodItem ->
            InlineFoodItemRow(
                item = foodItem,
                lang = lang,
                canMoveUp = index > 0,
                canMoveDown = index < items.lastIndex,
                onMoveUp = { onReorder(index, index - 1) },
                onMoveDown = { onReorder(index, index + 1) },
                onEdit = { onEdit(foodItem) },
                onCopy = { onCopy(foodItem) },
                onDelete = { onDelete(foodItem) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun InlineFoodItemRow(
    item: FoodItemModel,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe verso destra dalla metà in poi -> Copia alimento
                    onCopy()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe verso sinistra dalla metà in poi -> Chiede conferma eliminazione
                    onDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd
            val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart
            val backgroundColor = when {
                isStartToEnd -> SagePrimary
                isEndToStart -> TerracottaAccent
                else -> Color.Transparent
            }
            val alignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            val icon = if (isStartToEnd) Icons.Default.ContentCopy else Icons.Default.DeleteOutline
            val text = if (isStartToEnd) {
                if (lang == com.example.data.model.AppLanguage.IT) "Copia" else "Copy"
            } else {
                if (lang == com.example.data.model.AppLanguage.IT) "Elimina" else "Delete"
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp),
                contentAlignment = alignment
            ) {
                if (isStartToEnd || isEndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isStartToEnd) {
                            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
                            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onEdit() }
                .testTag("food_item_${item.id}"),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Controlli freccia Su e Giù per riordino fluido ed immediato
                Column(
                    modifier = Modifier.padding(end = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("btn_move_up_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (lang == com.example.data.model.AppLanguage.IT) "Sposta su" else "Move up",
                            tint = if (canMoveUp) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("btn_move_down_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (lang == com.example.data.model.AppLanguage.IT) "Sposta giù" else "Move down",
                            tint = if (canMoveDown) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format("%.1f", item.quantity)
                        Text(
                            text = com.example.ui.i18n.AppStrings.translateFoodName(item.name, lang),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($qtyStr ${item.unit.getLocalizedShortName(lang)})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val factor = if (item.unit == QuantityUnit.GRAMS) item.quantity / 100.0 else item.quantity
                        Text("${(item.calories * factor).toInt()} kcal", fontSize = 11.sp, color = SagePrimary, fontWeight = FontWeight.Bold)

                        val category = FoodCategory.fromCategoryString(item.category) ?: FoodCategory.categorize(item.name)
                        Text(
                            text = "${category.iconEmoji} ${category.getLocalizedName(lang)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.toDefrost) {
                            DefrostBadge()
                        }
                    }
                }
            }
        }
    }
}


/**
 * Dialog Material 3 per la modifica rapida di quantita, unita e scongelamento dell'alimento selezionato.
 */
@Composable
fun EditFoodItemDialog(
    foodItem: FoodItemModel,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onDismiss: () -> Unit,
    onConfirm: (FoodItemModel) -> Unit
) {
    var quantityText by remember {
        val qty = if (foodItem.quantity % 1.0 == 0.0) foodItem.quantity.toInt().toString() else foodItem.quantity.toString()
        mutableStateOf(qty)
    }
    var selectedUnit by remember { mutableStateOf(foodItem.unit) }
    var toDefrost by remember { mutableStateOf(foodItem.toDefrost) }
    var selectedCategory by remember {
        mutableStateOf(
            FoodCategory.fromCategoryString(foodItem.category) ?: FoodCategory.categorize(foodItem.name)
        )
    }

    fun adjustQuantity(delta: Double) {
        val current = quantityText.replace(',', '.').toDoubleOrNull() ?: foodItem.quantity
        val updated = (current + delta).coerceAtLeast(1.0)
        quantityText = if (updated % 1.0 == 0.0) updated.toInt().toString() else String.format("%.1f", updated).replace(',', '.')
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = com.example.ui.i18n.AppStrings.editFoodTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = com.example.ui.i18n.AppStrings.translateFoodName(foodItem.name, lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selezione Categoria (Corsia Spesa)
                CategorySelectorDropdown(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    lang = lang
                )

                // Selezione Unità di Misura
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = com.example.ui.i18n.AppStrings.unitLabel(lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuantityUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit },
                                label = { Text(unit.getLocalizedName(lang), fontSize = 12.sp) },
                                leadingIcon = if (selectedUnit == unit) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                // Campo Quantità e Stepper rapidi (+ / -)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = com.example.ui.i18n.AppStrings.quantityLabel(lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_food_quantity"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            Text(
                                text = selectedUnit.getLocalizedShortName(lang),
                                modifier = Modifier.padding(end = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    // Quick Stepper Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedUnit == QuantityUnit.GRAMS) {
                            OutlinedButton(
                                onClick = { adjustQuantity(-50.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("-50g", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { adjustQuantity(-10.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("-10g", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { adjustQuantity(10.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+10g", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { adjustQuantity(50.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+50g", fontSize = 11.sp)
                            }
                        } else {
                            val pcsShort = QuantityUnit.PIECES.getLocalizedShortName(lang)
                            OutlinedButton(
                                onClick = { adjustQuantity(-1.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("-1 $pcsShort", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { adjustQuantity(1.0) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+1 $pcsShort", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Scongelamento
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = CartaDaZucchero,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = com.example.ui.i18n.AppStrings.defrostReminderLabel(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = toDefrost,
                            onCheckedChange = { toDefrost = it },
                            modifier = Modifier.testTag("switch_edit_food_defrost"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CartaDaZucchero,
                                checkedTrackColor = CartaDaZuccheroContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedQty = quantityText.replace(',', '.').toDoubleOrNull() ?: foodItem.quantity
                    val finalQty = if (parsedQty <= 0.0) 1.0 else parsedQty
                    onConfirm(
                        foodItem.copy(
                            quantity = finalQty,
                            unit = selectedUnit,
                            toDefrost = toDefrost,
                            category = selectedCategory.displayName
                        )
                    )
                },
                modifier = Modifier.testTag("btn_save_edit_food")
            ) {
                Text(com.example.ui.i18n.AppStrings.saveChanges(lang))
            }
        },

        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_edit_food")
            ) {
                Text(com.example.ui.i18n.AppStrings.cancel(lang))
            }
        }
    )
}

/**
 * Modulo Inline fluido per l'inserimento dell'alimento con ricerca rapida nel database.
 * Minimizza i tap ed evita schermate aggiuntive.
 */
@Composable
fun InlineAddFoodForm(
    mealType: MealType,
    searchResults: List<FoodItemModel>,
    isSearching: Boolean,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onSearchQueryChanged: (String) -> Unit,
    onConfirmAdd: (FoodItemModel) -> Unit,
    onCancel: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf(QuantityUnit.GRAMS) }
    var toDefrost by remember { mutableStateOf(false) }

    var baseCalories by remember { mutableDoubleStateOf(0.0) }
    var baseProtein by remember { mutableDoubleStateOf(0.0) }
    var baseCarbs by remember { mutableDoubleStateOf(0.0) }
    var baseFat by remember { mutableDoubleStateOf(0.0) }
    var selectedCategory by remember { mutableStateOf(FoodCategory.FRUITS_VEGETABLES) }

    var showAllResults by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(250)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignora se il componente è stato smontato
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val titlePrefix = if (lang == com.example.data.model.AppLanguage.IT) "Nuovo alimento per" else "New food for"
            Text(
                text = "$titlePrefix ${mealType.getLocalizedName(lang)}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo di ricerca con supporto database FoodDb e alimenti utilizzati in precedenza
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showAllResults = false
                    onSearchQueryChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("inline_search_input"),
                label = { Text(com.example.ui.i18n.AppStrings.searchFoodPlaceholder(lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showAllResults = false
                            onSearchQueryChanged("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Risultati ricerca in tempo reale con distinzione cromatica sorgente
            if (isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${com.example.ui.i18n.AppStrings.searchFoodPlaceholder(lang)}...", fontSize = 11.sp, color = Color.White)
                }
            } else if (searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.ui.i18n.AppStrings.tapToSelect(lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FoodSourceDot(source = "Local")
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(if (lang == com.example.data.model.AppLanguage.IT) "Locale" else "Local", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(8.dp))
                                FoodSourceDot(source = "FoodDb")
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("FoodDb", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                        val displayedResults = if (showAllResults) searchResults else searchResults.take(5)
                        displayedResults.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        foodName = item.name
                                        brand = item.brand ?: ""
                                        baseCalories = item.calories
                                        baseProtein = item.protein
                                        baseCarbs = item.carbs
                                        baseFat = item.fat
                                        selectedCategory = FoodCategory.fromCategoryString(item.category) ?: FoodCategory.categorize(item.name)
                                        searchQuery = ""
                                        showAllResults = false
                                        onSearchQueryChanged("")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FoodSourceDot(source = item.source)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.ui.i18n.AppStrings.translateFoodName(item.name, lang),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    if (!item.secondaryName.isNullOrBlank()) {
                                        Text(
                                            text = item.secondaryName,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                    val sourceText = if (item.source == "Local") {
                                        if (lang == com.example.data.model.AppLanguage.IT) "Archivio interno" else "Internal archive"
                                    } else "FoodDb"
                                    val brandText = if (!item.brand.isNullOrBlank()) "${item.brand} • " else ""
                                    Text(
                                        text = "$brandText$sourceText • ${item.calories.toInt()} kcal/100g",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }

                        if (searchResults.size > 5) {
                            TextButton(
                                onClick = { showAllResults = !showAllResults },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_toggle_expand_results")
                            ) {
                                Text(
                                    text = if (showAllResults) com.example.ui.i18n.AppStrings.showLess(lang) else com.example.ui.i18n.AppStrings.showAll(lang, searchResults.size),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nome Alimento
            OutlinedTextField(
                value = foodName,
                onValueChange = {
                    foodName = it
                    if (selectedCategory == FoodCategory.FRUITS_VEGETABLES || selectedCategory == FoodCategory.PANTRY_SPICES) {
                        selectedCategory = FoodCategory.categorize(it)
                    }
                },
                label = { Text("${com.example.ui.i18n.AppStrings.foodNameLabel(lang)} *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inline_food_name"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selettore Categoria Alimento (Corsia Supermercato)
            CategorySelectorDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                lang = lang
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quantità e Unità di Misura (Grammi / Pezzi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text(com.example.ui.i18n.AppStrings.quantityLabel(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                FilterChip(
                    selected = selectedUnit == QuantityUnit.GRAMS,
                    onClick = { selectedUnit = QuantityUnit.GRAMS },
                    label = { Text("${QuantityUnit.GRAMS.getLocalizedName(lang)} (${QuantityUnit.GRAMS.getLocalizedShortName(lang)})", fontSize = 11.sp) }
                )

                FilterChip(
                    selected = selectedUnit == QuantityUnit.PIECES,
                    onClick = { selectedUnit = QuantityUnit.PIECES },
                    label = { Text("${QuantityUnit.PIECES.getLocalizedName(lang)} (${QuantityUnit.PIECES.getLocalizedShortName(lang)})", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle "Da scongelare"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = CartaDaZucchero,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = com.example.ui.i18n.AppStrings.defrostReminderLabel(lang),
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = toDefrost,
                    onCheckedChange = { toDefrost = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CartaDaZucchero,
                        checkedTrackColor = CartaDaZuccheroContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pulsanti di Conferma e Annulla inline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(com.example.ui.i18n.AppStrings.cancel(lang), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (foodName.isNotBlank()) {
                            val qty = quantityInput.toDoubleOrNull() ?: 100.0
                            val newItem = FoodItemModel(
                                id = UUID.randomUUID().toString(),
                                mealId = "",
                                name = foodName.trim(),
                                brand = brand.ifBlank { null },
                                quantity = qty,
                                unit = selectedUnit,
                                toDefrost = toDefrost,
                                calories = baseCalories,
                                protein = baseProtein,
                                carbs = baseCarbs,
                                fat = baseFat,
                                category = selectedCategory.displayName
                            )
                            onConfirmAdd(newItem)
                        }
                    },
                    enabled = foodName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier.testTag("btn_inline_confirm_add")
                ) {
                    Text(com.example.ui.i18n.AppStrings.addFoodToMeal(lang, mealType.getLocalizedName(lang)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

