package com.example.ui.i18n

import com.example.data.model.AppLanguage
import com.example.data.model.MealType

object AppStrings {

    // --- Navigazione & Sezioni Principali ---
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

    // --- Intestazione & Titoli Schermata Pasti ---
    fun activePlanHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "PIANO ATTIVO"
        AppLanguage.EN -> "ACTIVE PLAN"
    }

    fun activePlanBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Piano Attivo"
        AppLanguage.EN -> "Active Plan"
    }

    fun testDefrostNotification(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Test Notifica Scongelamento"
        AppLanguage.EN -> "Test Defrost Notification"
    }

    fun noActivePlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun piano attivo"
        AppLanguage.EN -> "No active plan"
    }

    fun managePlans(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Gestisci Piani"
        AppLanguage.EN -> "Manage Plans"
    }

    fun changePlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cambia"
        AppLanguage.EN -> "Change"
    }

    fun defrostDiagnostics(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Diagnostica Scongelamento"
        AppLanguage.EN -> "Defrost Diagnostics"
    }

    fun recommendedTime(time: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Orario consigliato: $time"
        AppLanguage.EN -> "Recommended time: $time"
    }

    fun noFoodPlannedInMeal(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun alimento programmato per questo pasto."
        AppLanguage.EN -> "No food items planned for this meal."
    }

    fun dailyMacroSummaryTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Riepilogo Nutrizionale Giornaliero"
        AppLanguage.EN -> "Daily Nutritional Summary"
    }

    // --- Impostazioni ---
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

    // --- Ricerca Alimenti & BottomSheet ---
    fun searchFoodPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cerca Alimento"
        AppLanguage.EN -> "Search Food"
    }

    fun searchFoodHint(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cerca un alimento o inserisci i dati manualmente"
        AppLanguage.EN -> "Search for a food item or enter data manually"
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
        AppLanguage.IT -> "Database alimenti (FoodDb)"
        AppLanguage.EN -> "Food database (FoodDb)"
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
        AppLanguage.IT -> "Qta"
        AppLanguage.EN -> "Qty"
    }

    fun unitLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Unità di misura"
        AppLanguage.EN -> "Unit of measurement"
    }

    fun defrostReminderLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Scongelamento"
        AppLanguage.EN -> "Defrost"
    }

    fun defrostReminderSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Avviso push del giorno precedente"
        AppLanguage.EN -> "Push alert the previous day"
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
        AppLanguage.IT -> "Elimina Alimento"
        AppLanguage.EN -> "Delete Food"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Annulla"
        AppLanguage.EN -> "Cancel"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Chiudi"
        AppLanguage.EN -> "Close"
    }

    fun save(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Salva"
        AppLanguage.EN -> "Save"
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

    fun proteinAbbrev(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Prot"
        AppLanguage.EN -> "Prot"
    }

    fun carbsAbbrev(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Carb"
        AppLanguage.EN -> "Carbs"
    }

    fun fatAbbrev(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Gras"
        AppLanguage.EN -> "Fat"
    }

    // --- Lista Spesa (ShoppingListScreen) ---
    fun shoppingListTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Lista della Spesa"
        AppLanguage.EN -> "Shopping List"
    }

    fun shoppingListSearchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cerca nella spesa (alimento, reparto, giorno)..."
        AppLanguage.EN -> "Search grocery (food, aisle, day)..."
    }

    fun daysFilterAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Tutti (Lun - Dom)"
        AppLanguage.EN -> "All (Mon - Sun)"
    }

    fun daysFilterStartWeek(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Inizio (Lun - Gio)"
        AppLanguage.EN -> "Weekdays (Mon - Thu)"
    }

    fun daysFilterWeekend(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Weekend (Ven - Dom)"
        AppLanguage.EN -> "Weekend (Fri - Sun)"
    }

    fun btnGenerateAggregate(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Genera Lista Aggregata"
        AppLanguage.EN -> "Generate Combined List"
    }

    fun btnResetList(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Ripristina"
        AppLanguage.EN -> "Reset All"
    }

    fun btnAddExtra(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Extra"
        AppLanguage.EN -> "Add Extra"
    }

    fun btnShareList(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Condividi"
        AppLanguage.EN -> "Share"
    }

    fun btnClearChecked(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Pulisci Acquistati"
        AppLanguage.EN -> "Clear Checked"
    }

    fun emptyShoppingListTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun prodotto in lista"
        AppLanguage.EN -> "No items in shopping list"
    }

    fun emptyShoppingListSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Tocca 'Genera Lista Aggregata' per prelevare gli ingredienti dal piano attivo."
        AppLanguage.EN -> "Tap 'Generate Combined List' to fetch ingredients from the active meal plan."
    }

    fun dialogAddExtraTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Aggiungi Articolo Extra"
        AppLanguage.EN -> "Add Extra Item"
    }

    fun dialogAdjustPantryTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Regola Quantità & Dispensa"
        AppLanguage.EN -> "Adjust Quantity & Pantry"
    }

    fun foodNeedFromMeals(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Fabbisogno dai pasti:"
        AppLanguage.EN -> "Required from meals:"
    }

    fun inPantryLabel(unit: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Già in frigo / dispensa ($unit)"
        AppLanguage.EN -> "Already in fridge / pantry ($unit)"
    }

    fun realToBuyLabel(unit: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Quantità reale da comprare ($unit) *"
        AppLanguage.EN -> "Actual quantity to buy ($unit) *"
    }

    fun restoreOriginalQty(origStr: String, unit: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Ripristina originale ($origStr $unit)"
        AppLanguage.EN -> "Restore original ($origStr $unit)"
    }

    fun purchasedCountLabel(purchased: Int, total: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "$purchased di $total articoli acquistati"
        AppLanguage.EN -> "$purchased of $total items purchased"
    }

    fun generationRangeTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Intervallo di Generazione"
        AppLanguage.EN -> "Generation Range"
    }

    fun itemsToBuy(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "$count da prendere"
        AppLanguage.EN -> "$count to buy"
    }

    fun purchasedSectionTitle(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Articoli già presi ($count)"
        AppLanguage.EN -> "Purchased items ($count)"
    }

    fun showLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Mostra"
        AppLanguage.EN -> "Show"
    }

    fun hideLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nascondi"
        AppLanguage.EN -> "Hide"
    }

    fun noFoodFoundTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun articolo trovato"
        AppLanguage.EN -> "No items found"
    }

    fun noFoodFoundSubtitle(query: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nessun risultato per \"$query\""
        AppLanguage.EN -> "No results for \"$query\""
    }

    fun showAllItems(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Mostra tutti gli articoli"
        AppLanguage.EN -> "Show all items"
    }

    fun dialogShareListTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Condividi Lista della Spesa"
        AppLanguage.EN -> "Share Shopping List"
    }

    fun shareListExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Invia la lista organizzata per corsie di supermercato, oppure esporta il formato JSON strutturato per consentire l'unione con altre liste."
        AppLanguage.EN -> "Send the list organized by grocery aisles, or export structured JSON format to allow merging with other lists."
    }

    fun tabOrganizedText(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Testo Organizzato"
        AppLanguage.EN -> "Organized Text"
    }

    fun tabJsonFormat(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Formato JSON"
        AppLanguage.EN -> "JSON Format"
    }

    fun copy(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Copia"
        AppLanguage.EN -> "Copy"
    }

    fun dialogImportTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Importa / Unisci Lista"
        AppLanguage.EN -> "Import / Merge List"
    }

    fun importExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Incolla il payload JSON della lista della spesa condivisa."
        AppLanguage.EN -> "Paste the JSON payload of the shared shopping list."
    }

    fun importModeTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modalità di importazione:"
        AppLanguage.EN -> "Import mode:"
    }

    fun importModeMerge(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Unisci alla lista esistente (somma le quantità)"
        AppLanguage.EN -> "Merge into existing list (add quantities)"
    }

    fun importModeReplace(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Sostituisci interamente la lista attuale"
        AppLanguage.EN -> "Completely replace current list"
    }

    fun importAction(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Importa"
        AppLanguage.EN -> "Import"
    }

    fun adjustPantryExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Hai degli avanzi in frigo o in dispensa? Inserisci la quantità disponibile per calcolare quanto comprare effettivamente:"
        AppLanguage.EN -> "Do you have leftovers in fridge or pantry? Enter available quantity to calculate how much to buy:"
    }

    fun saveQuantity(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Salva Quantità"
        AppLanguage.EN -> "Save Quantity"
    }

    fun plannedVsPantryLabel(origStr: String, pantryStr: String, unit: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Pianificati: $origStr $unit • In dispensa: $pantryStr $unit"
        AppLanguage.EN -> "Planned: $origStr $unit • In pantry: $pantryStr $unit"
    }

    fun extraFoodDialogExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Aggiungi articoli extra non presenti nei pasti (es. sapone per piatti, sacchetti frigo, sale...)"
        AppLanguage.EN -> "Add extra items not in meals (e.g. dish soap, freezer bags, salt...)"
    }

    fun extraFoodNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nome articolo (es. Detersivo piatti)"
        AppLanguage.EN -> "Item name (e.g. Dish soap)"
    }

    fun addExtraConfirm(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Aggiungi"
        AppLanguage.EN -> "Add"
    }

    // --- Gestione Piani (MealPlansManagementScreen) ---
    fun myPlansTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "I Miei Piani Alimentari"
        AppLanguage.EN -> "My Meal Plans"
    }

    fun myPlansSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Gestisci i tuoi piani settimanali e crea nuove schede nutrizionali"
        AppLanguage.EN -> "Manage your weekly plans and create nutrition schedules"
    }

    fun btnNewPlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nuovo Piano"
        AppLanguage.EN -> "New Plan"
    }

    fun createdOn(dateStr: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Creato il: $dateStr"
        AppLanguage.EN -> "Created on: $dateStr"
    }

    fun btnSetActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Imposta come Attivo"
        AppLanguage.EN -> "Set as Active"
    }

    fun btnEdit(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modifica"
        AppLanguage.EN -> "Edit"
    }

    fun btnDelete(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Elimina"
        AppLanguage.EN -> "Delete"
    }

    fun dialogNewPlanTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Crea Nuovo Piano"
        AppLanguage.EN -> "Create New Plan"
    }

    fun planNameInputLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nome del Piano * (es. Dieta Definizione)"
        AppLanguage.EN -> "Plan Name * (e.g. Definition Diet)"
    }

    fun planNotesInputLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Note o Obiettivi (opzionale)"
        AppLanguage.EN -> "Notes or Goals (optional)"
    }

    fun setActiveCheckboxLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Imposta come piano attivo"
        AppLanguage.EN -> "Set as active plan"
    }

    fun btnCreatePlan(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Crea Piano"
        AppLanguage.EN -> "Create Plan"
    }

    fun dialogDeletePlanTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Eliminare il piano?"
        AppLanguage.EN -> "Delete this plan?"
    }

    fun dialogDeletePlanMessage(title: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Sei sicuro di voler eliminare '$title'? Questa operazione non può essere annullata."
        AppLanguage.EN -> "Are you sure you want to delete '$title'? This operation cannot be undone."
    }

    // --- Editor Piano (PlanEditorScreen) ---
    fun editPlanHeaderTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modifica Piano"
        AppLanguage.EN -> "Edit Plan"
    }

    fun unsavedChangesTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modifiche non salvate"
        AppLanguage.EN -> "Unsaved Changes"
    }

    fun unsavedChangesMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Ci sono modifiche non salvate al piano alimentare. Cosa desideri fare?"
        AppLanguage.EN -> "There are unsaved changes to the meal plan. What would you like to do?"
    }

    fun saveAndExit(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Sì, salva ed esci"
        AppLanguage.EN -> "Yes, save and exit"
    }

    fun exitWithoutSaving(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Esci senza salvare"
        AppLanguage.EN -> "Exit without saving"
    }

    fun addFoodToSectionBtn(mealName: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "+ Aggiungi alimento a $mealName"
        AppLanguage.EN -> "+ Add food to $mealName"
    }

    fun modalCopyFoodTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Copia Alimento in Altri Pasti"
        AppLanguage.EN -> "Copy Food to Other Meals"
    }

    fun selectTargetDaysLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Seleziona i giorni di destinazione:"
        AppLanguage.EN -> "Select target days:"
    }

    fun selectTargetMealsLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Seleziona i pasti di destinazione:"
        AppLanguage.EN -> "Select target meals:"
    }

    fun confirmCopyBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Conferma Copia"
        AppLanguage.EN -> "Confirm Copy"
    }

    // --- Traduzione Dinamica Piani e Alimenti Dimostrativi Predefiniti ---
    fun translatePlanTitle(title: String, lang: AppLanguage): String {
        if (lang == AppLanguage.IT) return title
        return when {
            title.contains("Piano Benessere Mediterraneo", ignoreCase = true) ->
                title.replace("Piano Benessere Mediterraneo", "Mediterranean Wellness Plan", ignoreCase = true)
            title.contains("Piano Definizione", ignoreCase = true) ->
                title.replace("Piano Definizione", "Definition Meal Plan", ignoreCase = true)
            else -> title
        }
    }

    fun translatePlanNotes(notes: String?, lang: AppLanguage): String? {
        if (notes == null || lang == AppLanguage.IT) return notes
        return when {
            notes.contains("Dieta bilanciata ricca di verdure, pesce, cereali integrali e frutta di stagione.", ignoreCase = true) ->
                "Balanced diet rich in vegetables, fish, whole grains and seasonal fruits."
            else -> notes
        }
    }

    fun translateFoodName(name: String, lang: AppLanguage): String {
        if (lang == AppLanguage.IT) return name
        return when (name.trim()) {
            "Yogurt Greco 0%" -> "Greek Yogurt 0%"
            "Fiocchi d'Avena Integrale" -> "Whole Rolled Oats"
            "Mela" -> "Apple"
            "Noci sgusciate" -> "Shelled Walnuts"
            "Riso Basmati Integrale", "Riso Integrale" -> "Brown Basmati Rice"
            "Petto di Pollo ai Ferri", "Pollo ai Ferri" -> "Grilled Chicken Breast"
            "Olio Extravergine d'Oliva" -> "Extra Virgin Olive Oil"
            "Pane di Segale" -> "Rye Bread"
            "Bresaola della Valtellina", "Bresaola" -> "Bresaola (Air-Dried Beef)"
            "Trancio di Salmone al Forno", "Trancio di Salmone" -> "Baked Salmon Fillet"
            "Broccoli al Vapore" -> "Steamed Broccoli"
            "Banana" -> "Banana"
            "Fesa di Tacchino" -> "Turkey Breast Slices"
            "Mandorle" -> "Almonds"
            "Tonno al naturale", "Tonno Naturale" -> "Canned Tuna in Brine"
            "Insalata Mista" -> "Mixed Salad"
            "Pomodori Pachino", "Pomodori" -> "Cherry Tomatoes"
            "Uova sode", "Uova Intere", "Uova" -> "Boiled Eggs"
            "Pasta Integrale" -> "Whole Wheat Pasta"
            "Merluzzo al Vapore", "Merluzzo" -> "Steamed Cod"
            "Zucchine Grigliate", "Zucchine" -> "Grilled Zucchini"
            "Ricotta Vaccina", "Ricotta" -> "Ricotta Cheese"
            else -> name
        }
    }

    fun mealTypeName(type: MealType, lang: AppLanguage): String = type.getLocalizedName(lang)

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

    // --- Autenticazione & Login ---
    fun authScreenTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Benvenuto in Meal Planner"
        AppLanguage.EN -> "Welcome to Meal Planner"
    }

    fun authScreenSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Pianifica i tuoi pasti, organizza la spesa e sincronizza tutto sul cloud."
        AppLanguage.EN -> "Plan your meals, organize groceries, and sync everything to the cloud."
    }

    fun authBenefit1(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Sincronizzazione Cloud Automatica"
        AppLanguage.EN -> "Automatic Cloud Synchronization"
    }

    fun authBenefit1Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "I tuoi piani settimanali e la lista della spesa sempre al sicuro e aggiornati."
        AppLanguage.EN -> "Your weekly plans and grocery lists are always secure and up-to-date."
    }

    fun authBenefit2(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accesso Multi-Dispositivo"
        AppLanguage.EN -> "Multi-Device Access"
    }

    fun authBenefit2Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accedi da qualsiasi telefono o tablet con il tuo account Google."
        AppLanguage.EN -> "Sign in from any phone or tablet with your Google account."
    }

    fun authBenefit3(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Archivio Alimenti & Notifiche Scongelamento"
        AppLanguage.EN -> "Food Database & Defrost Reminders"
    }

    fun authBenefit3Desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Oltre 14.000 alimenti con macronutrienti e notifiche push la sera prima."
        AppLanguage.EN -> "14,000+ foods with macronutrients and push reminders the evening before."
    }

    fun signInWithGoogleButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accedi con Google"
        AppLanguage.EN -> "Sign in with Google"
    }

    fun signingIn(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accesso in corso..."
        AppLanguage.EN -> "Signing in..."
    }

    fun signOut(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Disconnetti"
        AppLanguage.EN -> "Sign Out"
    }

    fun accountProfileTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Account Collegato"
        AppLanguage.EN -> "Connected Account"
    }

    // --- Abbonamento Cloud & Entitlements ---
    fun subscriptionSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Pacchetto Cloud & Abbonamento"
        AppLanguage.EN -> "Cloud Package & Subscription"
    }

    fun subscriptionSectionSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Gestione dei vantaggi cloud e sincronizzazione Firestore"
        AppLanguage.EN -> "Manage cloud benefits and Firestore synchronization"
    }

    fun subscriptionStatusActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Abbonamento Cloud Attivo"
        AppLanguage.EN -> "Cloud Subscription Active"
    }

    fun subscriptionStatusFree(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modalità Base (Solo Locale)"
        AppLanguage.EN -> "Basic Mode (Local Only)"
    }

    fun subscriptionSimulatedBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Simulazione attiva (Accesso Completo)"
        AppLanguage.EN -> "Simulation active (Full Access)"
    }

    fun subscriptionSimulatedDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Questa funzionalità in futuro richiederà un pacchetto a pagamento. Attualmente l'accesso è simulato come abilitato per consentire i test."
        AppLanguage.EN -> "This feature will require a paid package in the future. Access is currently simulated as enabled to allow testing."
    }

    fun subscriptionToggleTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Simula Abbonamento Cloud"
        AppLanguage.EN -> "Simulate Cloud Subscription"
    }

    fun subscriptionToggleSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Attiva/disattiva per testare il funzionamento cloud vs locale"
        AppLanguage.EN -> "Toggle to test cloud vs local behavior"
    }

    fun cloudSyncStatusSynced(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Cloud Sincronizzato"
        AppLanguage.EN -> "Cloud Synced"
    }

    fun cloudSyncStatusLocal(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Modalità Locale"
        AppLanguage.EN -> "Local Mode"
    }

    // --- Autenticazione Email/Password ---
    fun tabSignIn(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accedi"
        AppLanguage.EN -> "Sign In"
    }

    fun tabSignUp(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Registrati"
        AppLanguage.EN -> "Register"
    }

    fun emailLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Email"
        AppLanguage.EN -> "Email"
    }

    fun passwordLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Password"
        AppLanguage.EN -> "Password"
    }

    fun confirmPasswordLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Conferma Password"
        AppLanguage.EN -> "Confirm Password"
    }

    fun displayNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Nome (facoltativo)"
        AppLanguage.EN -> "Name (optional)"
    }

    fun btnSignInEmail(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Accedi con Email"
        AppLanguage.EN -> "Sign In with Email"
    }

    fun btnSignUpEmail(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Crea Account e Accedi"
        AppLanguage.EN -> "Create Account & Sign In"
    }

    fun orDivider(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "oppure"
        AppLanguage.EN -> "or"
    }

    fun googleSignInButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Continua con Google"
        AppLanguage.EN -> "Continue with Google"
    }

    fun errorInvalidEmail(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Inserisci un indirizzo email valido"
        AppLanguage.EN -> "Please enter a valid email address"
    }

    fun errorShortPassword(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "La password deve contenere almeno 6 caratteri"
        AppLanguage.EN -> "Password must contain at least 6 characters"
    }

    fun errorPasswordMismatch(lang: AppLanguage): String = when (lang) {
        AppLanguage.IT -> "Le password non coincidono"
        AppLanguage.EN -> "Passwords do not match"
    }
}

