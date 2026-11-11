package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singolo step pianificato di variazione graduale.
 * Include la data calcolata e i pesi di partenza e destinazione.
 */
data class GradualityScheduledStep(
    val stepIndex: Int,
    val dateMillis: Long,
    val fromQuantity: Double,
    val toQuantity: Double
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("stepIndex", stepIndex)
        obj.put("dateMillis", dateMillis)
        obj.put("fromQuantity", fromQuantity)
        obj.put("toQuantity", toQuantity)
        return obj
    }

    fun getFormattedDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    companion object {
        fun fromJson(obj: JSONObject): GradualityScheduledStep {
            return GradualityScheduledStep(
                stepIndex = obj.optInt("stepIndex", 1),
                dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                fromQuantity = obj.optDouble("fromQuantity", 0.0),
                toQuantity = obj.optDouble("toQuantity", 0.0)
            )
        }
    }
}

/**
 * Rappresenta la configurazione di incremento graduale per un raggruppamento
 * omogeneo di alimento + pasti associati.
 * Salva la pianificazione temporale a step (date e pesi da minQuantity a maxQuantity),
 * senza vincolarsi al peso attuale del piano.
 */
data class GradualityGroupConfig(
    val groupKey: String, // es. "pane|GRAMS" o "pane|80.0|GRAMS"
    val foodName: String,
    val unit: QuantityUnit,
    val category: String,
    val targetMealTypes: List<MealType>,
    val targetDays: List<Int>,
    val isEnabled: Boolean = false,
    val minQuantity: Double = 0.0, // Peso iniziale (minimo)
    val maxQuantity: Double = minQuantity, // Peso massimo
    val incrementStep: Double = 0.0,
    val cadenceDays: Int = 14, // 7 = settimanale, 14 = bisettimanale, o N giorni
    val startDateMillis: Long = System.currentTimeMillis(),
    val scheduledSteps: List<GradualityScheduledStep> = emptyList(),
    val nextStepDateMillis: Long = startDateMillis + cadenceDays * 86_400_000L,
    // Campi per retrocompatibilità con record precedenti
    val baseQuantity: Double = minQuantity,
    val currentAppliedQuantity: Double = minQuantity
) {
    /**
     * Genera la sequenza di step calcolata dal minimo al massimo.
     */
    fun generateScheduledSteps(): List<GradualityScheduledStep> {
        if (!isEnabled || incrementStep == 0.0) return emptyList()
        val steps = mutableListOf<GradualityScheduledStep>()
        var current = minQuantity
        var index = 1
        val isIncreasing = incrementStep > 0
        while (if (isIncreasing) current < maxQuantity else current > maxQuantity) {
            val next = if (isIncreasing) {
                minOf(current + incrementStep, maxQuantity)
            } else {
                maxOf(current + incrementStep, maxQuantity)
            }
            val stepDate = startDateMillis + index * cadenceDays * 86_400_000L
            steps.add(
                GradualityScheduledStep(
                    stepIndex = index,
                    dateMillis = stepDate,
                    fromQuantity = current,
                    toQuantity = next
                )
            )
            current = next
            index++
        }
        return steps
    }

    /**
     * Calcola il peso teorico pianificato ad oggi (o alla data specificata).
     * Se la data è antecedente a startDateMillis, restituisce minQuantity.
     * Altrimenti avanza in base agli intervalli di cadenza trascorsi.
     */
    fun calculateTheoreticalWeight(atMillis: Long = System.currentTimeMillis()): Double {
        if (!isEnabled || incrementStep == 0.0) return minQuantity
        if (atMillis < startDateMillis) return minQuantity
        val elapsedDays = ((atMillis - startDateMillis) / (86_400_000L)).toInt()
        val completedIntervals = if (cadenceDays > 0) elapsedDays / cadenceDays else 0
        if (completedIntervals <= 0) return minQuantity

        val theoretical = minQuantity + completedIntervals * incrementStep
        return if (incrementStep > 0) {
            minOf(theoretical, maxQuantity)
        } else {
            maxOf(theoretical, maxQuantity)
        }
    }

    /**
     * Trova il prossimo step pianificato in ordine temporale.
     */
    fun nextScheduledStep(atMillis: Long = System.currentTimeMillis()): GradualityScheduledStep? {
        val steps = if (scheduledSteps.isNotEmpty()) scheduledSteps else generateScheduledSteps()
        if (steps.isEmpty()) return null
        return steps.firstOrNull { it.dateMillis >= atMillis } ?: steps.lastOrNull()
    }

    /**
     * Calcola i giorni mancanti al prossimo step rispetto al momento attuale.
     */
    fun daysRemaining(fromMillis: Long = System.currentTimeMillis()): Int {
        val nextStep = nextScheduledStep(fromMillis)
        val targetDate = nextStep?.dateMillis ?: nextStepDateMillis
        val diff = targetDate - fromMillis
        return (diff / (1000L * 60 * 60 * 24)).toInt()
    }

    /**
     * Calcola la quantità che verrà applicata con il prossimo step teorico.
     */
    fun nextQuantity(): Double {
        val nextStep = nextScheduledStep()
        if (nextStep != null) {
            return nextStep.toQuantity
        }
        val next = currentAppliedQuantity + incrementStep
        return if (incrementStep >= 0) {
            minOf(next, maxQuantity)
        } else {
            maxOf(next, maxQuantity)
        }
    }

    /**
     * Indica se la quantità massima (o minima) stabilita è già stata raggiunta dal teorico o dall'applicato.
     */
    fun isMaxReached(currentWeight: Double = currentAppliedQuantity): Boolean {
        return if (incrementStep >= 0) {
            currentWeight >= maxQuantity
        } else {
            currentWeight <= maxQuantity
        }
    }

    fun getFormattedTargetMeals(lang: AppLanguage): String {
        return targetMealTypes.joinToString(", ") { it.getLocalizedName(lang) }
    }

    fun getFormattedTargetDays(lang: AppLanguage): String {
        if (targetDays.size == 7) {
            return if (lang == AppLanguage.IT) "Tutti i giorni" else "Every day"
        }
        val daysMap = mapOf(
            1 to if (lang == AppLanguage.IT) "Lun" else "Mon",
            2 to if (lang == AppLanguage.IT) "Mar" else "Tue",
            3 to if (lang == AppLanguage.IT) "Mer" else "Wed",
            4 to if (lang == AppLanguage.IT) "Gio" else "Thu",
            5 to if (lang == AppLanguage.IT) "Ven" else "Fri",
            6 to if (lang == AppLanguage.IT) "Sab" else "Sat",
            7 to if (lang == AppLanguage.IT) "Dom" else "Sun"
        )
        return targetDays.sorted().joinToString(", ") { daysMap[it] ?: "$it" }
    }

    fun getFormattedDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("groupKey", groupKey)
        json.put("foodName", foodName)
        json.put("unit", unit.name)
        json.put("category", category)

        val mealsArr = JSONArray()
        targetMealTypes.forEach { mealsArr.put(it.name) }
        json.put("targetMealTypes", mealsArr)

        val daysArr = JSONArray()
        targetDays.forEach { daysArr.put(it) }
        json.put("targetDays", daysArr)

        json.put("isEnabled", isEnabled)
        json.put("minQuantity", minQuantity)
        json.put("maxQuantity", maxQuantity)
        json.put("incrementStep", incrementStep)
        json.put("cadenceDays", cadenceDays)
        json.put("startDateMillis", startDateMillis)
        json.put("nextStepDateMillis", nextStepDateMillis)
        json.put("baseQuantity", baseQuantity)
        json.put("currentAppliedQuantity", currentAppliedQuantity)

        val stepsArr = JSONArray()
        val stepsToSave = if (scheduledSteps.isNotEmpty()) scheduledSteps else generateScheduledSteps()
        stepsToSave.forEach { stepsArr.put(it.toJson()) }
        json.put("scheduledSteps", stepsArr)

        return json
    }

    companion object {
        fun fromJson(json: JSONObject): GradualityGroupConfig {
            val groupKey = json.optString("groupKey")
            val foodName = json.optString("foodName")
            val unitStr = json.optString("unit", "GRAMS")
            val unit = try { QuantityUnit.valueOf(unitStr) } catch (e: Exception) { QuantityUnit.GRAMS }
            val category = json.optString("category", "")

            val mealsList = mutableListOf<MealType>()
            val mealsArr = json.optJSONArray("targetMealTypes")
            if (mealsArr != null) {
                for (i in 0 until mealsArr.length()) {
                    try {
                        mealsList.add(MealType.valueOf(mealsArr.getString(i)))
                    } catch (e: Exception) {}
                }
            }

            val daysList = mutableListOf<Int>()
            val daysArr = json.optJSONArray("targetDays")
            if (daysArr != null) {
                for (i in 0 until daysArr.length()) {
                    daysList.add(daysArr.getInt(i))
                }
            }

            val isEnabled = json.optBoolean("isEnabled", false)
            val baseQuantity = json.optDouble("baseQuantity", 0.0)
            val minQuantity = json.optDouble("minQuantity", baseQuantity)
            val maxQuantity = json.optDouble("maxQuantity", minQuantity)
            val incrementStep = json.optDouble("incrementStep", 0.0)
            val cadenceDays = json.optInt("cadenceDays", 14)
            val startDateMillis = json.optLong("startDateMillis", System.currentTimeMillis())
            val nextStepDateMillis = json.optLong("nextStepDateMillis", startDateMillis + cadenceDays * 86_400_000L)
            val currentAppliedQuantity = json.optDouble("currentAppliedQuantity", minQuantity)

            val stepsList = mutableListOf<GradualityScheduledStep>()
            val stepsArr = json.optJSONArray("scheduledSteps")
            if (stepsArr != null) {
                for (i in 0 until stepsArr.length()) {
                    stepsList.add(GradualityScheduledStep.fromJson(stepsArr.getJSONObject(i)))
                }
            }

            val item = GradualityGroupConfig(
                groupKey = groupKey,
                foodName = foodName,
                unit = unit,
                category = category,
                targetMealTypes = mealsList,
                targetDays = daysList,
                isEnabled = isEnabled,
                minQuantity = minQuantity,
                maxQuantity = maxQuantity,
                incrementStep = incrementStep,
                cadenceDays = cadenceDays,
                startDateMillis = startDateMillis,
                scheduledSteps = stepsList,
                nextStepDateMillis = nextStepDateMillis,
                baseQuantity = baseQuantity,
                currentAppliedQuantity = currentAppliedQuantity
            )

            // Se scheduledSteps era vuoto ma l'elemento è abilitato, calcola gli step al volo
            return if (item.isEnabled && item.scheduledSteps.isEmpty()) {
                item.copy(scheduledSteps = item.generateScheduledSteps())
            } else {
                item
            }
        }
    }
}

/**
 * Dati di confronto per la schermata di spesa o popup di notifica:
 * incrocia il peso reale effettivo presente nel piano alimentare
 * con il peso teorico calcolato ad oggi secondo la pianificazione.
 */
data class GradualityItemComparison(
    val groupConfig: GradualityGroupConfig,
    val realCurrentWeight: Double,
    val theoreticalWeight: Double,
    val nextStep: GradualityScheduledStep?,
    val isAligned: Boolean = realCurrentWeight == theoreticalWeight,
    val isCompleted: Boolean = if (groupConfig.incrementStep >= 0) realCurrentWeight >= groupConfig.maxQuantity else realCurrentWeight <= groupConfig.maxQuantity
) {
    fun daysRemaining(atMillis: Long = System.currentTimeMillis()): Int {
        val targetDate = nextStep?.dateMillis ?: groupConfig.nextStepDateMillis
        val diff = targetDate - atMillis
        return (diff / (86_400_000L)).toInt()
    }
}

/**
 * Contenitore per la configurazione di gradualità dell'intero piano alimentare.
 * Include una data di inizio e una cadenza unificate per tutti gli alimenti coinvolti.
 */
data class GradualityPlanConfig(
    val items: List<GradualityGroupConfig> = emptyList(),
    val globalStartDateMillis: Long = System.currentTimeMillis(),
    val globalCadenceDays: Int = 14
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("globalStartDateMillis", globalStartDateMillis)
        root.put("globalCadenceDays", globalCadenceDays)
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        root.put("items", arr)
        return root.toString()
    }

    /**
     * Applica data di inizio e cadenza globali a tutti gli alimenti configurati,
     * rigenerando gli step pianificati per ciascuno.
     */
    fun withGlobalSettings(newStartDateMillis: Long, newCadenceDays: Int): GradualityPlanConfig {
        val updatedItems = items.map { item ->
            val updated = item.copy(
                startDateMillis = newStartDateMillis,
                cadenceDays = newCadenceDays,
                nextStepDateMillis = newStartDateMillis + newCadenceDays * 86_400_000L
            )
            updated.copy(scheduledSteps = updated.generateScheduledSteps())
        }
        return copy(
            items = updatedItems,
            globalStartDateMillis = newStartDateMillis,
            globalCadenceDays = newCadenceDays
        )
    }

    /**
     * Indica se il piano ha almeno un alimento con gradualità abilitata.
     */
    fun hasActiveGraduality(): Boolean {
        return items.any { it.isEnabled }
    }

    /**
     * Restituisce i giorni mancanti al prossimo step più imminente tra tutti gli alimenti abilitati.
     * Ritorna null se non ci sono elementi abilitati con incrementi pendenti.
     */
    fun minDaysRemaining(fromMillis: Long = System.currentTimeMillis()): Int? {
        val enabledPending = items.filter { it.isEnabled && !it.isMaxReached() }
        if (enabledPending.isEmpty()) return null
        return enabledPending.minOf { it.daysRemaining(fromMillis) }
    }

    /**
     * Elenco degli alimenti con gradualità abilitata che non hanno ancora raggiunto il massimo.
     */
    fun getPendingIncrements(): List<GradualityGroupConfig> {
        return items.filter { it.isEnabled && !it.isMaxReached() }
    }

    companion object {
        fun fromJsonString(jsonStr: String?): GradualityPlanConfig {
            if (jsonStr.isNullOrBlank()) return GradualityPlanConfig()
            return try {
                if (jsonStr.trim().startsWith("{")) {
                    val root = JSONObject(jsonStr)
                    val startDate = root.optLong("globalStartDateMillis", System.currentTimeMillis())
                    val cadence = root.optInt("globalCadenceDays", 14)
                    val arr = root.optJSONArray("items") ?: JSONArray()
                    val list = mutableListOf<GradualityGroupConfig>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(GradualityGroupConfig.fromJson(obj))
                    }
                    GradualityPlanConfig(
                        items = list,
                        globalStartDateMillis = startDate,
                        globalCadenceDays = cadence
                    )
                } else {
                    // Retrocompatibilità con array JSON diretto
                    val arr = JSONArray(jsonStr)
                    val list = mutableListOf<GradualityGroupConfig>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(GradualityGroupConfig.fromJson(obj))
                    }
                    val firstItem = list.firstOrNull()
                    val startDate = firstItem?.startDateMillis ?: System.currentTimeMillis()
                    val cadence = firstItem?.cadenceDays ?: 14
                    GradualityPlanConfig(
                        items = list,
                        globalStartDateMillis = startDate,
                        globalCadenceDays = cadence
                    )
                }
            } catch (e: Exception) {
                GradualityPlanConfig()
            }
        }
    }
}
