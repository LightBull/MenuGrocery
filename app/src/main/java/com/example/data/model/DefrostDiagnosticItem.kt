package com.example.data.model

data class DefrostDiagnosticItem(
    val foodItemId: String,
    val foodName: String,
    val dayOfWeek: Int, // 1 = Lunedì, 7 = Domenica (giorno del pasto)
    val dayName: String, // "Lunedì", "Venerdì", ecc.
    val mealTypeDisplayName: String, // "Pranzo", "Cena", ecc.
    val reminderDayName: String, // "Domenica", "Giovedì" (sera prima)
    val reminderTimeFormatted: String = "20:00",
    val isScheduledInOs: Boolean
)
