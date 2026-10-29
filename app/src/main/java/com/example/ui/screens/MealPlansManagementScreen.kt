package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.components.DefrostDiagnosticsBottomSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

    // Stato per apertura dell'Editor a schermo intero:
    // null = mostra lista piani, oppure MealPlanEntity per modifica, o nuovo piano
    var editingPlan by remember { mutableStateOf<MealPlanEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showResetDbDialog by remember { mutableStateOf(false) }
    var showDefrostDiagnostics by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gestione Piani Alimentari",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crea, personalizza e attiva i tuoi piani alimentari. I pasti giornalieri e la lista della spesa rifletteranno sempre il piano contrassegnato come attivo.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showDefrostDiagnostics = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_defrost_diagnostics"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diagnostica Scongelamento & Notifiche", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showResetDbDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_reset_database"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ricrea Database (Versione Attuale)", fontSize = 13.sp)
                        }
                    }
                }
            }

            items(plans, key = { it.id }) { plan ->
                val isActive = plan.id == activePlan?.id
                MealPlanCard(
                    plan = plan,
                    isActive = isActive,
                    canDelete = plans.size > 1 && !isActive,
                    onActivate = {
                        viewModel.activatePlan(plan.id)
                        onPlanActivated()
                    },
                    onEdit = {
                        editingPlan = plan
                    },
                    onDuplicate = {
                        viewModel.duplicatePlan(plan.id)
                    },
                    onDelete = { viewModel.deletePlan(plan) }
                )
            }
        }

        // FAB per Creare Nuovo Piano (apre l'editor completo con i pasti della settimana)
        ExtendedFloatingActionButton(
            onClick = { isCreatingNew = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("fab_create_plan"),
            containerColor = SagePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuovo Piano", fontWeight = FontWeight.Bold)
        }

        if (showResetDbDialog) {
            AlertDialog(
                onDismissRequest = { showResetDbDialog = false },
                title = { Text("Ricreare il Database?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Questa operazione eliminerà tutti i dati e ricreerà le tabelle secondo la versione attuale dello schema (versione 2 con gestione avanzata della spesa, calcolatore dispensa e categorie), ripopolando i pasti di prova predefiniti.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetDbDialog = false
                            viewModel.resetDatabaseToDefault()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Distruggi e Ricrea", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDbDialog = false }) {
                        Text("Annulla")
                    }
                }
            )
        }

        if (showDefrostDiagnostics) {
            DefrostDiagnosticsBottomSheet(
                viewModel = viewModel,
                onDismiss = { showDefrostDiagnostics = false }
            )
        }
    }
}

@Composable
fun MealPlanCard(
    plan: MealPlanEntity,
    isActive: Boolean,
    canDelete: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                                    text = "PIANO ATTIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(plan.createdAt))
                    Text(
                        text = "Creato il $dateStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("btn_edit_plan_${plan.id.take(4)}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifica pasti e piano",
                            tint = SagePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplica piano",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina piano",
                                tint = TerracottaAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (!plan.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = plan.notes,
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
                    Text("Imposta come Attivo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = SagePrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Questo piano governa i pasti attuali e la lista della spesa",
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
