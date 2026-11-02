package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayOfWeekInfo
import com.example.data.model.FoodCategory
import com.example.data.model.MealType
import com.example.data.model.NutritionalSummary
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.CartaDaZuccheroContainer
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.TerracottaAccent

@Composable
fun DaySelectorRow(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DayOfWeekInfo.ALL_DAYS) { dayInfo ->
            val isSelected = dayInfo.dayIndex == selectedDay

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDaySelected(dayInfo.dayIndex) }
                    .testTag("day_chip_${dayInfo.shortName}"),
                color = if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayInfo.shortName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dayInfo.name.take(3),
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyNutritionalCard(
    summary: NutritionalSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Calorie",
                        tint = TerracottaAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${summary.calories.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Totale stimato del giorno",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroPill(label = "Prot", value = "${summary.protein.toInt()}g", color = SagePrimary)
                MacroPill(label = "Carb", value = "${summary.carbs.toInt()}g", color = CartaDaZucchero)
                MacroPill(label = "Gras", value = "${summary.fat.toInt()}g", color = TerracottaAccent)
            }
        }
    }
}

@Composable
fun MacroPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 4.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Badge visivo per alimenti con promemoria di scongelamento attivo.
 * Di default nelle liste pasti mostra solo l'icona compatta (showText = false).
 */
@Composable
fun DefrostBadge(
    modifier: Modifier = Modifier,
    showText: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = CartaDaZuccheroContainer,
        contentColor = CartaDaZucchero
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (showText) 8.dp else 5.dp,
                vertical = if (showText) 4.dp else 3.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AcUnit,
                contentDescription = "Da scongelare alle 20:00",
                modifier = Modifier.size(13.dp),
                tint = CartaDaZucchero
            )
            if (showText) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Da scongelare (20:00)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Indicatore cromatico di provenienza dell'alimento:
 * Giallo (#FFD54F) = Locale (inserito in precedenza nei pasti)
 * Rosso (#E57373) = Database USDA
 */
@Composable
fun FoodSourceDot(
    source: String?,
    modifier: Modifier = Modifier
) {
    val isLocal = source == "Local"
    val dotColor = if (isLocal) Color(0xFFFFD54F) else Color(0xFFE57373)
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}

/**
 * Selettore a tendina Material 3 per la Categoria Alimento (assegnata a corsia spesa)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorDropdown(
    selectedCategory: FoodCategory,
    onCategorySelected: (FoodCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria (Corsia Supermercato)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag("dropdown_category_selector"),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FoodCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.iconEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(category.displayName, fontSize = 13.sp, fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Dialog Material 3 per la duplicazione flessibile di un alimento su più giorni e pasti.
 */
@Composable
fun CopyFoodItemDialog(
    foodName: String,
    onDismiss: () -> Unit,
    onConfirmCopy: (targetDays: List<Int>, targetMealTypes: List<MealType>) -> Unit
) {
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedMealTypes by remember { mutableStateOf(setOf<MealType>()) }

    val allDays = DayOfWeekInfo.ALL_DAYS.map { it.dayIndex }
    val allSelectedDays = selectedDays.size == allDays.size
    val allMealTypes = MealType.entries
    val allSelectedMeals = selectedMealTypes.size == allMealTypes.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Copia Alimento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = foodName,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sezione Giorni
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Giorni di destinazione",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedDays = if (allSelectedDays) emptySet() else allDays.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (allSelectedDays) "Deseleziona tutti" else "Tutti i giorni",
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Griglia/Row di chip per i 7 giorni
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeekInfo.ALL_DAYS.forEach { dayInfo ->
                            val isSelected = selectedDays.contains(dayInfo.dayIndex)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInfo.dayIndex
                                    } else {
                                        selectedDays + dayInfo.dayIndex
                                    }
                                },
                                label = { Text(dayInfo.shortName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sezione Pasti
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pasti di destinazione",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedMealTypes = if (allSelectedMeals) emptySet() else allMealTypes.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (allSelectedMeals) "Deseleziona tutti" else "Tutti i pasti",
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MealType.entries.forEach { mealType ->
                            val isSelected = selectedMealTypes.contains(mealType)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMealTypes = if (isSelected) {
                                            selectedMealTypes - mealType
                                        } else {
                                            selectedMealTypes + mealType
                                        }
                                    },
                                color = if (isSelected) SagePrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedMealTypes = if (checked) selectedMealTypes + mealType else selectedMealTypes - mealType
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = mealType.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                val totalCopies = selectedDays.size * selectedMealTypes.size
                if (totalCopies > 0) {
                    Text(
                        text = "Verrà copiato su $totalCopies pasti complessivi.",
                        fontSize = 11.sp,
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmCopy(selectedDays.toList().sorted(), selectedMealTypes.toList())
                },
                enabled = selectedDays.isNotEmpty() && selectedMealTypes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text("Copia Alimento", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

/**
 * Dialog Material 3 per copiare l'intero giorno (tutti i pasti e alimenti) verso uno o più giorni selezionati.
 */
@Composable
fun CopyDayDialog(
    sourceDayIndex: Int,
    sourceDayName: String,
    totalItemsCount: Int,
    onDismiss: () -> Unit,
    onConfirmCopy: (targetDays: List<Int>) -> Unit
) {
    val availableDays = DayOfWeekInfo.ALL_DAYS.filter { it.dayIndex != sourceDayIndex }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    val allSelectedDays = selectedDays.size == availableDays.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Copia Intero Giorno",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Da $sourceDayName ($totalItemsCount alimenti configurati)",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giorni di destinazione",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            selectedDays = if (allSelectedDays) emptySet() else availableDays.map { it.dayIndex }.toSet()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (allSelectedDays) "Deseleziona tutti" else "Tutti gli altri giorni",
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = "Tutti i pasti di $sourceDayName (Colazione, Spuntini, Pranzo, Cena) verranno copiati nei giorni selezionati aggiungendosi agli alimenti già configurati.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableDays.forEach { dayInfo ->
                        val isSelected = selectedDays.contains(dayInfo.dayIndex)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInfo.dayIndex
                                    } else {
                                        selectedDays + dayInfo.dayIndex
                                    }
                                },
                            color = if (isSelected) SagePrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedDays = if (checked) selectedDays + dayInfo.dayIndex else selectedDays - dayInfo.dayIndex
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dayInfo.fullName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                if (selectedDays.isNotEmpty()) {
                    Text(
                        text = "Verranno copiati $totalItemsCount alimenti su ${selectedDays.size} giorno/i.",
                        fontSize = 11.sp,
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmCopy(selectedDays.toList().sorted())
                },
                enabled = selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text("Copia Giorno", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

/**
 * Dialog Material 3 per copiare l'intero pasto verso uno o più giorni e tipi di pasto selezionati.
 */
@Composable
fun CopyMealDialog(
    mealName: String,
    dayName: String,
    totalItemsCount: Int,
    initialMealType: MealType,
    onDismiss: () -> Unit,
    onConfirmCopy: (targetDays: List<Int>, targetMealTypes: List<MealType>) -> Unit
) {
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedMealTypes by remember { mutableStateOf(setOf(initialMealType)) }

    val allDays = DayOfWeekInfo.ALL_DAYS.map { it.dayIndex }
    val allSelectedDays = selectedDays.size == allDays.size
    val allMealTypes = MealType.entries
    val allSelectedMeals = selectedMealTypes.size == allMealTypes.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Copia Intero Pasto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$mealName di $dayName ($totalItemsCount alimenti)",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sezione Giorni di destinazione
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Giorni di destinazione",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedDays = if (allSelectedDays) emptySet() else allDays.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (allSelectedDays) "Deseleziona tutti" else "Tutti i giorni",
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Griglia/Row di chip per i 7 giorni
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeekInfo.ALL_DAYS.forEach { dayInfo ->
                            val isSelected = selectedDays.contains(dayInfo.dayIndex)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayInfo.dayIndex
                                    } else {
                                        selectedDays + dayInfo.dayIndex
                                    }
                                },
                                label = { Text(dayInfo.shortName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sezione Pasti di destinazione
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pasti di destinazione",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedMealTypes = if (allSelectedMeals) emptySet() else allMealTypes.toSet()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (allSelectedMeals) "Deseleziona tutti" else "Tutti i pasti",
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MealType.entries.forEach { mealType ->
                            val isSelected = selectedMealTypes.contains(mealType)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMealTypes = if (isSelected) {
                                            selectedMealTypes - mealType
                                        } else {
                                            selectedMealTypes + mealType
                                        }
                                    },
                                color = if (isSelected) SagePrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedMealTypes = if (checked) selectedMealTypes + mealType else selectedMealTypes - mealType
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = mealType.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                val totalTargetMeals = selectedDays.size * selectedMealTypes.size
                if (totalTargetMeals > 0) {
                    Text(
                        text = "I $totalItemsCount alimenti verranno copiati su $totalTargetMeals pasti selezionati.",
                        fontSize = 11.sp,
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmCopy(selectedDays.toList().sorted(), selectedMealTypes.toList())
                },
                enabled = selectedDays.isNotEmpty() && selectedMealTypes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text("Copia Pasto", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}


