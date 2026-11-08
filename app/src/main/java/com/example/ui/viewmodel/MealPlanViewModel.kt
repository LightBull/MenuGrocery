package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MealPlanDatabase
import com.example.data.local.MealPlanEntity
import com.example.data.local.ShoppingListItemEntity
import com.example.data.model.DayOfWeekInfo
import com.example.data.model.DefrostDiagnosticItem
import com.example.data.model.FoodItemModel
import com.example.data.model.MealType
import com.example.data.model.MealWithItems
import com.example.data.model.NutritionalSummary
import com.example.data.model.QuantityUnit
import com.example.data.repository.MealPlanRepository
import com.example.notification.DefrostScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

import android.app.Activity
import com.example.data.auth.AuthRepository
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserSubscriptionState
import com.example.data.subscription.SubscriptionManager
import com.google.firebase.auth.FirebaseUser

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MealPlanRepository
    val onnxVectorService: com.example.data.vector.OnnxVectorService
    val authRepository: AuthRepository = AuthRepository(application)
    val subscriptionManager: SubscriptionManager get() = repository.subscriptionManager
    val subscriptionState: StateFlow<UserSubscriptionState> get() = subscriptionManager.subscriptionState
    val currentUser: StateFlow<FirebaseUser?> get() = authRepository.currentUser

    fun setSubscriptionTier(tier: SubscriptionTier) {
        subscriptionManager.setTier(tier)
    }

    fun syncWithCloud(onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.autoMergeWithCloud()
            onComplete(result.first, result.second)
        }
    }

    fun signOut(activity: Activity, onComplete: () -> Unit = {}) {
        authRepository.signOut(viewModelScope, onComplete)
    }

    val activePlan: StateFlow<MealPlanEntity?>
    val allPlans: StateFlow<List<MealPlanEntity>>
    val shoppingList: StateFlow<List<ShoppingListItemEntity>>

    // Giorno della settimana selezionato (1 = Lun, 7 = Dom)
    private val _selectedDay = MutableStateFlow(calculateCurrentDayIndex())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Pasti del giorno selezionato
    private val _mealsForSelectedDay = MutableStateFlow<List<MealWithItems>>(emptyList())
    val mealsForSelectedDay: StateFlow<List<MealWithItems>> = _mealsForSelectedDay.asStateFlow()

    // Totale nutrizionale del giorno
    private val _dailySummary = MutableStateFlow(NutritionalSummary())
    val dailySummary: StateFlow<NutritionalSummary> = _dailySummary.asStateFlow()

    // Giorni selezionati per la lista della spesa
    private val _selectedShoppingDays = MutableStateFlow<Set<Int>>((1..7).toSet())
    val selectedShoppingDays: StateFlow<Set<Int>> = _selectedShoppingDays.asStateFlow()

    private val prefs = application.getSharedPreferences("meal_plan_prefs", android.content.Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        com.example.data.model.AppLanguage.fromCode(prefs.getString("app_language", "it") ?: "it")
    )
    val currentLanguage: StateFlow<com.example.data.model.AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: com.example.data.model.AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
        loadMealsForSelectedDay()
    }

    // Risultati ricerca alimenti
    private val _searchResults = MutableStateFlow<List<FoodItemModel>>(emptyList())
    val searchResults: StateFlow<List<FoodItemModel>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Messaggio toast o alert
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Stato dell'editor del piano: traccia se ci sono modifiche pendenti non salvate
    private val _isEditorDirty = MutableStateFlow(false)
    val isEditorDirty: StateFlow<Boolean> = _isEditorDirty.asStateFlow()

    // Stato di apertura dell'editor di creazione/modifica piano
    private val _isPlanEditorOpen = MutableStateFlow(false)
    val isPlanEditorOpen: StateFlow<Boolean> = _isPlanEditorOpen.asStateFlow()

    fun setPlanEditorOpen(open: Boolean) {
        _isPlanEditorOpen.value = open
    }

    // Indice del tab di navigazione richiesto mentre l'editor ha modifiche pendenti
    private val _pendingNavigationTab = MutableStateFlow<Int?>(null)
    val pendingNavigationTab: StateFlow<Int?> = _pendingNavigationTab.asStateFlow()

    fun setEditorDirty(dirty: Boolean) {
        _isEditorDirty.value = dirty
    }

    fun requestTabNavigation(targetTab: Int) {
        _pendingNavigationTab.value = targetTab
    }

    fun clearPendingNavigationTab() {
        _pendingNavigationTab.value = null
    }

    fun postFeedbackMessage(msg: String) {
        _userFeedbackMessage.value = msg
    }

    fun triggerManualBackup(): java.io.File? {
        val backupFile = com.example.data.local.DatabaseMigrationManager.createBackupJson(getApplication())
        if (backupFile != null) {
            postFeedbackMessage("Backup creato: ${backupFile.name}")
        }
        return backupFile
    }

    fun getAvailableBackups(): List<java.io.File> {
        return com.example.data.local.DatabaseMigrationManager.listBackups(getApplication())
    }

    // Diagnostica allarmi di scongelamento
    private val _defrostDiagnostics = MutableStateFlow<List<DefrostDiagnosticItem>>(emptyList())
    val defrostDiagnostics: StateFlow<List<DefrostDiagnosticItem>> = _defrostDiagnostics.asStateFlow()

    init {
        val db = MealPlanDatabase.getDatabase(application)
        repository = MealPlanRepository(db, application)
        onnxVectorService = com.example.data.vector.OnnxVectorService(application, db.foodEmbeddingDao())

        // Feedback in caso di aggiornamento schema con ripristino da backup
        com.example.data.local.DatabaseMigrationManager.lastMigrationSummary?.let { summary ->
            postFeedbackMessage("Database aggiornato da v${summary.sourceVersion} a v${summary.targetVersion}. Backup creato e ${summary.restoredRowsCount} record ripristinati.")
            com.example.data.local.DatabaseMigrationManager.clearLastMigrationSummary()
        }

        activePlan = repository.getActivePlan().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allPlans = repository.getAllPlans().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        shoppingList = repository.getShoppingList().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Inizializza piano predefinito se primo avvio e risincronizza allarmi attivi
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            repository.resyncDefrostReminders()
            loadDefrostDiagnostics()
            loadMealsForSelectedDay()
        }

        // Ascolta variazioni del piano attivo per ricaricare la vista del giorno (BR-01) e diagnostica
        viewModelScope.launch {
            activePlan.collect {
                loadMealsForSelectedDay()
                loadDefrostDiagnostics()
            }
        }
    }

    private fun calculateCurrentDayIndex(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun selectDay(dayIndex: Int) {
        _selectedDay.value = dayIndex
        loadMealsForSelectedDay()
    }

    fun loadMealsForSelectedDay() {
        val plan = activePlan.value ?: return
        viewModelScope.launch {
            val meals = repository.getMealsWithItemsForDay(plan.id, _selectedDay.value)
            _mealsForSelectedDay.value = meals
            recalculateDailySummary(meals)
        }
    }

    private fun recalculateDailySummary(meals: List<MealWithItems>) {
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        for (meal in meals) {
            for (item in meal.items) {
                // Calcola i valori in proporzione alla quantità se espressa in grammi (base 100g) o pezzi
                val factor = if (item.unit == QuantityUnit.GRAMS) item.quantity / 100.0 else item.quantity
                totalCalories += item.calories * factor
                totalProtein += item.protein * factor
                totalCarbs += item.carbs * factor
                totalFat += item.fat * factor
            }
        }

        _dailySummary.value = NutritionalSummary(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        )
    }

    fun addFoodItem(
        mealId: String,
        name: String,
        brand: String?,
        quantity: Double,
        unit: QuantityUnit,
        toDefrost: Boolean,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        imageUrl: String?,
        mealType: MealType
    ) {
        viewModelScope.launch {
            repository.addFoodItem(
                mealId = mealId,
                name = name,
                brand = brand,
                quantity = quantity,
                unit = unit,
                toDefrost = toDefrost,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                imageUrl = imageUrl,
                dayOfWeek = _selectedDay.value,
                mealType = mealType
            )
            loadMealsForSelectedDay()
            _userFeedbackMessage.value = if (toDefrost) {
                "Alimento aggiunto! Promemoria scongelamento impostato alle 20:00."
            } else {
                "Alimento aggiunto con successo!"
            }
        }
    }

    fun deleteFoodItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteFoodItem(itemId)
            loadMealsForSelectedDay()
        }
    }

    fun updateFoodItem(item: FoodItemModel, mealDisplayName: String) {
        viewModelScope.launch {
            repository.updateFoodItem(item, _selectedDay.value, mealDisplayName)
            loadMealsForSelectedDay()
        }
    }

    // --- Gestione Lista della Spesa ---
    fun toggleShoppingDay(dayIndex: Int) {
        val current = _selectedShoppingDays.value.toMutableSet()
        if (current.contains(dayIndex)) {
            if (current.size > 1) { // Non consentire selezione vuota
                current.remove(dayIndex)
            }
        } else {
            current.add(dayIndex)
        }
        _selectedShoppingDays.value = current
    }

    fun setQuickShoppingRange(days: List<Int>) {
        _selectedShoppingDays.value = days.toSet()
    }

    fun generateShoppingList() {
        viewModelScope.launch {
            repository.generateShoppingListFromActivePlan(
                selectedDays = _selectedShoppingDays.value.toList()
            )
            _userFeedbackMessage.value = "Lista della spesa aggiornata e aggregata per i giorni selezionati!"
        }
    }

    fun toggleShoppingItem(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            repository.toggleItemPurchased(item)
        }
    }

    fun addManualShoppingExtra(name: String, quantity: Double, unit: QuantityUnit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addManualExtra(name, quantity, unit)
            _userFeedbackMessage.value = "Articolo extra aggiunto!"
        }
    }

    fun updateShoppingItemQuantities(id: String, actualQuantity: Double, pantryQuantity: Double) {
        viewModelScope.launch {
            repository.updateShoppingItemQuantities(id, actualQuantity, pantryQuantity)
            _userFeedbackMessage.value = "Quantità da acquistare aggiornata!"
        }
    }

    fun deleteShoppingItem(id: String) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun clearShoppingList() {
        viewModelScope.launch {
            repository.clearShoppingList()
        }
    }

    fun clearPurchasedShoppingItems() {
        viewModelScope.launch {
            repository.clearPurchasedShoppingItems()
        }
    }

    fun resetShoppingList() {
        viewModelScope.launch {
            repository.resetShoppingListPurchasedStatus()
            _userFeedbackMessage.value = "Lista ripristinata: tutti gli articoli sono contrassegnati come da prendere!"
        }
    }

    // --- Ricerca Alimenti Reattiva (Debounce 250ms) ---
    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(250) // Micro-attesa fluida durante la digitazione
            val results = repository.searchFood(query, _currentLanguage.value)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun copyFoodItem(
        item: FoodItemModel,
        targetDays: List<Int>,
        targetMealTypes: List<MealType>
    ) {
        if (targetDays.isEmpty() || targetMealTypes.isEmpty()) return
        viewModelScope.launch {
            repository.copyFoodItemToMeals(item, targetDays, targetMealTypes)
            _userFeedbackMessage.value = "Alimento '${item.name}' copiato con successo nei pasti selezionati!"
            loadMealsForSelectedDay()
        }
    }

    // --- Gestione Piani ---
    fun createPlan(title: String, notes: String?, setAsActive: Boolean) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createPlan(title, notes, setAsActive)
            if (setAsActive) {
                repository.generateShoppingListFromActivePlan(
                    selectedDays = _selectedShoppingDays.value.toList()
                )
            }
            _userFeedbackMessage.value = "Nuovo piano creato con successo!"
        }
    }

    suspend fun loadPlanWithAllDaysMeals(planId: String): Map<Int, List<MealWithItems>> {
        return repository.getPlanWithAllDaysMeals(planId)
    }

    fun saveFullPlan(
        planId: String,
        title: String,
        notes: String?,
        setAsActive: Boolean,
        daysMeals: Map<Int, List<MealWithItems>>,
        onComplete: () -> Unit
    ) {
        if (title.isBlank()) return
        // Crea una copia difensiva immutabile di tutti i giorni e cibi per evitare qualsiasi race condition tra thread
        val snapshotMap = daysMeals.mapValues { (_, meals) ->
            meals.map { it.copy(items = it.items.toList()) }
        }.toMap()

        viewModelScope.launch {
            val wasOrWillBeActive = (activePlan.value?.id == planId) || setAsActive
            repository.saveFullPlan(planId, title.trim(), notes?.ifBlank { null }, setAsActive, snapshotMap)
            if (wasOrWillBeActive) {
                repository.generateShoppingListFromActivePlan(
                    selectedDays = _selectedShoppingDays.value.toList()
                )
            }
            _userFeedbackMessage.value = "Piano salvato con successo e lista della spesa aggiornata!"
            // Ricarica i pasti del giorno correntemente visualizzato in Lista Pasti (mantenendo il giorno invariato)
            loadMealsForSelectedDay()
            onComplete()
        }
    }

    fun duplicatePlan(planId: String) {
        viewModelScope.launch {
            val newId = repository.duplicatePlan(planId)
            if (newId.isNotBlank()) {
                _userFeedbackMessage.value = "Piano duplicato con successo!"
            }
        }
    }

    fun activatePlan(planId: String) {
        viewModelScope.launch {
            repository.activatePlan(planId)
            _userFeedbackMessage.value = "Piano attivato! La spesa e i pasti ora seguono questo piano."
        }
    }

    fun deletePlan(plan: MealPlanEntity) {
        viewModelScope.launch {
            repository.deletePlan(plan)
            loadMealsForSelectedDay()
            _userFeedbackMessage.value = if (currentLanguage.value == com.example.data.model.AppLanguage.IT)
                "Piano '${plan.title}' eliminato."
            else
                "Plan '${plan.title}' deleted."
        }
    }

    // --- Condivisione & Unione Liste ---
    fun exportShoppingListAsJson(): String {
        val items = shoppingList.value
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("name", item.name)
                put("quantity", item.quantity)
                put("originalQuantity", item.originalQuantity)
                put("pantryQuantity", item.pantryQuantity)
                put("unit", item.unit.name)
                put("isManualExtra", item.isManualExtra)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun exportShoppingListAsFormattedText(): String {
        val items = shoppingList.value
        if (items.isEmpty()) return "Lista della spesa vuota."

        val sb = StringBuilder("🛒 Lista della Spesa - MealPlan\n\n")
        val planItems = items.filter { !it.isManualExtra }
        val extraItems = items.filter { it.isManualExtra }

        if (planItems.isNotEmpty()) {
            sb.append("📋 Dal Piano Alimentare:\n")
            planItems.forEach {
                val check = if (it.isPurchased) "✓" else "□"
                val qtyStr = if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else String.format("%.1f", it.quantity)
                val pantryNote = if (it.pantryQuantity > 0.0) {
                    val origStr = if (it.originalQuantity % 1.0 == 0.0) it.originalQuantity.toInt().toString() else String.format("%.1f", it.originalQuantity)
                    val pStr = if (it.pantryQuantity % 1.0 == 0.0) it.pantryQuantity.toInt().toString() else String.format("%.1f", it.pantryQuantity)
                    " (Pianificati: $origStr, in dispensa: $pStr)"
                } else ""
                sb.append("$check ${it.name}: $qtyStr ${it.unit.shortName}$pantryNote\n")
            }
            sb.append("\n")
        }

        if (extraItems.isNotEmpty()) {
            sb.append("📦 Extra e Varie:\n")
            extraItems.forEach {
                val check = if (it.isPurchased) "✓" else "□"
                val qtyStr = if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else String.format("%.1f", it.quantity)
                sb.append("$check ${it.name}: $qtyStr ${it.unit.shortName}\n")
            }
        }

        return sb.toString()
    }

    fun importShoppingListFromJson(jsonString: String, replaceExisting: Boolean): Boolean {
        return try {
            val jsonArray = JSONArray(jsonString)
            val importedList = mutableListOf<ShoppingListItemEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val quantity = obj.getDouble("quantity")
                val originalQuantity = obj.optDouble("originalQuantity", quantity)
                val pantryQuantity = obj.optDouble("pantryQuantity", 0.0)
                val unitStr = obj.optString("unit", QuantityUnit.GRAMS.name)
                val unit = try { QuantityUnit.valueOf(unitStr) } catch (_: Exception) { QuantityUnit.GRAMS }
                val isManualExtra = obj.optBoolean("isManualExtra", false)

                importedList.add(
                    ShoppingListItemEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        planId = activePlan.value?.id,
                        name = name,
                        quantity = quantity,
                        originalQuantity = originalQuantity,
                        pantryQuantity = pantryQuantity,
                        unit = unit,
                        isPurchased = false,
                        isManualExtra = isManualExtra,
                        sourceDays = if (isManualExtra) "Extra" else "Importato"
                    )
                )
            }

            viewModelScope.launch {
                repository.mergeShoppingList(importedList, replaceExisting)
                _userFeedbackMessage.value = if (replaceExisting) {
                    "Lista della spesa sostituita con ${importedList.size} alimenti!"
                } else {
                    "Integrazione completata! Le quantità degli alimenti comuni sono state sommate."
                }
            }
            true
        } catch (_: Exception) {
            _userFeedbackMessage.value = "Errore durante l'importazione: formato non valido."
            false
        }
    }

    fun testDefrostNotification(foodName: String, mealType: MealType) {
        DefrostScheduler.triggerImmediateTestReminder(
            getApplication(),
            foodName = foodName,
            mealDisplayName = mealType.displayName
        )
        _userFeedbackMessage.value = "Notifica di test inviata! Controlla la tendina notifiche."
    }

    fun loadDefrostDiagnostics() {
        viewModelScope.launch {
            _defrostDiagnostics.value = repository.getDefrostDiagnosticItems()
        }
    }

    fun resyncDefrostReminders() {
        viewModelScope.launch {
            val count = repository.resyncDefrostReminders()
            _defrostDiagnostics.value = repository.getDefrostDiagnosticItems()
            _userFeedbackMessage.value = "Allarmi ripuliti! Risincronizzati $count promemoria per il piano attivo."
        }
    }

    fun resetDatabaseToDefault() {
        viewModelScope.launch {
            repository.resetDatabaseToDefault()
            loadMealsForSelectedDay()
            loadDefrostDiagnostics()
            _userFeedbackMessage.value = "Database ricreato e ripopolato secondo l'ultima versione!"
        }
    }

    private val _availableBackups = MutableStateFlow<List<java.io.File>>(emptyList())
    val availableBackups: StateFlow<List<java.io.File>> = _availableBackups.asStateFlow()

    fun loadAvailableBackups() {
        viewModelScope.launch {
            _availableBackups.value = repository.getAvailableBackups()
        }
    }

    fun createManualBackup() {
        viewModelScope.launch {
            val file = repository.createManualBackup()
            if (file != null) {
                loadAvailableBackups()
                _userFeedbackMessage.value = "Backup creato con successo: ${file.name}"
            } else {
                _userFeedbackMessage.value = "Errore durante la creazione del backup"
            }
        }
    }

    fun restoreBackup(backupFile: java.io.File) {
        viewModelScope.launch {
            val success = repository.restoreFromBackup(backupFile)
            if (success) {
                loadMealsForSelectedDay()
                loadDefrostDiagnostics()
                loadAvailableBackups()
                _userFeedbackMessage.value = "Dati ripristinati con successo da: ${backupFile.name} (con embedding ONNX)!"
            } else {
                _userFeedbackMessage.value = "Errore durante il ripristino del backup"
            }
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }
}
