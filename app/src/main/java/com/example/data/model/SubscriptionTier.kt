package com.example.data.model

enum class SubscriptionTier {
    FREE,       // Modalità solo locale (Room DB)
    PREMIUM     // Modalità Cloud (Room DB + Firestore Sync)
}

data class UserSubscriptionState(
    val tier: SubscriptionTier = SubscriptionTier.PREMIUM,
    val isSimulated: Boolean = true,
    val expiresAt: Long? = null
)
