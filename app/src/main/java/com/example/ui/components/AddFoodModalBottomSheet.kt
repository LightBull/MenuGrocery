package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItemModel
import com.example.data.model.MealType
import com.example.data.model.QuantityUnit
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.SagePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodModalBottomSheet(
    sheetState: SheetState,
    targetMealType: MealType,
    targetMealId: String,
    searchResults: List<FoodItemModel>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onConfirmAdd: (
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
        mealType: MealType
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf(QuantityUnit.GRAMS) }
    var toDefrost by remember { mutableStateOf(false) }

    // Valori nutrizionali base per 100g o per porzione
    var baseCalories by remember { mutableDoubleStateOf(0.0) }
    var baseProtein by remember { mutableDoubleStateOf(0.0) }
    var baseCarbs by remember { mutableDoubleStateOf(0.0) }
    var baseFat by remember { mutableDoubleStateOf(0.0) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showAllResults by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Aggiungi a ${targetMealType.displayName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Cerca un alimento o inserisci i dati manualmente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo di ricerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showAllResults = false
                    onSearchQueryChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_search_input"),
                label = { Text("Cerca Alimento") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Cerca")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showAllResults = false
                            onSearchQueryChanged("")
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Cancella")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Indicatore di ricerca e lista risultati
            if (isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerca Alimento in corso...", fontSize = 12.sp, color = Color.White)
                }
            } else if (searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Tocca per selezionare:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val displayedResults = if (showAllResults) searchResults else searchResults.take(5)
                        displayedResults.forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        foodName = result.name
                                        brand = result.brand ?: ""
                                        baseCalories = result.calories
                                        baseProtein = result.protein
                                        baseCarbs = result.carbs
                                        baseFat = result.fat
                                        selectedImageUrl = result.imageUrl
                                        searchQuery = ""
                                        showAllResults = false
                                        onSearchQueryChanged("")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FoodSourceDot(source = result.source)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    val srcDesc = if (result.source == "Local") "Archivio locale (già usato)" else result.source
                                    val brandText = if (!result.brand.isNullOrBlank()) "${result.brand} • " else ""
                                    Text(
                                        text = "$brandText$srcDesc • ${result.calories.toInt()} kcal/100g",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }

                        if (searchResults.size > 5) {
                            TextButton(
                                onClick = { showAllResults = !showAllResults },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_toggle_expand_results_sheet")
                            ) {
                                Text(
                                    text = if (showAllResults) "Mostra meno" else "Visualizza tutti (${searchResults.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nome Alimento
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_name_input"),
                label = { Text("Nome Alimento *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Marca (opzionale)
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Marca (opzionale)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quantità e Unità di Misura (Grammi o Pezzi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quantity_input"),
                    label = { Text("Quantità *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedUnit == QuantityUnit.GRAMS,
                        onClick = { selectedUnit = QuantityUnit.GRAMS },
                        label = { Text("Grammi (g)") },
                        modifier = Modifier.testTag("chip_unit_grams")
                    )
                    FilterChip(
                        selected = selectedUnit == QuantityUnit.PIECES,
                        onClick = { selectedUnit = QuantityUnit.PIECES },
                        label = { Text("Pezzi (pz)") },
                        modifier = Modifier.testTag("chip_unit_pieces")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sezione Notifiche Push Locali di Scongelamento (BR-03)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "Scongelamento",
                            tint = CartaDaZucchero,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Da scongelare",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Notifica automatica alle 20:00 della sera precedente",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Switch(
                        checked = toDefrost,
                        onCheckedChange = { toDefrost = it },
                        modifier = Modifier.testTag("switch_to_defrost"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CartaDaZucchero,
                            checkedTrackColor = CartaDaZucchero.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anteprima Macronutrienti Calcolati per la porzione scelta
            val qty = quantityInput.toDoubleOrNull() ?: 100.0
            val factor = if (selectedUnit == QuantityUnit.GRAMS) qty / 100.0 else qty
            val calcCal = baseCalories * factor
            val calcProt = baseProtein * factor
            val calcCarb = baseCarbs * factor
            val calcFat = baseFat * factor

            if (baseCalories > 0.0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("${calcCal.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Prot: ${calcProt.toInt()}g", fontSize = 12.sp)
                        Text("Carb: ${calcCarb.toInt()}g", fontSize = 12.sp)
                        Text("Gras: ${calcFat.toInt()}g", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tasto Conferma Aggiunta
            Button(
                onClick = {
                    if (foodName.isNotBlank()) {
                        val finalQty = quantityInput.toDoubleOrNull() ?: 1.0
                        onConfirmAdd(
                            targetMealId,
                            foodName.trim(),
                            brand.ifBlank { null },
                            finalQty,
                            selectedUnit,
                            toDefrost,
                            baseCalories,
                            baseProtein,
                            baseCarbs,
                            baseFat,
                            selectedImageUrl,
                            targetMealType
                        )
                        onDismiss()
                    }
                },
                enabled = foodName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_confirm_add_food"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SagePrimary
                )
            ) {
                Text(
                    text = "Aggiungi a ${targetMealType.displayName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
