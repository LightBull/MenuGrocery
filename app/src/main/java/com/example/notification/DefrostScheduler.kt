package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.MealPlanDatabase
import com.example.receiver.DefrostAlarmReceiver
import java.util.Calendar

object DefrostScheduler {

    private const val TAG = "DefrostScheduler"
    private const val PREFS_NAME = "defrost_scheduler_prefs"
    private const val KEY_SCHEDULED_IDS = "scheduled_food_item_ids"

    fun getTrackedItemIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet())?.toSet() ?: emptySet()
    }

    private fun saveTrackedItemIds(context: Context, ids: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SCHEDULED_IDS, ids).apply()
    }

    private fun addTrackedItemId(context: Context, foodItemId: String) {
        val current = getTrackedItemIds(context).toMutableSet()
        current.add(foodItemId)
        saveTrackedItemIds(context, current)
    }

    private fun removeTrackedItemId(context: Context, foodItemId: String) {
        val current = getTrackedItemIds(context).toMutableSet()
        if (current.remove(foodItemId)) {
            saveTrackedItemIds(context, current)
        }
    }

    /**
     * Cancella tutti i promemoria registrati nell'AlarmManager dell'OS
     * e svuota la lista delle chiavi tracciate.
     */
    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trackedIds = getTrackedItemIds(context)
        val intent = Intent(context, DefrostAlarmReceiver::class.java)

        for (foodItemId in trackedIds) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                foodItemId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        saveTrackedItemIds(context, emptySet())
        Log.d(TAG, "Cancellati tutti i promemoria di scongelamento (${trackedIds.size} trovati).")
    }

    /**
     * Cancella il promemoria specifico per un dato alimento.
     */
    fun cancelDefrostReminder(context: Context, foodItemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DefrostAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            foodItemId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        removeTrackedItemId(context, foodItemId)
    }

    /**
     * BR-03: Calcola la sera precedente al pasto (GiornoPasto - 1) alle ore 20:00
     * e imposta la notifica tramite AlarmManager.
     * @param dayOfWeek Giorno del pasto (1 = Lunedì, 7 = Domenica)
     */
    fun scheduleDefrostReminder(
        context: Context,
        foodItemId: String,
        foodName: String,
        mealDisplayName: String,
        dayOfWeek: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Converti il giorno della sera precedente (20:00 del giorno prima)
        // Se pasto è Lunedì (1) -> sera precedente è Domenica (Calendar.SUNDAY)
        // Se pasto è Venerdì (5) -> sera precedente è Giovedì (Calendar.THURSDAY)
        val reminderCalendarDay = when (dayOfWeek) {
            1 -> Calendar.SUNDAY
            2 -> Calendar.MONDAY
            3 -> Calendar.TUESDAY
            4 -> Calendar.WEDNESDAY
            5 -> Calendar.THURSDAY
            6 -> Calendar.FRIDAY
            7 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }

        // Calcola la prossima data valida alle 20:00:00
        val reminderCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Trova la prossima occorrenza futura del giorno di promemoria alle 20:00
            while (get(Calendar.DAY_OF_WEEK) != reminderCalendarDay || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, DefrostAlarmReceiver::class.java).apply {
            putExtra(DefrostAlarmReceiver.EXTRA_FOOD_ITEM_ID, foodItemId)
            putExtra(DefrostAlarmReceiver.EXTRA_FOOD_NAME, foodName)
            putExtra(DefrostAlarmReceiver.EXTRA_MEAL_NAME, mealDisplayName)
            putExtra(DefrostAlarmReceiver.EXTRA_NOTIFICATION_ID, foodItemId.hashCode())
            putExtra(DefrostAlarmReceiver.EXTRA_MEAL_DAY_OF_WEEK, dayOfWeek)
            putExtra(DefrostAlarmReceiver.EXTRA_IS_TEST, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            foodItemId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderCalendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderCalendar.timeInMillis,
                    pendingIntent
                )
            }
            addTrackedItemId(context, foodItemId)
            Log.d(TAG, "Allarme programmato per '$foodName' (Giorno $dayOfWeek) alle ore 20:00 del giorno precedente (${reminderCalendar.time})")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permesso allarme negato: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderCalendar.timeInMillis,
                pendingIntent
            )
            addTrackedItemId(context, foodItemId)
        }
    }

    /**
     * Risincronizzazione globale di tutti i promemoria di scongelamento:
     * 1. Elimina qualsiasi vecchio allarme registrato (anche orfani di vecchi piani)
     * 2. Recupera dal DB il solo piano attivo corrente
     * 3. Registra esclusivamente gli alimenti con toDefrost = true del piano attivo
     */
    suspend fun resyncAllActiveDefrostReminders(context: Context, database: MealPlanDatabase): Int = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        cancelAllReminders(context)

        val activePlan = database.mealPlanDao().getActivePlanSync() ?: return@withContext 0
        val meals = database.mealDao().getMealsForPlanSync(activePlan.id)
        val mealIds = meals.map { it.id }
        if (mealIds.isEmpty()) return@withContext 0

        val foodItems = database.mealFoodItemDao().getItemsForMealsSync(mealIds)
        val defrostItems = foodItems.filter { it.toDefrost }

        var scheduledCount = 0
        for (item in defrostItems) {
            val meal = meals.firstOrNull { it.id == item.mealId }
            if (meal != null) {
                scheduleDefrostReminder(
                    context = context,
                    foodItemId = item.id,
                    foodName = item.name,
                    mealDisplayName = meal.mealType.displayName,
                    dayOfWeek = meal.dayOfWeek
                )
                scheduledCount++
            }
        }

        Log.d(TAG, "Risincronizzazione completata: $scheduledCount allarmi attivi per il piano '${activePlan.title}'")
        scheduledCount
    }

    /**
     * Esegue un test immediato del promemoria di scongelamento per verifica visiva immediata
     */
    fun triggerImmediateTestReminder(context: Context, foodName: String, mealDisplayName: String) {
        val intent = Intent(context, DefrostAlarmReceiver::class.java).apply {
            putExtra(DefrostAlarmReceiver.EXTRA_IS_TEST, true)
            putExtra(DefrostAlarmReceiver.EXTRA_FOOD_NAME, foodName)
            putExtra(DefrostAlarmReceiver.EXTRA_MEAL_NAME, mealDisplayName)
            putExtra(DefrostAlarmReceiver.EXTRA_NOTIFICATION_ID, (System.currentTimeMillis() % 10000).toInt())
        }
        context.sendBroadcast(intent)
    }
}
