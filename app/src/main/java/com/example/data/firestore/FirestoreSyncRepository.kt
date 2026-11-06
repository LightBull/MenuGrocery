package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.MealPlanEntity
import com.example.data.local.ShoppingListItemEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private fun getCurrentUserId(): String? = try {
        Firebase.auth.currentUser?.uid
    } catch (e: Exception) {
        null
    }

    suspend fun saveUserProfile(displayName: String?, email: String?, photoUrl: String?, tier: String = "PREMIUM") = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val userRef = db.collection("users").document(uid)
            val data = mapOf(
                "userId" to uid,
                "email" to (email ?: ""),
                "displayName" to (displayName ?: ""),
                "photoUrl" to (photoUrl ?: ""),
                "subscriptionTier" to tier,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(data, SetOptions.merge()).await()
            Log.d("FirestoreSync", "Profilo utente salvato su Firestore per uid=$uid")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore nel salvataggio profilo utente", e)
        }
    }

    suspend fun uploadMealPlan(
        plan: MealPlanEntity,
        mealsJson: String
    ) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val planRef = db.collection("users").document(uid).collection("meal_plans").document(plan.id)
            val data = mapOf(
                "id" to plan.id,
                "userId" to uid,
                "title" to plan.title,
                "notes" to (plan.notes ?: ""),
                "isActive" to plan.isActive,
                "mealsJson" to mealsJson,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            planRef.set(data, SetOptions.merge()).await()
            Log.d("FirestoreSync", "Piano ${plan.id} sincronizzato su Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore upload piano su Firestore", e)
        }
    }

    suspend fun deleteMealPlan(planId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            db.collection("users").document(uid).collection("meal_plans").document(planId).delete().await()
            Log.d("FirestoreSync", "Piano $planId eliminato da Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore eliminazione piano da Firestore", e)
        }
    }

    suspend fun uploadShoppingItem(item: ShoppingListItemEntity) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val itemRef = db.collection("users").document(uid).collection("shopping_items").document(item.id)
            val data = mapOf(
                "id" to item.id,
                "userId" to uid,
                "foodName" to item.name,
                "category" to item.category,
                "quantity" to item.quantity,
                "unit" to item.unit.name,
                "isPurchased" to item.isPurchased,
                "isManualExtra" to item.isManualExtra,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            itemRef.set(data, SetOptions.merge()).await()
            Log.d("FirestoreSync", "Shopping item ${item.id} sincronizzato su Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore upload shopping item su Firestore", e)
        }
    }

    suspend fun updateShoppingItemPurchased(itemId: String, isPurchased: Boolean) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            val itemRef = db.collection("users").document(uid).collection("shopping_items").document(itemId)
            itemRef.update(
                mapOf(
                    "isPurchased" to isPurchased,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore aggiornamento acquisto item su Firestore", e)
        }
    }

    suspend fun deleteShoppingItem(itemId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        try {
            db.collection("users").document(uid).collection("shopping_items").document(itemId).delete().await()
            Log.d("FirestoreSync", "Shopping item $itemId eliminato da Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore eliminazione shopping item da Firestore", e)
        }
    }

    suspend fun fetchRemoteMealPlans(): List<FirestoreMealPlan> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        try {
            val snapshot = db.collection("users").document(uid).collection("meal_plans").get().await()
            snapshot.toObjects(FirestoreMealPlan::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore fetch piani remoti", e)
            emptyList()
        }
    }

    suspend fun fetchRemoteShoppingItems(): List<FirestoreShoppingItem> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        try {
            val snapshot = db.collection("users").document(uid).collection("shopping_items").get().await()
            snapshot.toObjects(FirestoreShoppingItem::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Errore fetch shopping items remoti", e)
            emptyList()
        }
    }
}
