package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ReleaseInfo
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer

/**
 * Dialog di notifica disponibilità aggiornamento su Google Play Store.
 * Mostra solo la nuova versione disponibile con i tasti "Aggiorna" e "Chiudi",
 * senza anticipare le novità della nuova versione non ancora installata.
 */
@Composable
fun AppUpdateDialog(
    availableVersionCode: Int,
    availableVersionName: String? = null,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayVersion = availableVersionName ?: "$availableVersionCode.0"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_app_update"),
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SagePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (lang == AppLanguage.IT) "Aggiornamento Disponibile" else "Update Available",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SagePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (lang == AppLanguage.IT) "Nuova versione: v$displayVersion" else "New version: v$displayVersion",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SagePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Text(
                text = if (lang == AppLanguage.IT)
                    "È disponibile una nuova versione di MealPlan su Google Play Store con importanti novità e ottimizzazioni. Vuoi aggiornare adesso?"
                else
                    "A new version of MealPlan is available on Google Play Store with key features and optimizations. Would you like to update now?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmUpdate,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_update_app")
            ) {
                Text(
                    text = if (lang == AppLanguage.IT) "Aggiorna" else "Update",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_dismiss_update_app")
            ) {
                Text(
                    text = if (lang == AppLanguage.IT) "Chiudi" else "Close",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Sovraccarico per retrocompatibilità qualora venga passato un oggetto ReleaseInfo.
 */
@Composable
fun AppUpdateDialog(
    releaseInfo: ReleaseInfo,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppUpdateDialog(
        availableVersionCode = releaseInfo.versionCode,
        availableVersionName = releaseInfo.versionName,
        lang = lang,
        onDismiss = onDismiss,
        onConfirmUpdate = onConfirmUpdate,
        modifier = modifier
    )
}
