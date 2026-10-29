package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.MealPlanDatabase
import com.example.notification.DefrostScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DefrostAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DefrostAlarmReceiver"
        const val CHANNEL_ID = "defrost_reminders_channel"
        const val EXTRA_FOOD_ITEM_ID = "extra_food_item_id"
        const val EXTRA_FOOD_NAME = "extra_food_name"
        const val EXTRA_MEAL_NAME = "extra_meal_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_MEAL_DAY_OF_WEEK = "extra_meal_day_of_week"
        const val EXTRA_IS_TEST = "extra_is_test"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)
        val foodItemId = intent.getStringExtra(EXTRA_FOOD_ITEM_ID)
        val foodName = intent.getStringExtra(EXTRA_FOOD_NAME) ?: "Alimento"
        val mealName = intent.getStringExtra(EXTRA_MEAL_NAME) ?: "pasto di domani"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, System.currentTimeMillis().toInt())

        // Test manuale o istantaneo da UI
        if (isTest) {
            postNotification(context, foodName, mealName, notificationId)
            return
        }

        // Se non è un test e non c'è foodItemId, scarta silenziosamente
        if (foodItemId.isNullOrBlank()) {
            Log.w(TAG, "Ricevuto broadcast senza ID alimento né flag di test. Scartato.")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MealPlanDatabase.getDatabase(context)
                val activePlan = db.mealPlanDao().getActivePlanSync()
                val foodItem = db.mealFoodItemDao().getItemById(foodItemId)

                // Se non c'è un piano attivo o il cibo è stato cancellato o non è più da scongelare: scarta e cancella l'allarme
                if (activePlan == null || foodItem == null || !foodItem.toDefrost) {
                    Log.d(TAG, "Allarme annullato: alimento o piano non più valido (toDefrost=${foodItem?.toDefrost})")
                    DefrostScheduler.cancelDefrostReminder(context, foodItemId)
                    return@launch
                }

                // Verifica se il pasto dell'alimento appartiene al piano ATTIVO
                val meal = db.mealDao().getMealById(foodItem.mealId)
                if (meal == null || meal.planId != activePlan.id) {
                    Log.d(TAG, "Allarme annullato: pasto non appartiene al piano attivo")
                    DefrostScheduler.cancelDefrostReminder(context, foodItemId)
                    return@launch
                }

                // VERIFICA CRUCIALE: Controlla che domani corrisponda esattamente al giorno del pasto previsto
                // (1 = Lunedì, 7 = Domenica)
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                val tomorrowDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }

                if (meal.dayOfWeek != tomorrowDayOfWeek) {
                    Log.w(TAG, "Allarme non sincronizzato: pasto per giorno ${meal.dayOfWeek}, ma domani è giorno $tomorrowDayOfWeek. Scartato.")
                    DefrostScheduler.cancelDefrostReminder(context, foodItemId)
                    // Rischedula alla data futura corretta
                    DefrostScheduler.scheduleDefrostReminder(
                        context = context,
                        foodItemId = foodItem.id,
                        foodName = foodItem.name,
                        mealDisplayName = meal.mealType.displayName,
                        dayOfWeek = meal.dayOfWeek
                    )
                    return@launch
                }

                // Se tutte le verifiche di coerenza e appartenenza sono soddisfatte, emetti la notifica
                postNotification(context, foodName, mealName, notificationId)

                // Rischedula per la prossima settimana (ricorrenza settimanale del piano)
                DefrostScheduler.scheduleDefrostReminder(
                    context = context,
                    foodItemId = foodItem.id,
                    foodName = foodItem.name,
                    mealDisplayName = meal.mealType.displayName,
                    dayOfWeek = meal.dayOfWeek
                )
            } catch (e: Exception) {
                // Non emettere notifiche in fallback se c'è un errore
                Log.e(TAG, "Errore durante la gestione dell'allarme di scongelamento: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(
        context: Context,
        foodName: String,
        mealName: String,
        notificationId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crea il canale di notifica per Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Promemoria Scongelamento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifiche serali (ore 20:00) per alimenti da scongelare per il giorno successivo"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("❄️ Promemoria Scongelamento")
            .setContentText("Ricordati di scongelare \"$foodName\" per $mealName!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Ricordati di tirare fuori dal congelatore \"$foodName\" stasera alle 20:00 per averlo pronto per $mealName di domani."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notifica di scongelamento inviata con successo per '$foodName' (id=$notificationId)")
    }
}
