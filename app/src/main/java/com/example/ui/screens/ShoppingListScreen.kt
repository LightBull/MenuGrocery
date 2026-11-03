package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShoppingListItemEntity
import com.example.data.model.DayOfWeekInfo
import com.example.data.model.FoodCategory
import com.example.data.model.QuantityUnit
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.CartaDaZuccheroContainer
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.TerracottaAccent
import com.example.ui.viewmodel.MealPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: MealPlanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.shoppingList.collectAsState()
    val selectedDays by viewModel.selectedShoppingDays.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showAddExtraDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var itemToAdjust by remember { mutableStateOf<ShoppingListItemEntity?>(null) }

    val purchasedCount = items.count { it.isPurchased }
    val totalCount = items.size
    val progress = if (totalCount > 0) purchasedCount.toFloat() / totalCount else 0f

    val unpurchasedItems = remember(items) { items.filter { !it.isPurchased } }
    val purchasedItems = remember(items) { items.filter { it.isPurchased } }

    var searchQuery by remember { mutableStateOf("") }
    // Accordion: Tutte le categorie aperte per impostazione predefinita (set vuoto di categorie compresse)
    var collapsedCategories by remember { mutableStateOf(setOf<FoodCategory>()) }
    var isPurchasedSectionCollapsed by remember { mutableStateOf(false) }

    val filteredUnpurchasedItems = remember(unpurchasedItems, searchQuery, currentLanguage) {
        if (searchQuery.isBlank()) unpurchasedItems
        else {
            val q = searchQuery.trim().lowercase()
            unpurchasedItems.filter {
                it.name.lowercase().contains(q) ||
                com.example.ui.i18n.AppStrings.translateFoodName(it.name, currentLanguage).lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                FoodCategory.categorize(it.name, it.isManualExtra, it.category).getLocalizedName(currentLanguage).lowercase().contains(q) ||
                it.sourceDays.lowercase().contains(q)
            }
        }
    }

    val filteredPurchasedItems = remember(purchasedItems, searchQuery, currentLanguage) {
        if (searchQuery.isBlank()) purchasedItems
        else {
            val q = searchQuery.trim().lowercase()
            purchasedItems.filter {
                it.name.lowercase().contains(q) ||
                com.example.ui.i18n.AppStrings.translateFoodName(it.name, currentLanguage).lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                FoodCategory.categorize(it.name, it.isManualExtra, it.category).getLocalizedName(currentLanguage).lowercase().contains(q) ||
                it.sourceDays.lowercase().contains(q)
            }
        }
    }

    // Raggruppa gli alimenti da comprare per corsie del supermercato (FoodCategory)
    val categorizedItems = remember(filteredUnpurchasedItems) {
        val map = mutableMapOf<FoodCategory, MutableList<ShoppingListItemEntity>>()
        for (item in filteredUnpurchasedItems) {
            val cat = FoodCategory.categorize(item.name, item.isManualExtra, item.category)
            map.getOrPut(cat) { mutableListOf() }.add(item)
        }
        map
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Intestazione & Azioni Rapide (Genera, Condividi, Importa, Ripristina)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = com.example.ui.i18n.AppStrings.shoppingListTitle(currentLanguage),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = com.example.ui.i18n.AppStrings.purchasedCountLabel(purchasedCount, totalCount, currentLanguage),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.resetShoppingList() },
                                    modifier = Modifier.testTag("btn_reset_shopping_list")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = com.example.ui.i18n.AppStrings.btnResetList(currentLanguage),
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { showShareDialog = true },
                                    modifier = Modifier.testTag("btn_share_shopping_list")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = com.example.ui.i18n.AppStrings.btnShareList(currentLanguage),
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { showImportDialog = true },
                                    modifier = Modifier.testTag("btn_import_shopping_list")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = com.example.ui.i18n.AppStrings.dialogImportTitle(currentLanguage),
                                        tint = Color.White
                                    )
                                }
                                if (purchasedCount > 0) {
                                    IconButton(
                                        onClick = { viewModel.clearPurchasedShoppingItems() },
                                        modifier = Modifier.testTag("btn_clear_purchased")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = com.example.ui.i18n.AppStrings.btnClearChecked(currentLanguage),
                                            tint = TerracottaAccent
                                        )
                                    }
                                }
                            }
                        }

                        if (totalCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SagePrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Barra di Ricerca Lista della Spesa
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("input_search_shopping_list"),
                    placeholder = { Text(com.example.ui.i18n.AppStrings.shoppingListSearchPlaceholder(currentLanguage), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = com.example.ui.i18n.AppStrings.searchFoodPlaceholder(currentLanguage), tint = SagePrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = com.example.ui.i18n.AppStrings.close(currentLanguage))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Selezione Range di Giorni per Generazione Automatica Aggregata
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.ui.i18n.AppStrings.generationRangeTitle(currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            val planTitle = com.example.ui.i18n.AppStrings.translatePlanTitle(
                                activePlan?.title ?: com.example.ui.i18n.AppStrings.noActivePlan(currentLanguage),
                                currentLanguage
                            )
                            Text(
                                text = "${com.example.ui.i18n.AppStrings.activePlanBadge(currentLanguage)}: $planTitle",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chip rapidi: Tutta la settimana, Lun-Gio, Ven-Dom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedDays.size == 7,
                                onClick = { viewModel.setQuickShoppingRange(listOf(1, 2, 3, 4, 5, 6, 7)) },
                                label = { Text(com.example.ui.i18n.AppStrings.daysFilterAll(currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_range_full_week")
                            )
                            FilterChip(
                                selected = selectedDays == setOf(1, 2, 3, 4),
                                onClick = { viewModel.setQuickShoppingRange(listOf(1, 2, 3, 4)) },
                                label = { Text(com.example.ui.i18n.AppStrings.daysFilterStartWeek(currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_range_mon_thu")
                            )
                            FilterChip(
                                selected = selectedDays == setOf(5, 6, 7),
                                onClick = { viewModel.setQuickShoppingRange(listOf(5, 6, 7)) },
                                label = { Text(com.example.ui.i18n.AppStrings.daysFilterWeekend(currentLanguage), fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_range_fri_sun")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chip singoli per ciascun giorno
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayOfWeekInfo.ALL_DAYS.forEach { dayInfo ->
                                val isSelected = selectedDays.contains(dayInfo.dayIndex)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleShoppingDay(dayInfo.dayIndex) }
                                        .padding(2.dp),
                                    color = if (isSelected) SagePrimary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = dayInfo.getLocalizedShortName(currentLanguage),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateShoppingList() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_generate_shopping_list"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(com.example.ui.i18n.AppStrings.btnGenerateAggregate(currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            FilledTonalButton(
                                onClick = { viewModel.resetShoppingList() },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_quick_reset_list"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = com.example.ui.i18n.AppStrings.btnResetList(currentLanguage), modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(com.example.ui.i18n.AppStrings.btnResetList(currentLanguage), fontSize = 12.sp, color = Color.White)
                            }

                            FilledTonalButton(
                                onClick = { showAddExtraDialog = true },
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_add_extra"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(com.example.ui.i18n.AppStrings.btnAddExtra(currentLanguage), fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Sezioni Categorie Corsie del Supermercato (con Accordion richiudibile)
            FoodCategory.entries.forEach { category ->
                val categoryItems = categorizedItems[category]
                if (!categoryItems.isNullOrEmpty()) {
                    val isCollapsed = collapsedCategories.contains(category)
                    item(key = "category_header_${category.name}") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    collapsedCategories = if (isCollapsed) {
                                        collapsedCategories - category
                                    } else {
                                        collapsedCategories + category
                                    }
                                }
                                .testTag("header_category_${category.name.lowercase()}"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(category.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.getLocalizedName(currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = com.example.ui.i18n.AppStrings.itemsToBuy(categoryItems.size, currentLanguage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (!isCollapsed) {
                        items(categoryItems, key = { it.id }) { item ->
                            ShoppingItemRow(
                                item = item,
                                lang = currentLanguage,
                                onToggle = { viewModel.toggleShoppingItem(item) },
                                onAdjustQuantity = { itemToAdjust = item },
                                onDelete = { viewModel.deleteShoppingItem(item.id) }
                            )
                        }
                    }
                }
            }

            // Sezione dedicata agli articoli già presi / acquistati (richiudibile)
            if (filteredPurchasedItems.isNotEmpty()) {
                item(key = "purchased_header") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isPurchasedSectionCollapsed = !isPurchasedSectionCollapsed }
                            .testTag("header_purchased_section"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.example.ui.i18n.AppStrings.purchasedSectionTitle(filteredPurchasedItems.size, currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPurchasedSectionCollapsed) com.example.ui.i18n.AppStrings.showLabel(currentLanguage) else com.example.ui.i18n.AppStrings.hideLabel(currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isPurchasedSectionCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (!isPurchasedSectionCollapsed) {
                    items(filteredPurchasedItems, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            lang = currentLanguage,
                            onToggle = { viewModel.toggleShoppingItem(item) },
                            onAdjustQuantity = { itemToAdjust = item },
                            onDelete = { viewModel.deleteShoppingItem(item.id) }
                        )
                    }
                }
            }

            // Empty state per ricerca senza risultati
            if (searchQuery.isNotBlank() && filteredUnpurchasedItems.isEmpty() && filteredPurchasedItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = com.example.ui.i18n.AppStrings.noFoodFoundTitle(currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = com.example.ui.i18n.AppStrings.noFoodFoundSubtitle(searchQuery, currentLanguage),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { searchQuery = "" }) {
                            Text(com.example.ui.i18n.AppStrings.showAllItems(currentLanguage))
                        }
                    }
                }
            }

            // Empty State se la lista è vuota
            if (items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = com.example.ui.i18n.AppStrings.emptyShoppingListTitle(currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.ui.i18n.AppStrings.emptyShoppingListSubtitle(currentLanguage),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Dialog: Inserimento Extra Manuale
        if (showAddExtraDialog) {
            AddManualExtraDialog(
                lang = currentLanguage,
                onConfirm = { name, qty, unit ->
                    viewModel.addManualShoppingExtra(name, qty, unit)
                    showAddExtraDialog = false
                },
                onDismiss = { showAddExtraDialog = false }
            )
        }

        // Dialog: Condivisione Lista Spesa con testo categorizzato
        if (showShareDialog) {
            val categorizedFormattedText = remember(items, currentLanguage) {
                if (items.isEmpty()) return@remember if (currentLanguage == com.example.data.model.AppLanguage.IT) "Lista della spesa vuota." else "Shopping list is empty."
                val headerTitle = if (currentLanguage == com.example.data.model.AppLanguage.IT) "🛒 Lista della Spesa - MealPlan\n\n" else "🛒 Shopping List - MealPlan\n\n"
                val sb = StringBuilder(headerTitle)
                FoodCategory.entries.forEach { cat ->
                    val catItems = items.filter { FoodCategory.categorize(it.name, it.isManualExtra) == cat }
                    if (catItems.isNotEmpty()) {
                        sb.append("${cat.iconEmoji} ${cat.getLocalizedName(currentLanguage)}:\n")
                        catItems.forEach {
                            val check = if (it.isPurchased) "✓" else "□"
                            val qtyStr = if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else String.format("%.1f", it.quantity)
                            val foodName = com.example.ui.i18n.AppStrings.translateFoodName(it.name, currentLanguage)
                            sb.append("$check $foodName: $qtyStr ${it.unit.getLocalizedShortName(currentLanguage)}\n")
                        }
                        sb.append("\n")
                    }
                }
                sb.toString()
            }

            ShareShoppingListDialog(
                lang = currentLanguage,
                formattedText = categorizedFormattedText,
                jsonText = viewModel.exportShoppingListAsJson(),
                onShareSystem = { textToShare ->
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, com.example.ui.i18n.AppStrings.dialogShareListTitle(currentLanguage))
                    context.startActivity(shareIntent)
                },
                onCopy = { textToCopy ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Lista Spesa", textToCopy)
                    clipboard.setPrimaryClip(clip)
                },
                onDismiss = { showShareDialog = false }
            )
        }

        // Dialog: Importazione e Integrazione/Merge
        if (showImportDialog) {
            ImportShoppingListDialog(
                lang = currentLanguage,
                onConfirmImport = { jsonInput, replaceExisting ->
                    viewModel.importShoppingListFromJson(jsonInput, replaceExisting)
                    showImportDialog = false
                },
                onDismiss = { showImportDialog = false }
            )
        }

        // Dialog: Modifica Quantità Reale & Avanzi in Dispensa
        if (itemToAdjust != null) {
            val item = itemToAdjust!!
            AdjustQuantityDialog(
                item = item,
                lang = currentLanguage,
                onSave = { actualQty, pantryQty ->
                    viewModel.updateShoppingItemQuantities(item.id, actualQty, pantryQty)
                    itemToAdjust = null
                },
                onDismiss = { itemToAdjust = null }
            )
        }
    }
}

/**
 * Badge compatti per mostrare chiaramente i giorni in cui l'alimento viene consumato.
 * Per gli articoli Extra aggiunti manualmente non viene mostrato nulla (dato che non sono nel piano).
 * Per gli alimenti del piano vengono usate sigle a massimo due caratteri (L, Ma, Me, G, V, S, D).
 */
@Composable
fun ConsumedDaysBadges(
    sourceDays: String,
    isManualExtra: Boolean,
    modifier: Modifier = Modifier
) {
    // Per gli alimenti e articoli aggiunti manualmente Extra, non servono i giorni dato che non sono nel piano alimentare
    if (isManualExtra) return

    if (sourceDays.isBlank()) return

    val tokens = remember(sourceDays) {
        sourceDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tokens.forEach { dayToken ->
            val shortDay = when (dayToken.trim().lowercase()) {
                "lun", "lunedì", "lunedi", "1", "l", "mon" -> "L"
                "mar", "martedì", "martedi", "2", "ma", "tue" -> "Ma"
                "mer", "mercoledì", "mercoledi", "3", "me", "wed" -> "Me"
                "gio", "giovedì", "giovedi", "4", "g", "thu" -> "G"
                "ven", "venerdì", "venerdi", "5", "v", "fri" -> "V"
                "sab", "sabato", "6", "s", "sat" -> "S"
                "dom", "domenica", "7", "d", "sun" -> "D"
                else -> dayToken.take(2)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SagePrimary.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, SagePrimary.copy(alpha = 0.45f))
            ) {
                Text(
                    text = shortDay,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SagePrimary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingListItemEntity,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onToggle: () -> Unit,
    onAdjustQuantity: () -> Unit,
    onDelete: () -> Unit
) {
    val hasPantryAdjustment = item.pantryQuantity > 0.0 || item.quantity != item.originalQuantity
    val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format("%.1f", item.quantity)
    val origStr = if (item.originalQuantity % 1.0 == 0.0) item.originalQuantity.toInt().toString() else String.format("%.1f", item.originalQuantity)
    val pantryStr = if (item.pantryQuantity % 1.0 == 0.0) item.pantryQuantity.toInt().toString() else String.format("%.1f", item.pantryQuantity)
    val unitStr = item.unit.getLocalizedShortName(lang)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        color = if (item.isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (item.isPurchased) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isPurchased,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SagePrimary
                    ),
                    modifier = Modifier.testTag("checkbox_${item.id.take(4)}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = com.example.ui.i18n.AppStrings.translateFoodName(item.name, lang),
                        fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isPurchased) Color.White.copy(alpha = 0.5f) else Color.White
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // 2ª RIGA: Chip Quantità (grammi/pezzi) affiancato dai badge dei giorni di consumo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAdjustQuantity() }
                                .testTag("btn_adjust_qty_${item.id.take(4)}"),
                            color = if (hasPantryAdjustment) CartaDaZuccheroContainer else SagePrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$qtyStr $unitStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = com.example.ui.i18n.AppStrings.dialogAdjustPantryTitle(lang),
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // Badge dei giorni di consumo (L, Ma, Me, G, V, S, D) affiancato alla quantità (solo per alimenti da piano)
                        if (!item.isManualExtra && item.sourceDays.isNotBlank()) {
                            ConsumedDaysBadges(
                                sourceDays = item.sourceDays,
                                isManualExtra = false
                            )
                        }
                    }

                    // Se è presente una regolazione per dispensa, mostra il dettaglio pianificati vs in dispensa
                    if (hasPantryAdjustment) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = com.example.ui.i18n.AppStrings.plannedVsPantryLabel(origStr, pantryStr, unitStr, lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = com.example.ui.i18n.AppStrings.deleteFood(lang),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Dialog per regolare la reale quantità necessaria in base agli avanzi in dispensa/frigo.
 */
@Composable
fun AdjustQuantityDialog(
    item: ShoppingListItemEntity,
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onSave: (actualQuantity: Double, pantryQuantity: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val origQty = item.originalQuantity
    val unit = item.unit.getLocalizedShortName(lang)

    var pantryInput by remember {
        mutableStateOf(if (item.pantryQuantity > 0.0) {
            if (item.pantryQuantity % 1.0 == 0.0) item.pantryQuantity.toInt().toString() else item.pantryQuantity.toString()
        } else "")
    }

    var actualInput by remember {
        mutableStateOf(
            if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
        )
    }

    val origStr = if (origQty % 1.0 == 0.0) origQty.toInt().toString() else String.format("%.1f", origQty)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(com.example.ui.i18n.AppStrings.dialogAdjustPantryTitle(lang), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = com.example.ui.i18n.AppStrings.translateFoodName(item.name, lang),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card riepilogo quantità pianificata
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(com.example.ui.i18n.AppStrings.foodNeedFromMeals(lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$origStr $unit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Text(
                    text = com.example.ui.i18n.AppStrings.adjustPantryExplanation(lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Campo Avanzi in Frigo / Dispensa
                OutlinedTextField(
                    value = pantryInput,
                    onValueChange = { input ->
                        pantryInput = input
                        val pVal = input.toDoubleOrNull() ?: 0.0
                        val toBuy = kotlin.math.max(0.0, origQty - pVal)
                        actualInput = if (toBuy % 1.0 == 0.0) toBuy.toInt().toString() else String.format("%.1f", toBuy)
                    },
                    label = { Text(com.example.ui.i18n.AppStrings.inPantryLabel(unit, lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pantry_qty"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo Quantità Effettiva da Comprare (modificabile anche a mano libera)
                OutlinedTextField(
                    value = actualInput,
                    onValueChange = { actualInput = it },
                    label = { Text(com.example.ui.i18n.AppStrings.realToBuyLabel(unit, lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_actual_qty"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Tasto rapido per ripristinare il valore originale
                TextButton(
                    onClick = {
                        pantryInput = ""
                        actualInput = if (origQty % 1.0 == 0.0) origQty.toInt().toString() else origQty.toString()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(com.example.ui.i18n.AppStrings.restoreOriginalQty(origStr, unit, lang), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actualQty = actualInput.toDoubleOrNull() ?: origQty
                    val pantryQty = pantryInput.toDoubleOrNull() ?: 0.0
                    onSave(actualQty, pantryQty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                modifier = Modifier.testTag("btn_save_adjusted_qty")
            ) {
                Text(com.example.ui.i18n.AppStrings.saveQuantity(lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(com.example.ui.i18n.AppStrings.cancel(lang))
            }
        }
    )
}

@Composable
fun AddManualExtraDialog(
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onConfirm: (name: String, quantity: Double, unit: QuantityUnit) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(QuantityUnit.PIECES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(com.example.ui.i18n.AppStrings.dialogAddExtraTitle(lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = com.example.ui.i18n.AppStrings.extraFoodDialogExplanation(lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(com.example.ui.i18n.AppStrings.extraFoodNameLabel(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_extra_name")
                )
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
                        modifier = Modifier.weight(1f).testTag("input_extra_qty"),
                        singleLine = true
                    )
                    FilterChip(
                        selected = selectedUnit == QuantityUnit.PIECES,
                        onClick = { selectedUnit = QuantityUnit.PIECES },
                        label = { Text(QuantityUnit.PIECES.getLocalizedName(lang)) }
                    )
                    FilterChip(
                        selected = selectedUnit == QuantityUnit.GRAMS,
                        onClick = { selectedUnit = QuantityUnit.GRAMS },
                        label = { Text(QuantityUnit.GRAMS.getLocalizedName(lang)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val qty = quantityInput.toDoubleOrNull() ?: 1.0
                        onConfirm(name.trim(), qty, selectedUnit)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text(com.example.ui.i18n.AppStrings.addExtraConfirm(lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(com.example.ui.i18n.AppStrings.cancel(lang))
            }
        }
    )
}

@Composable
fun ShareShoppingListDialog(
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    formattedText: String,
    jsonText: String,
    onShareSystem: (String) -> Unit,
    onCopy: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var shareAsJson by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(com.example.ui.i18n.AppStrings.dialogShareListTitle(lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = com.example.ui.i18n.AppStrings.shareListExplanation(lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !shareAsJson,
                        onClick = { shareAsJson = false },
                        label = { Text(com.example.ui.i18n.AppStrings.tabOrganizedText(lang)) }
                    )
                    FilterChip(
                        selected = shareAsJson,
                        onClick = { shareAsJson = true },
                        label = { Text(com.example.ui.i18n.AppStrings.tabJsonFormat(lang)) }
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (shareAsJson) jsonText else formattedText,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onShareSystem(if (shareAsJson) jsonText else formattedText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(com.example.ui.i18n.AppStrings.btnShareList(lang))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onCopy(if (shareAsJson) jsonText else formattedText)
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(com.example.ui.i18n.AppStrings.copy(lang))
            }
        }
    )
}

@Composable
fun ImportShoppingListDialog(
    lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.IT,
    onConfirmImport: (jsonInput: String, replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(com.example.ui.i18n.AppStrings.dialogImportTitle(lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = com.example.ui.i18n.AppStrings.importExplanation(lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    label = { Text("Payload JSON") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("input_import_json")
                )

                Text(
                    text = com.example.ui.i18n.AppStrings.importModeTitle(lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !replaceExisting,
                        onClick = { replaceExisting = false }
                    )
                    Text(com.example.ui.i18n.AppStrings.importModeMerge(lang), fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = replaceExisting,
                        onClick = { replaceExisting = true }
                    )
                    Text(com.example.ui.i18n.AppStrings.importModeReplace(lang), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonInput.isNotBlank()) {
                        onConfirmImport(jsonInput.trim(), replaceExisting)
                    }
                },
                enabled = jsonInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text(com.example.ui.i18n.AppStrings.importAction(lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(com.example.ui.i18n.AppStrings.cancel(lang))
            }
        }
    )
}
