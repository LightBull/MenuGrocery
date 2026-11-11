package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

object AppUpdateHelper {

    private const val TAG = "AppUpdateHelper"
    private const val PLAY_STORE_PACKAGE_NAME = "com.aistudio.mealplan.rkfpw"
    private const val PREFS_NAME = "meal_plan_prefs"
    private const val KEY_LAST_SEEN_WHATS_NEW = "last_seen_whats_new_version_code"

    /**
     * Verifica se le novità della versione attualmente installata sono già state visualizzate dall'utente.
     */
    fun hasSeenWhatsNewForCurrentVersion(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSeenCode = prefs.getInt(KEY_LAST_SEEN_WHATS_NEW, -1)
        val currentCode = getCurrentVersionCode(context)
        return lastSeenCode >= currentCode
    }

    /**
     * Segna le novità della versione attualmente installata come già visualizzate.
     */
    fun markWhatsNewSeenForCurrentVersion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCode = getCurrentVersionCode(context)
        prefs.edit().putInt(KEY_LAST_SEEN_WHATS_NEW, currentCode).apply()
    }

    /**
     * Controlla asincronamente se è disponibile un aggiornamento dell'app su Google Play Store.
     * Gestisce in sicurezza sia l'ambiente di produzione che l'emulatore/debug.
     */
    fun checkForUpdate(
        context: Context,
        onUpdateAvailable: (availableVersionCode: Int) -> Unit,
        onNoUpdateAvailable: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                val availability = appUpdateInfo.updateAvailability()
                Log.d(TAG, "Update availability: $availability, availableVersionCode: ${appUpdateInfo.availableVersionCode()}")

                if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                    onUpdateAvailable(appUpdateInfo.availableVersionCode())
                } else {
                    onNoUpdateAvailable()
                }
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Google Play In-App Update check failed: ${exception.message}")
                onError(exception)
                onNoUpdateAvailable()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppUpdateManager", e)
            onError(e)
            onNoUpdateAvailable()
        }
    }

    /**
     * Apre direttamente la pagina dell'applicazione su Google Play Store per consentire
     * all'utente di eseguire l'aggiornamento.
     */
    fun openPlayStore(context: Context) {
        val targetPackage = context.packageName.ifBlank { PLAY_STORE_PACKAGE_NAME }
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$targetPackage")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$targetPackage")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Play Store intent", e)
        }
    }

    /**
     * Recupera il nome della versione attualmente installata (es. "11.0").
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "11.0"
        } catch (e: Exception) {
            "11.0"
        }
    }

    /**
     * Recupera il codice di versione attualmente installato (es. 11).
     */
    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            11
        }
    }
}
