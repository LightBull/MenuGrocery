package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DailyMealPlanScreen
import com.example.ui.screens.MealPlansManagementScreen
import com.example.ui.screens.ShoppingListScreen
import com.example.ui.theme.MealPlanTheme
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.viewmodel.MealPlanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealPlanTheme {
                MealPlanAppRoot()
            }
        }
    }
}

@Composable
fun MealPlanAppRoot(
    viewModel: MealPlanViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()

    val isEditorDirty by viewModel.isEditorDirty.collectAsState()

    val handleTabSelection = { targetTab: Int ->
        if (targetTab != selectedTab) {
            if (isEditorDirty) {
                viewModel.requestTabNavigation(targetTab)
            } else {
                selectedTab = targetTab
            }
        }
    }

    // Richiesta permessi di notifica per Android 13+ (POST_NOTIFICATIONS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Gestito internamente */ }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Mostra messaggi toast/feedback all'utente
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    // Gestione BackHandler per ritorno alla Home (Tab 0) dalle sotto-schermate (se editor non attivo/dirty)
    BackHandler(enabled = selectedTab != 0 && !isEditorDirty) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { handleTabSelection(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.RestaurantMenu else Icons.Outlined.RestaurantMenu,
                            contentDescription = "Piano Pasti"
                        )
                    },
                    label = { Text("Piano Pasti", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        indicatorColor = SagePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_meals")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { handleTabSelection(1) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                            contentDescription = "Lista Spesa"
                        )
                    },
                    label = { Text("Lista Spesa", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        indicatorColor = SagePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_shopping")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { handleTabSelection(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Layers else Icons.Outlined.Layers,
                            contentDescription = "I Miei Piani"
                        )
                    },
                    label = { Text("I Miei Piani", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        indicatorColor = SagePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_plans")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> DailyMealPlanScreen(
                    viewModel = viewModel,
                    onNavigateToPlans = { selectedTab = 2 }
                )
                1 -> ShoppingListScreen(
                    viewModel = viewModel
                )
                2 -> MealPlansManagementScreen(
                    viewModel = viewModel,
                    onPlanActivated = { selectedTab = 0 },
                    onNavigateToTab = { selectedTab = it }
                )
            }
        }
    }
}
