package com.example.data.subscription

import com.example.data.model.SubscriptionTier
import com.example.data.model.UserSubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager {
    // Default: simulazione attiva con PREMIUM come richiesto
    private val _subscriptionState = MutableStateFlow(
        UserSubscriptionState(
            tier = SubscriptionTier.PREMIUM,
            isSimulated = true
        )
    )
    val subscriptionState: StateFlow<UserSubscriptionState> = _subscriptionState.asStateFlow()

    fun setTier(tier: SubscriptionTier) {
        _subscriptionState.value = _subscriptionState.value.copy(tier = tier)
    }

    fun isCloudSyncActive(): Boolean {
        return _subscriptionState.value.tier == SubscriptionTier.PREMIUM
    }

    companion object {
        @Volatile
        private var instance: SubscriptionManager? = null

        fun getInstance(): SubscriptionManager {
            return instance ?: synchronized(this) {
                instance ?: SubscriptionManager().also { instance = it }
            }
        }
    }
}
