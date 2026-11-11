package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.WhatsNewDialog
import com.example.util.AppUpdateHelper
import com.example.data.model.AppReleaseNotesProvider
import com.example.data.model.ReleaseInfo
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AppLanguage
import com.example.data.model.SubscriptionTier
import com.example.ui.components.FoodSourceDot
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.CartaDaZucchero
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.viewmodel.MealPlanViewModel

import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.DefrostDiagnosticsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MealPlanViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val cloudAndLoginEnabled = false
    var showDefrostDiagnostics by remember { mutableStateOf(false) }
    var showReleaseNotesDialog by remember { mutableStateOf<ReleaseInfo?>(null) }
    var availableUpdateVersion by remember { mutableStateOf<Int?>(null) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.settingsTitle(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.back(currentLanguage),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sezione Account Google (mostrata solo se cloud/auth abilitato)
            if (cloudAndLoginEnabled && currentUser != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SagePrimaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initial = (currentUser?.displayName ?: currentUser?.email ?: "U").take(1).uppercase()
                                        Text(
                                            text = initial,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = SagePrimary
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: AppStrings.accountProfileTitle(currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (activity != null) {
                                            viewModel.signOut(activity) {
                                                onNavigateBack()
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_settings_sign_out"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppStrings.signOut(currentLanguage),
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sezione Abbonamento Cloud (mostrata solo se abilitata)
            if (cloudAndLoginEnabled) {
                item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = AppStrings.subscriptionSectionTitle(currentLanguage),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (subscriptionState.tier == SubscriptionTier.PREMIUM)
                                            AppStrings.subscriptionStatusActive(currentLanguage)
                                        else
                                            AppStrings.subscriptionStatusFree(currentLanguage),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (subscriptionState.tier == SubscriptionTier.PREMIUM)
                                            SagePrimary
                                        else
                                            Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (subscriptionState.tier == SubscriptionTier.PREMIUM)
                                    SagePrimaryContainer.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (subscriptionState.tier == SubscriptionTier.PREMIUM) SagePrimary else Color.Gray.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (subscriptionState.tier == SubscriptionTier.PREMIUM) "PREMIUM" else "FREE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (subscriptionState.tier == SubscriptionTier.PREMIUM) SagePrimary else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Banner informativo sulla simulazione
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = AppStrings.subscriptionSimulatedBadge(currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppStrings.subscriptionSimulatedDesc(currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggle di test simulazione
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.subscriptionToggleTitle(currentLanguage),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = AppStrings.subscriptionToggleSubtitle(currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Switch(
                                checked = subscriptionState.tier == SubscriptionTier.PREMIUM,
                                onCheckedChange = { isChecked ->
                                    viewModel.setSubscriptionTier(
                                        if (isChecked) SubscriptionTier.PREMIUM else SubscriptionTier.FREE
                                    )
                                },
                                modifier = Modifier.testTag("switch_subscription_tier"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SagePrimary,
                                    checkedTrackColor = SagePrimary.copy(alpha = 0.4f)
                                )
                            )
                        }

                        if (subscriptionState.tier == SubscriptionTier.PREMIUM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.syncWithCloud { up, down ->
                                        viewModel.postFeedbackMessage(
                                            if (currentLanguage == AppLanguage.IT)
                                                "Sincronizzazione completata: $up caricati, $down scaricati"
                                            else
                                                "Sync completed: $up uploaded, $down downloaded"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_manual_cloud_sync"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SagePrimaryContainer,
                                    contentColor = SagePrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Sincronizza Cloud Adesso" else "Sync Cloud Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

            // Sezione Lingua
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SagePrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = AppStrings.languageSectionTitle(currentLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = AppStrings.languageSectionSubtitle(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Opzione Italiano
                        LanguageOptionCard(
                            language = AppLanguage.IT,
                            title = "Italiano",
                            description = "Interfaccia in italiano • Ricerca prioritaria colonna 'name'",
                            isSelected = currentLanguage == AppLanguage.IT,
                            onClick = { viewModel.setLanguage(AppLanguage.IT) },
                            testTag = "radio_lang_it"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opzione Inglese
                        LanguageOptionCard(
                            language = AppLanguage.EN,
                            title = "English",
                            description = "English interface • Priority search in 'english_name' column",
                            isSelected = currentLanguage == AppLanguage.EN,
                            onClick = { viewModel.setLanguage(AppLanguage.EN) },
                            testTag = "radio_lang_en"
                        )
                    }
                }
            }

            // Sezione Database Alimenti (Sola Lettura)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Dataset,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = AppStrings.foodDbSectionTitle(currentLanguage),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Room ORM Pre-packaged Asset",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF388E3C).copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = AppStrings.foodDbReadOnlyBadge(currentLanguage),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5D6A7)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (currentLanguage == AppLanguage.IT)
                                "L'applicazione utilizza due database SQLite su disco:\n1. Database Principale dei Piani (Room ORM, scrittura/lettura)\n2. Database Alimenti Preconfezionato (FoodDb.db, sola lettura)"
                            else
                                "The app uses two on-disk SQLite databases:\n1. Main Meal Plan Database (Room ORM, read/write)\n2. Pre-packaged Food Database (FoodDb.db, read-only)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Percorso Database Principale (mealplan_database):" else "Main Database Path (mealplan_database):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5D6A7)
                                )
                                Text(
                                    text = context.getDatabasePath("mealplan_database").absolutePath,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Cartella Backup di sicurezza:" else "Safety Backups Directory:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CartaDaZucchero
                                )
                                Text(
                                    text = "${context.filesDir.absolutePath}/backups/",
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Percorso Database Alimenti (FoodDb.db):" else "Food Database Path (FoodDb.db):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = context.getDatabasePath("FoodDb.db").absolutePath,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Sezione Legenda Indicatori Colori
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentLanguage == AppLanguage.IT) "Legenda Origine Alimenti" else "Food Origin Legend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FoodSourceDot(source = "Local")
                            Column {
                                Text(
                                    text = AppStrings.sourceLocalLabel(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Alimenti inseriti dall'utente nei pasti precedenti" else "Foods previously entered in user meals",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FoodSourceDot(source = "FoodDb")
                            Column {
                                Text(
                                    text = AppStrings.sourceFoodDbLabel(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Alimenti da FoodDb.db (principal_food_tables o food_back_archive)" else "Foods from FoodDb.db (principal_food_tables or food_back_archive)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Sezione Gestione Backup e Ripristino Dati Personali
            item {
                val availableBackups by viewModel.availableBackups.collectAsState()
                var selectedBackupToRestore by remember { mutableStateOf<java.io.File?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.loadAvailableBackups()
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SagePrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Backup,
                                            contentDescription = null,
                                            tint = SagePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.IT) "Backup & Ripristino Dati" else "Data Backup & Restore",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (currentLanguage == AppLanguage.IT) "Salvataggi di sicurezza sul dispositivo" else "On-device safety backups",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { viewModel.createManualBackup() },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_create_manual_backup")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Nuovo" else "New",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (currentLanguage == AppLanguage.IT)
                                "I backup salvano integralmente i tuoi piani, pasti e alimenti. Ripristinando un backup, i dati vengono automaticamente adattati allo schema attuale e integrati con la tabella vettoriale ONNX."
                            else
                                "Backups store your plans, meals, and foods. Restoring adapts records to the new schema and populates ONNX vector embeddings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (availableBackups.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Nessun backup trovato nella cartella locale." else "No local backups found.",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                availableBackups.forEach { backupFile ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = backupFile.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                                val dateFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                                                    .format(java.util.Date(backupFile.lastModified()))
                                                Text(
                                                    text = "${backupFile.length() / 1024} KB • $dateFormatted",
                                                    fontSize = 10.sp,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                            }

                                            Button(
                                                onClick = { selectedBackupToRestore = backupFile },
                                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("btn_restore_${backupFile.name}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.IT) "Ripristina" else "Restore",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dialog di conferma ripristino
                        selectedBackupToRestore?.let { file ->
                            AlertDialog(
                                onDismissRequest = { selectedBackupToRestore = null },
                                title = {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.IT) "Conferma Ripristino Dati" else "Confirm Data Restore",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                text = {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.IT)
                                            "Vuoi ripristinare i dati dal file:\n\n${file.name}\n\nI dati attuali verranno integrati e aggiornati con la nuova struttura e il motore vettoriale ONNX."
                                        else
                                            "Do you want to restore data from:\n\n${file.name}?\n\nCurrent data will be merged and ONNX vector embeddings regenerated."
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.restoreBackup(file)
                                            selectedBackupToRestore = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                                    ) {
                                        Text(if (currentLanguage == AppLanguage.IT) "Sì, Ripristina" else "Yes, Restore", color = Color.White)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { selectedBackupToRestore = null }) {
                                        Text(if (currentLanguage == AppLanguage.IT) "Annulla" else "Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Sezione Diagnostica Promemoria Scongelamento (spostata da I Miei Piani)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CartaDaZucchero.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = CartaDaZucchero,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = AppStrings.defrostDiagnostics(currentLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT)
                                        "Verifica stato promemoria, orari pianificati e test allarmi"
                                    else
                                        "Check reminders status, scheduled times and test alarms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showDefrostDiagnostics = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_defrost_diagnostics"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = CartaDaZucchero
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.IT) "Apri Diagnostica Scongelamento" else "Open Defrost Diagnostics",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Sezione Informazioni Versione e Aggiornamenti
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Versione applicazione" else "Application version",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SagePrimary
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    isCheckingUpdates = true
                                    AppUpdateHelper.checkForUpdate(
                                        context = context,
                                        onUpdateAvailable = { availableVersion ->
                                            isCheckingUpdates = false
                                            availableUpdateVersion = availableVersion
                                        },
                                        onNoUpdateAvailable = {
                                            isCheckingUpdates = false
                                            viewModel.postFeedbackMessage(
                                                if (currentLanguage == AppLanguage.IT)
                                                    "L'applicazione è già aggiornata all'ultima versione disponibile!"
                                                else
                                                    "The application is already up to date!"
                                            )
                                        },
                                        onError = {
                                            isCheckingUpdates = false
                                            viewModel.postFeedbackMessage(
                                                if (currentLanguage == AppLanguage.IT)
                                                    "Controllo aggiornamenti completato: app aggiornata alla v${com.example.BuildConfig.VERSION_NAME}"
                                                else
                                                    "Update check completed: app is on v${com.example.BuildConfig.VERSION_NAME}"
                                            )
                                        }
                                    )
                                },
                                enabled = !isCheckingUpdates,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_check_updates_settings")
                            ) {
                                if (isCheckingUpdates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = SagePrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT) "Verifica" else "Check",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tasto per consultare le novità della versione
                        TextButton(
                            onClick = {
                                showReleaseNotesDialog = AppReleaseNotesProvider.getReleaseInfo(com.example.BuildConfig.VERSION_CODE)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_view_whats_new"),
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.IT)
                                        "Visualizza le novità introdotte in questa versione"
                                    else
                                        "View what's new in this version",
                                    fontSize = 12.sp,
                                    color = SagePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDefrostDiagnostics) {
        DefrostDiagnosticsBottomSheet(
            viewModel = viewModel,
            onDismiss = { showDefrostDiagnostics = false }
        )
    }

    availableUpdateVersion?.let { newVersionCode ->
        AppUpdateDialog(
            availableVersionCode = newVersionCode,
            lang = currentLanguage,
            onDismiss = { availableUpdateVersion = null },
            onConfirmUpdate = {
                availableUpdateVersion = null
                AppUpdateHelper.openPlayStore(context)
            }
        )
    }

    showReleaseNotesDialog?.let { releaseInfo ->
        WhatsNewDialog(
            releaseInfo = releaseInfo,
            lang = currentLanguage,
            onConfirm = { showReleaseNotesDialog = null }
        )
    }
}

@Composable
private fun LanguageOptionCard(
    language: AppLanguage,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val borderColor = if (isSelected) SagePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val bgColor = if (isSelected) SagePrimaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = language.flag,
                    fontSize = 22.sp
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SagePrimary,
                    unselectedColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}
