package com.example.ui.i18n

import com.example.data.model.AppLanguage
import com.example.data.model.MealType

object AppStrings {

    fun navMealPlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Piano Pasti"
        AppLanguage.EN -> "Meal Plan"
    }

    fun navShoppingList(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Lista Spesa"
        AppLanguage.EN -> "Shopping List"
    }

    fun navMyPlans(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "I Miei Piani"
        AppLanguage.EN -> "My Plans"
    }

    fun activePlanHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "PIANO ATTIVO"
        AppLanguage.EN -> "ACTIVE PLAN"
    }

    fun noActivePlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun piano attivo"
        AppLanguage.EN -> "No active plan"
    }

    fun managePlans(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Gestisci Piani"
        AppLanguage.EN -> "Manage Plans"
    }

    fun defrostDiagnostics(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Diagnostica Scongelamento"
        AppLanguage.EN -> "Defrost Diagnostics"
    }

    fun settingsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Impostazioni"
        AppLanguage.EN -> "Settings"
    }

    fun languageSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Lingua dell'applicazione"
        AppLanguage.EN -> "Application Language"
    }

    fun languageSectionSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Scegli la lingua per l'interfaccia e la priorità di ricerca alimenti"
        AppLanguage.EN -> "Choose language for interface labels and food search priority"
    }

    fun foodDbSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Database Alimenti (FoodDb.db)"
        AppLanguage.EN -> "Food Database (FoodDb.db)"
    }

    fun foodDbSectionSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Consultazione offline di oltre 14.000 alimenti con valori nutrizionali completi."
        AppLanguage.EN -> "Offline search across 14,000+ foods with complete nutrition values."
    }

    fun foodDbReadOnlyBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Sola Lettura"
        AppLanguage.EN -> "Read-Only"
    }

    fun foodDbTablesInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Tabelle: principal_food_tables (primaria) • food_back_archive (secondaria)"
        AppLanguage.EN -> "Tables: principal_food_tables (primary) • food_back_archive (secondary)"
    }

    fun appVersionLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Versione Applicazione"
        AppLanguage.EN -> "Application Version"
    }

    fun back(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Indietro"
        AppLanguage.EN -> "Back"
    }

    fun searchFoodPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cerca Alimento"
        AppLanguage.EN -> "Search Food"
    }

    fun tapToSelect(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Tocca per selezionare:"
        AppLanguage.EN -> "Tap to select:"
    }

    fun showLess(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Mostra meno"
        AppLanguage.EN -> "Show less"
    }

    fun showAll(lang: AppLanguage, count: Int): String = when (lang) {
        AppLanguage.IT -> "Visualizza tutti ($count)"
        AppLanguage.EN -> "View all ($count)"
    }

    fun sourceLocalLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Archivio locale (già usato)"
        AppLanguage.EN -> "Local archive (previously used)"
    }

    fun sourceFoodDbLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Database alimenti"
        AppLanguage.EN -> "Food database"
    }

    fun foodNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nome Alimento"
        AppLanguage.EN -> "Food Name"
    }

    fun brandCategoryLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Marca / Categoria"
        AppLanguage.EN -> "Brand / Category"
    }

    fun quantityLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Quantità"
        AppLanguage.EN -> "Quantity"
    }

    fun defrostReminderLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Notifica scongelamento (giorno prima)"
        AppLanguage.EN -> "Defrost reminder (day before)"
    }

    fun defrostReminderSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Avviso push alle ore 20:00 del giorno precedente"
        AppLanguage.EN -> "Push alert at 8:00 PM the previous day"
    }

    fun addFoodToMeal(lang: AppLanguage, mealName: String): String = when (lang) {
        AppLanguage.IT -> "Aggiungi a $mealName"
        AppLanguage.EN -> "Add to $mealName"
    }

    fun editFoodTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modifica Alimento"
        AppLanguage.EN -> "Edit Food"
    }

    fun addFoodTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Aggiungi Alimento"
        AppLanguage.EN -> "Add Food"
    }

    fun saveChanges(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Salva Modifiche"
        AppLanguage.EN -> "Save Changes"
    }

    fun deleteFood(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Elimina alimento"
        AppLanguage.EN -> "Delete food"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Annulla"
        AppLanguage.EN -> "Cancel"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Chiudi"
        AppLanguage.EN -> "Close"
    }

    fun kcal(lang: AppLanguage): String = "kcal"

    fun proteins(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Proteine"
        AppLanguage.EN -> "Proteins"
    }

    fun carbs(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Carboidrati"
        AppLanguage.EN -> "Carbs"
    }

    fun fat(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Grassi"
        AppLanguage.EN -> "Fat"
    }

    fun mealTypeName(type: MealType, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> when (type) {
            MealType.BREAKFAST -> "Colazione"
            MealType.SNACK_AM -> "Spuntino"
            MealType.LUNCH -> "Pranzo"
            MealType.SNACK_PM -> "Merenda"
            MealType.DINNER -> "Cena"
        }
        AppLanguage.EN -> when (type) {
            MealType.BREAKFAST -> "Breakfast"
            MealType.SNACK_AM -> "Morning Snack"
            MealType.LUNCH -> "Lunch"
            MealType.SNACK_PM -> "Afternoon Snack"
            MealType.DINNER -> "Dinner"
        }
    }

    fun dayName(day: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> when (day) {
            1 -> "Lunedì"
            2 -> "Martedì"
            3 -> "Mercoledì"
            4 -> "Giovedì"
            5 -> "Venerdì"
            6 -> "Sabato"
            7 -> "Domenica"
            else -> "Giorno $day"
        }
        AppLanguage.EN -> when (day) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day $day"
        }
    }

    fun dayShortName(day: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> when (day) {
            1 -> "Lun"
            2 -> "Mar"
            3 -> "Mer"
            4 -> "Gio"
            5 -> "Ven"
            6 -> "Sab"
            7 -> "Dom"
            else -> "G$day"
        }
        AppLanguage.EN -> when (day) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> "D$day"
        }
    }
}
