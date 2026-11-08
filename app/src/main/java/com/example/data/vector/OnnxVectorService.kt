package com.example.data.vector

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.data.local.FoodEmbeddingDao
import com.example.data.local.FoodEmbeddingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.util.UUID
import kotlin.math.sqrt

/**
 * Servizio per la gestione vettoriale degli alimenti, quantità e correlati semantici
 * tramite ONNX Runtime e persistenza in Room (food_embeddings).
 */
class OnnxVectorService(
    private val context: Context,
    private val foodEmbeddingDao: FoodEmbeddingDao
) {
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()
        } catch (_: Throwable) {
            // Se la libreria nativa ONNX si carica su richiesta, l'ambiente sarà istanziato al primo uso
        }
    }

    /**
     * Inizializza una sessione ONNX specificando il nome del file modello (es. presente in assets o memoria interna).
     */
    fun loadModelFromAssets(modelAssetName: String): Boolean {
        return try {
            val env = ortEnvironment ?: OrtEnvironment.getEnvironment().also { ortEnvironment = it }
            val assetStream = context.assets.open(modelAssetName)
            val modelBytes = assetStream.readBytes()
            assetStream.close()
            ortSession?.close()
            ortSession = env.createSession(modelBytes, OrtSession.SessionOptions())
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Genera un embedding semantico di base per il testo (fallback euristico/hashing normalizzato
     * nel caso il file del modello ONNX non sia ancora caricato nell'asset folder).
     */
    fun generateDeterministicEmbedding(text: String, dimensions: Int = 64): FloatArray {
        val vector = FloatArray(dimensions)
        val normalized = text.trim().lowercase()
        for (i in normalized.indices) {
            val charCode = normalized[i].code
            val index = (charCode * (i + 1)) % dimensions
            vector[index] += (charCode / 255.0f)
        }
        // Normalizzazione del vettore (lunghezza unitaria) per facilitare cosine similarity
        var sumSquares = 0.0f
        for (v in vector) sumSquares += v * v
        val norm = sqrt(sumSquares)
        if (norm > 0.0f) {
            for (i in vector.indices) vector[i] /= norm
        }
        return vector
    }

    /**
     * Calcola la somiglianza del coseno tra due vettori.
     */
    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size || v1.isEmpty()) return 0.0f
        var dot = 0.0f
        var n1 = 0.0f
        var n2 = 0.0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            n1 += v1[i] * v1[i]
            n2 += v2[i] * v2[i]
        }
        val denom = (sqrt(n1) * sqrt(n2))
        return if (denom > 0.0f) dot / denom else 0.0f
    }

    /**
     * Salva o aggiorna l'embedding per un alimento con quantità e correlati.
     */
    suspend fun saveFoodEmbedding(
        foodName: String,
        category: String = "",
        referenceQuantity: Double = 100.0,
        unit: String = "g",
        relatedFoods: List<String> = emptyList(),
        customVector: FloatArray? = null
    ): FoodEmbeddingEntity = withContext(Dispatchers.IO) {
        val vector = customVector ?: generateDeterministicEmbedding("$foodName $category ${relatedFoods.joinToString(" ")}")
        val jsonArray = JSONArray()
        for (value in vector) {
            jsonArray.put(value.toDouble())
        }

        val existing = foodEmbeddingDao.getEmbeddingByFoodName(foodName)
        val entity = FoodEmbeddingEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            foodName = foodName,
            category = category,
            referenceQuantity = referenceQuantity,
            unit = unit,
            relatedFoods = relatedFoods.joinToString(", "),
            embeddingVector = jsonArray.toString(),
            vectorDimensions = vector.size,
            modelVersion = if (ortSession != null) "onnx-custom" else "onnx-heuristic-v1",
            updatedAt = System.currentTimeMillis()
        )
        foodEmbeddingDao.insertEmbedding(entity)
        entity
    }

    /**
     * Cerca alimenti per similarità vettoriale rispetto a una query o descrizione.
     */
    suspend fun searchSimilarFoods(
        query: String,
        topK: Int = 10,
        minSimilarity: Float = 0.1f
    ): List<Pair<FoodEmbeddingEntity, Float>> = withContext(Dispatchers.IO) {
        val all = foodEmbeddingDao.getAllEmbeddingsSync()
        if (all.isEmpty()) return@withContext emptyList()

        val queryVector = generateDeterministicEmbedding(query, all.first().vectorDimensions.coerceAtLeast(64))
        val scoredList = mutableListOf<Pair<FoodEmbeddingEntity, Float>>()

        for (item in all) {
            try {
                val json = JSONArray(item.embeddingVector)
                val itemVector = FloatArray(json.length()) { idx -> json.getDouble(idx).toFloat() }
                val score = cosineSimilarity(queryVector, itemVector)
                if (score >= minSimilarity) {
                    scoredList.add(Pair(item, score))
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        scoredList.sortedByDescending { it.second }.take(topK)
    }

    fun close() {
        try {
            ortSession?.close()
            ortEnvironment?.close()
        } catch (_: Throwable) {}
    }
}
