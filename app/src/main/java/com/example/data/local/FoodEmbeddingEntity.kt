package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entità dedicata alla memorizzazione dei vettori di embedding per alimenti,
 * quantità e correlati semantici, generati o confrontati tramite ONNX Runtime.
 */
@Entity(
    tableName = "food_embeddings",
    indices = [
        Index(value = ["foodName"]),
        Index(value = ["category"])
    ]
)
data class FoodEmbeddingEntity(
    @PrimaryKey
    val id: String,
    val foodName: String,
    val category: String = "",
    val referenceQuantity: Double = 100.0,
    val unit: String = "g",
    val relatedFoods: String = "", // Cibi correlati separati da virgola
    val embeddingVector: String,    // Array float serializzato come stringa JSON o valori separati da virgola
    val vectorDimensions: Int = 0,
    val modelVersion: String = "onnx-v1",
    val updatedAt: Long = System.currentTimeMillis()
)
