package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUser_cannotWriteMealPlan() = runTest {
    auth.signOut()
    try {
      firestore.collection("users").document("alice").collection("meal_plans").document("p1").set(
        mapOf(
          "id" to "p1",
          "userId" to "alice",
          "title" to "Piano Dieta",
          "notes" to "Note dieta",
          "isActive" to true,
          "mealsJson" to "[]"
        )
      ).await()
      fail("Expected PERMISSION_DENIED for unauthenticated user")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  @Test
  fun authenticatedUser_canWriteAndReadOwnMealPlan() = runTest {
    val uid = signInTestUser("alice_rule@test.com")
    val docRef = firestore.collection("users").document(uid).collection("meal_plans").document("p1")

    docRef.set(
      mapOf(
        "id" to "p1",
        "userId" to uid,
        "title" to "Piano Settimanale",
        "notes" to "Note settimanale",
        "isActive" to true,
        "mealsJson" to "[]"
      )
    ).await()

    val snapshot = docRef.get().await()
    assertTrue(snapshot.exists())
    assertEquals("Piano Settimanale", snapshot.getString("title"))
  }

  @Test
  fun authenticatedUser_canWriteAndReadOwnShoppingItem() = runTest {
    val uid = signInTestUser("bob_rule@test.com")
    val itemRef = firestore.collection("users").document(uid).collection("shopping_items").document("item_1")

    itemRef.set(
      mapOf(
        "id" to "item_1",
        "userId" to uid,
        "foodName" to "Riso Basmati",
        "category" to "Dispensa",
        "quantity" to 500.0,
        "unit" to "GRAMS",
        "isPurchased" to false,
        "isManualExtra" to false
      )
    ).await()

    val snapshot = itemRef.get().await()
    assertTrue(snapshot.exists())
    assertEquals("Riso Basmati", snapshot.getString("foodName"))
  }

  @Test
  fun crossUser_cannotReadOtherUsersData() = runTest {
    val aliceUid = signInTestUser("alice_cross@test.com")
    firestore.collection("users").document(aliceUid).collection("meal_plans").document("plan_alice").set(
      mapOf(
        "id" to "plan_alice",
        "userId" to aliceUid,
        "title" to "Segreto Alice",
        "notes" to "Note segrete",
        "isActive" to true,
        "mealsJson" to "[]"
      )
    ).await()

    // Sign in as Bob and attempt to read Alice's plan
    signInTestUser("bob_cross@test.com")
    try {
      firestore.collection("users").document(aliceUid).collection("meal_plans").document("plan_alice").get().await()
      fail("Expected PERMISSION_DENIED when Bob tries to read Alice's meal plan")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }
}
