package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.example.data.model.GradualityPlanConfig
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MealPlanEntity
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.TerracottaAccent
import com.example.ui.viewmodel.MealPlanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MealPlansManagementScreen(
    viewModel: MealPlanViewModel,
    onPlanActivated: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val plans by viewModel.allPlans.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    // Stato per apertura dell'Editor a schermo intero
    var editingPlan by remember { mutableStateOf<MealPlanEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    // Dialoghi di conferma swipe
    var planToDuplicate by remember { mutableStateOf<MealPlanEntity?>(null) }
    var planToDelete by remember { mutableStateOf<MealPlanEntity?>(null) }

    if (isCreatingNew) {
        PlanEditorScreen(
            planId = null,
            viewModel = viewModel,
            onBack = { isCreatingNew = false },
            onNavigateToTab = { targetTab ->
                isCreatingNew = false
                onNavigateToTab(targetTab)
            }
        )
        return
    }

    if (editingPlan != null) {
        val plan = editingPlan!!
        PlanEditorScreen(
            planId = plan.id,
            initialTitle = plan.title,
            initialNotes = plan.notes,
            initialIsActive = plan.id == activePlan?.id,
            initialGradualityConfigJson = plan.gradualityConfigJson,
            viewModel = viewModel,
            onBack = { editingPlan = null },
            onNavigateToTab = { targetTab ->
                editingPlan = null
                onNavigateToTab(targetTab)
            }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(plans, key = { it.id }) { plan ->
                val isActive = plan.id == activePlan?.id
                SwipeableMealPlanCard(
                    plan = plan,
                    isActive = isActive,
                    lang = currentLanguage,
                    onEdit = { editingPlan = plan },
                    onActivate = {
                        viewModel.activatePlan(plan.id)
                        onPlanActivated()
                    },
                    onRequestDuplicate = { planToDuplicate = plan },
                    onRequestDelete = { planToDelete = plan }
                )
            }
        }

        // FAB per Creare Nuovo Piano (circolare con sola icona +)
        FloatingActionButton(
            onClick = { isCreatingNew = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("fab_create_plan"),
            containerColor = SagePrimary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = com.example.ui.i18n.AppStrings.btnNewPlan(currentLanguage),
                modifier = Modifier.size(26.dp)
            )
        }

        // Popup di conferma Duplicazione Piano (Swipe verso destra)
        if (planToDuplicate != null) {
            val target = planToDuplicate!!
            AlertDialog(
                onDismissRequest = { planToDuplicate = null },
                title = {
                    Text(
                        if (currentLanguage == com.example.data.model.AppLanguage.IT) "Copia Piano Alimentare" else "Duplicate Meal Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        if (currentLanguage == com.example.data.model.AppLanguage.IT)
                            "Vuoi creare una copia del piano '${target.title}'? Tutti i pasti e gli alimenti verranno duplicati nel nuovo piano."
                        else
                            "Do you want to create a copy of '${target.title}'? All meals and items will be copied to the new plan."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = target.id
                            planToDuplicate = null
                            viewModel.duplicatePlan(id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text(
                            if (currentLanguage == com.example.data.model.AppLanguage.IT) "Copia" else "Duplicate",
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { planToDuplicate = null }) {
                        Text(com.example.ui.i18n.AppStrings.cancel(currentLanguage))
                    }
                }
            )
        }

        // Popup di conferma Eliminazione Piano (Swipe verso sinistra)
        if (planToDelete != null) {
            val target = planToDelete!!
            val isTargetActive = target.id == activePlan?.id
            val isOnlyPlan = plans.size <= 1

            AlertDialog(
                onDismissRequest = { planToDelete = null },
                title = {
                    Text(
                        if (currentLanguage == com.example.data.model.AppLanguage.IT) "Elimina Piano Alimentare" else "Delete Meal Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                                "Sei sicuro di voler eliminare definitivamente il piano '${target.title}'?"
                            else
                                "Are you sure you want to permanently delete '${target.title}'?"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Riassunto dello stato attuale del piano
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTargetActive || isOnlyPlan) TerracottaAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (currentLanguage == com.example.data.model.AppLanguage.IT) "Stato attuale del piano:" else "Current plan status:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isTargetActive || isOnlyPlan) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (isTargetActive) {
                                    Text(
                                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                                            "• Questo è attualmente il PIANO ATTIVO. Eliminandolo, i pasti giornalieri e la lista della spesa non avranno più un piano di riferimento fino all'attivazione di uno nuovo."
                                        else
                                            "• This is currently the ACTIVE PLAN. Deleting it leaves meals and shopping without an active reference.",
                                        fontSize = 11.sp,
                                        color = TerracottaAccent
                                    )
                                } else {
                                    Text(
                                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                                            "• Questo piano è attualmente INATTIVO."
                                        else
                                            "• This plan is currently INACTIVE.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isOnlyPlan) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (currentLanguage == com.example.data.model.AppLanguage.IT)
                                            "• Questo è l'UNICO piano salvato. Eliminandolo la lista dei piani rimarrà vuota finché non ne crei un altro."
                                        else
                                            "• This is the ONLY plan saved. Deleting it will leave the plans list empty.",
                                        fontSize = 11.sp,
                                        color = TerracottaAccent
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val plan = target
                            planToDelete = null
                            viewModel.deletePlan(plan)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            com.example.ui.i18n.AppStrings.btnDelete(currentLanguage),
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { planToDelete = null }) {
                        Text(com.example.ui.i18n.AppStrings.cancel(currentLanguage))
                    }
                }
            )
        }
    }
}

@Composable
fun SwipeableMealPlanCard(
    plan: MealPlanEntity,
    isActive: Boolean,
    lang: com.example.data.model.AppLanguage,
    onEdit: () -> Unit,
    onActivate: () -> Unit,
    onRequestDuplicate: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe verso destra dalla metà in poi -> Copia piano con conferma
                    onRequestDuplicate()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe verso sinistra dalla metà in poi -> Elimina piano con conferma e stato
                    onRequestDelete()
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
                if (lang == com.example.data.model.AppLanguage.IT) "Copia" else "Duplicate"
            } else {
                if (lang == com.example.data.model.AppLanguage.IT) "Elimina" else "Delete"
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (isStartToEnd || isEndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isStartToEnd) {
                            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
                            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
                        }
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .testTag("plan_card_${plan.id.take(4)}"),
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) SagePrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SagePrimary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = com.example.ui.i18n.AppStrings.activePlanBadge(lang).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        val hasGraduality = remember(plan.gradualityConfigJson) {
                            GradualityPlanConfig.fromJsonString(plan.gradualityConfigJson).hasActiveGraduality()
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = com.example.ui.i18n.AppStrings.translatePlanTitle(plan.title, lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (hasGraduality) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SagePrimaryContainer,
                                    modifier = Modifier.testTag("badge_graduality_${plan.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = if (lang == com.example.data.model.AppLanguage.IT) "Gradualità attiva" else "Graduality active",
                                            tint = SagePrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (lang == com.example.data.model.AppLanguage.IT) "Gradualità" else "Graduality",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SagePrimary
                                        )
                                    }
                                }
                            }
                        }

                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(plan.createdAt))
                        Text(
                            text = com.example.ui.i18n.AppStrings.createdOn(dateStr, lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val notes = com.example.ui.i18n.AppStrings.translatePlanNotes(plan.notes, lang)
                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isActive) {
                    Button(
                        onClick = onActivate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_activate_plan_${plan.id.take(4)}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text(com.example.ui.i18n.AppStrings.btnSetActive(lang), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = SagePrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (lang == com.example.data.model.AppLanguage.IT) "Questo piano governa i pasti attuali e la lista della spesa" else "This plan governs current meals and the shopping list",
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                            fontSize = 11.sp,
                            color = SagePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
