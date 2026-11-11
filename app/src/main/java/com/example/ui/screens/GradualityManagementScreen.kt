package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.FoodCategory
import com.example.data.model.GradualityGroupConfig
import com.example.data.model.GradualityPlanConfig
import com.example.data.model.MealType
import com.example.data.model.MealWithItems
import com.example.data.model.QuantityUnit
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.CartaDaZuccheroContainer
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradualityManagementScreen(
    daysMeals: Map<Int, List<MealWithItems>>,
    initialConfig: GradualityPlanConfig,
    lang: AppLanguage,
    onBack: () -> Unit,
    onSave: (GradualityPlanConfig) -> Unit,
    onDeleteGraduality: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var globalStartDateMillis by remember(initialConfig.globalStartDateMillis) { mutableLongStateOf(initialConfig.globalStartDateMillis) }
    var globalCadenceDays by remember(initialConfig.globalCadenceDays) { mutableIntStateOf(initialConfig.globalCadenceDays) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    data class FoodOccurrence(
        val name: String,
        val quantity: Double,
        val unit: QuantityUnit,
        val category: String?,
        val mealType: MealType,
        val dayOfWeek: Int
    )

    val occurrences = remember(daysMeals) {
        val list = mutableListOf<FoodOccurrence>()
        daysMeals.forEach { (day, meals) ->
            meals.forEach { meal ->
                meal.items.forEach { item ->
                    if (item.name.isNotBlank()) {
                        list.add(
                            FoodOccurrence(
                                name = item.name.trim(),
                                quantity = item.quantity,
                                unit = item.unit,
                                category = item.category,
                                mealType = meal.mealType,
                                dayOfWeek = day
                            )
                        )
                    }
                }
            }
        }
        list
    }

    val initialGroups = remember(occurrences, initialConfig) {
        val grouped = occurrences.groupBy {
            "${it.name.lowercase(Locale.ROOT)}|${it.unit.name}"
        }

        val result = mutableListOf<GradualityGroupConfig>()
        grouped.forEach { (key, itemsInGroup) ->
            val first = itemsInGroup.first()
            val targetMeals = itemsInGroup.map { it.mealType }.distinct()
            val targetDays = itemsInGroup.map { it.dayOfWeek }.distinct()
            val categoryEnum = FoodCategory.categorize(first.name, false, first.category)

            val existing = initialConfig.items.find {
                it.foodName.equals(first.name, ignoreCase = true) && it.unit == first.unit
            }

            if (existing != null) {
                val updated = existing.copy(
                    groupKey = key,
                    category = categoryEnum.name,
                    targetMealTypes = targetMeals,
                    targetDays = targetDays,
                    cadenceDays = globalCadenceDays,
                    startDateMillis = globalStartDateMillis,
                    nextStepDateMillis = globalStartDateMillis + globalCadenceDays * 86_400_000L
                )
                result.add(updated.copy(scheduledSteps = updated.generateScheduledSteps()))
            } else {
                val newItem = GradualityGroupConfig(
                    groupKey = key,
                    foodName = first.name,
                    unit = first.unit,
                    category = categoryEnum.name,
                    targetMealTypes = targetMeals,
                    targetDays = targetDays,
                    isEnabled = false,
                    minQuantity = first.quantity,
                    maxQuantity = first.quantity + 20.0,
                    incrementStep = 10.0,
                    cadenceDays = globalCadenceDays,
                    startDateMillis = globalStartDateMillis,
                    nextStepDateMillis = globalStartDateMillis + globalCadenceDays * 86_400_000L,
                    baseQuantity = first.quantity,
                    currentAppliedQuantity = first.quantity
                )
                result.add(newItem.copy(scheduledSteps = newItem.generateScheduledSteps()))
            }
        }
        result
    }

    val groupsState = remember(initialGroups) {
        mutableStateMapOf<String, GradualityGroupConfig>().apply {
            initialGroups.forEach { put(it.groupKey, it) }
        }
    }

    val expandedCards = remember(initialGroups) {
        mutableStateMapOf<String, Boolean>().apply {
            initialGroups.forEach { put(it.groupKey, it.isEnabled) }
        }
    }

    var collapsedCategories by remember { mutableStateOf(setOf<FoodCategory>()) }

    fun updateGlobalSchedule(newStartDate: Long, newCadence: Int) {
        globalStartDateMillis = newStartDate
        globalCadenceDays = newCadence
        groupsState.keys.toList().forEach { key ->
            val current = groupsState[key] ?: return@forEach
            val updated = current.copy(
                startDateMillis = newStartDate,
                cadenceDays = newCadence,
                nextStepDateMillis = newStartDate + newCadence * 86_400_000L
            )
            groupsState[key] = updated.copy(scheduledSteps = updated.generateScheduledSteps())
        }
    }

    val categorizedGroups = remember(groupsState.values.toList()) {
        val map = mutableMapOf<FoodCategory, MutableList<GradualityGroupConfig>>()
        groupsState.values.forEach { group ->
            val cat = try {
                FoodCategory.valueOf(group.category)
            } catch (e: Exception) {
                FoodCategory.categorize(group.foodName, false, null)
            }
            map.getOrPut(cat) { mutableListOf() }.add(group)
        }
        map.toSortedMap(compareBy { it.ordinal })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CartaDaZuccheroContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = CartaDaZucchero,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (lang == AppLanguage.IT) "Gestione Gradualità" else "Graduality Management",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_graduality_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (lang == AppLanguage.IT) "Torna indietro" else "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val updatedConfig = GradualityPlanConfig(
                                items = groupsState.values.map { it.copy(scheduledSteps = it.generateScheduledSteps()) },
                                globalStartDateMillis = globalStartDateMillis,
                                globalCadenceDays = globalCadenceDays
                            )
                            onSave(updatedConfig)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("btn_save_graduality")
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.IT) "Salva" else "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "info_card") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == AppLanguage.IT) "Pianificazione Temporale Graduale" else "Gradual Schedule Planning",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == AppLanguage.IT)
                                    "La gradualità calcola date e pesi di incremento dal minimo al massimo. Non vincola il piano a un peso fisso e permette il confronto trasparente con i pesi reali."
                                else
                                    "Graduality computes dates and weights from minimum to maximum. It doesn't lock to a static weight and allows transparent comparison with actual meal plan weights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item(key = "global_settings_card") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SagePrimaryContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, SagePrimary.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_global_graduality_settings")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.IT) "Impostazioni Generali Gradualità" else "General Graduality Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.IT)
                                "La data di inizio e la cadenza impostate qui sotto sono uniche e condivise da tutti gli alimenti con gradualità."
                            else
                                "The start date and cadence configured below are shared across all foods with graduality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == AppLanguage.IT) "Data inizio piano:" else "Plan start date:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, SagePrimary.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance().apply { timeInMillis = globalStartDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val pickedCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                updateGlobalSchedule(pickedCal.timeInMillis, globalCadenceDays)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .testTag("btn_pick_global_start_date")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(globalStartDateMillis)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SagePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == AppLanguage.IT) "Cadenza:" else "Cadence:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = globalCadenceDays == 7,
                                    onClick = { updateGlobalSchedule(globalStartDateMillis, 7) },
                                    label = { Text(if (lang == AppLanguage.IT) "Settimanale (7 gg)" else "Weekly (7 d)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SagePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("chip_global_cadence_7")
                                )
                                FilterChip(
                                    selected = globalCadenceDays == 14,
                                    onClick = { updateGlobalSchedule(globalStartDateMillis, 14) },
                                    label = { Text(if (lang == AppLanguage.IT) "Bisettimanale (14 gg)" else "Biweekly (14 d)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SagePrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("chip_global_cadence_14")
                                )
                            }
                        }
                    }
                }
            }

            // Pulsante dedicato per eliminare completamente la configurazione della gradualità
            item(key = "delete_graduality_section") {
                OutlinedButton(
                    onClick = { showDeleteConfirmationDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .testTag("btn_delete_graduality")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == AppLanguage.IT) "Elimina configurazione gradualità" else "Delete graduality configuration",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            categorizedGroups.forEach { (category, groups) ->
                val isCollapsed = collapsedCategories.contains(category)

                item(key = "cat_header_${category.name}") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                collapsedCategories = if (isCollapsed) {
                                    collapsedCategories - category
                                } else {
                                    collapsedCategories + category
                                }
                            }
                            .testTag("cat_header_${category.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.getLocalizedName(lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = SagePrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = groups.size.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SagePrimary
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isCollapsed) "Espandi categoria" else "Comprimi categoria",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!isCollapsed) {
                    items(
                        items = groups,
                        key = { it.groupKey }
                    ) { groupConfig ->
                        val isExpanded = expandedCards[groupConfig.groupKey] ?: false
                        GradualityFoodCard(
                            config = groupConfig,
                            isExpanded = isExpanded,
                            lang = lang,
                            onToggleExpanded = {
                                expandedCards[groupConfig.groupKey] = !isExpanded
                            },
                            onConfigChange = { updated ->
                                val withSteps = updated.copy(scheduledSteps = updated.generateScheduledSteps())
                                groupsState[groupConfig.groupKey] = withSteps
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = {
                Text(
                    text = if (lang == AppLanguage.IT) "Elimina gradualità" else "Delete Graduality",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.IT)
                        "Sei sicuro di voler eliminare completamente la configurazione della gradualità per questo piano? Tutte le pianificazioni e gli step impostati verranno rimossi."
                    else
                        "Are you sure you want to completely delete the graduality configuration for this plan? All scheduled changes and steps will be removed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        onDeleteGraduality()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_delete_graduality")
                ) {
                    Text(if (lang == AppLanguage.IT) "Elimina" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false },
                    modifier = Modifier.testTag("btn_dismiss_delete_graduality")
                ) {
                    Text(if (lang == AppLanguage.IT) "Annulla" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun GradualityFoodCard(
    config: GradualityGroupConfig,
    isExpanded: Boolean,
    lang: AppLanguage,
    onToggleExpanded: () -> Unit,
    onConfigChange: (GradualityGroupConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(if (config.isEnabled) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("graduality_card_${config.groupKey}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (config.isEnabled) {
                            onToggleExpanded()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${config.foodName} (${config.minQuantity.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)} min)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${config.getFormattedTargetMeals(lang)} • ${config.getFormattedTargetDays(lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SagePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = config.isEnabled,
                        onCheckedChange = { enabled ->
                            val updated = config.copy(isEnabled = enabled)
                            onConfigChange(updated.copy(scheduledSteps = updated.generateScheduledSteps()))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SagePrimary
                        ),
                        modifier = Modifier.testTag("switch_${config.groupKey}")
                    )

                    if (config.isEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onToggleExpanded,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_expand_${config.groupKey}")
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                                tint = SagePrimary
                            )
                        }
                    }
                }
            }

            if (config.isEnabled && !isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExpanded() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: ${config.minQuantity.toIntIfWhole()} | Max: ${config.maxQuantity.toIntIfWhole()} | Passo: +${config.incrementStep.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (lang == AppLanguage.IT) "Modifica" else "Edit",
                        fontSize = 11.sp,
                        color = SagePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = config.isEnabled && isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (config.minQuantity > 0) config.minQuantity.toIntIfWhole() else "",
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: 0.0
                                val updated = config.copy(minQuantity = v)
                                onConfigChange(updated.copy(scheduledSteps = updated.generateScheduledSteps()))
                            },
                            label = { Text(if (lang == AppLanguage.IT) "Peso iniziale (min)" else "Initial wt (min)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("input_min_${config.groupKey}")
                        )

                        OutlinedTextField(
                            value = if (config.maxQuantity > 0) config.maxQuantity.toIntIfWhole() else "",
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: 0.0
                                val updated = config.copy(maxQuantity = v)
                                onConfigChange(updated.copy(scheduledSteps = updated.generateScheduledSteps()))
                            },
                            label = { Text(if (lang == AppLanguage.IT) "Peso max" else "Max wt", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_max_${config.groupKey}")
                        )

                        OutlinedTextField(
                            value = if (config.incrementStep > 0) config.incrementStep.toIntIfWhole() else "",
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: 0.0
                                val updated = config.copy(incrementStep = v)
                                onConfigChange(updated.copy(scheduledSteps = updated.generateScheduledSteps()))
                            },
                            label = { Text(if (lang == AppLanguage.IT) "Passo (+/-)" else "Step (+/-)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_step_${config.groupKey}")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val isMax = config.isMaxReached()
                    val daysRemaining = config.daysRemaining()
                    val nextQty = config.nextQuantity()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMax)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            SagePrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isMax) MaterialTheme.colorScheme.onSurfaceVariant else SagePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val previewText = if (isMax) {
                                if (lang == AppLanguage.IT)
                                    "Quantità massima stabilita (${config.maxQuantity.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)}) raggiunta!"
                                else
                                    "Maximum established quantity (${config.maxQuantity.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)}) reached!"
                            } else {
                                val daysText = if (daysRemaining <= 0) {
                                    if (lang == AppLanguage.IT) "oggi o scaduta" else "today or due"
                                } else {
                                    if (lang == AppLanguage.IT) "tra $daysRemaining giorni" else "in $daysRemaining days"
                                }
                                if (lang == AppLanguage.IT)
                                    "Prossima variazione: ${nextQty.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)} prevista $daysText (${config.getFormattedDate(config.nextStepDateMillis)})"
                                else
                                    "Next change: ${nextQty.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)} scheduled $daysText (${config.getFormattedDate(config.nextStepDateMillis)})"
                            }
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isMax) MaterialTheme.colorScheme.onSurfaceVariant else SagePrimary
                            )
                        }
                    }

                    // Anteprima degli step temporali pianificati
                    val steps = if (config.scheduledSteps.isNotEmpty()) config.scheduledSteps else config.generateScheduledSteps()
                    if (steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == AppLanguage.IT) "Pianificazione step calcolata:" else "Calculated scheduled steps:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .padding(vertical = 2.dp)
                        ) {
                            steps.take(6).forEach { step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Step ${step.stepIndex} (${step.getFormattedDate()}):",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${step.fromQuantity.toIntIfWhole()} → ${step.toQuantity.toIntIfWhole()} ${config.unit.getLocalizedShortName(lang)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SagePrimary
                                    )
                                }
                            }
                            if (steps.size > 6) {
                                Text(
                                    text = if (lang == AppLanguage.IT) "+ altri ${steps.size - 6} step" else "+ ${steps.size - 6} more steps",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.toIntIfWhole(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        String.format(java.util.Locale.getDefault(), "%.1f", this)
    }
}
