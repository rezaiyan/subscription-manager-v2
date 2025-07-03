package com.alirezaiyan.subscriptionmanager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionRepository(private val apiService: ApiService) {
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _totals = MutableStateFlow(SubscriptionTotals(0.0, 0.0))
    val totals: StateFlow<SubscriptionTotals> = _totals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        println("🏪 SubscriptionRepository initialized")
    }

    suspend fun loadSubscriptions() {
        println("🔄 Repository: Loading subscriptions...")
        try {
            _isLoading.value = true
            _error.value = null
            val subscriptions = apiService.getAllSubscriptions()
            println("✅ Repository: Loaded ${subscriptions.size} subscriptions")
            _subscriptions.value = subscriptions
            loadTotals()
        } catch (e: Exception) {
            println("❌ Repository: Failed to load subscriptions: ${e.message}")
            _error.value = "Failed to load subscriptions: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadActiveSubscriptions() {
        println("🔄 Repository: Loading active subscriptions...")
        try {
            _isLoading.value = true
            _error.value = null
            val subscriptions = apiService.getActiveSubscriptions()
            println("✅ Repository: Loaded ${subscriptions.size} active subscriptions")
            _subscriptions.value = subscriptions
        } catch (e: Exception) {
            println("❌ Repository: Failed to load active subscriptions: ${e.message}")
            _error.value = "Failed to load active subscriptions: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadTotals() {
        println("🔄 Repository: Loading totals...")
        try {
            val totals = apiService.getSubscriptionTotals()
            println("✅ Repository: Loaded totals - Monthly: $${totals.monthlyTotal}, Yearly: $${totals.yearlyTotal}")
            _totals.value = totals
        } catch (e: Exception) {
            println("❌ Repository: Failed to load totals: ${e.message}")
            _error.value = "Failed to load totals: ${e.message}"
        }
    }

    suspend fun searchSubscriptions(query: String) {
        if (query.isBlank()) {
            println("🔄 Repository: Empty search query, loading all subscriptions")
            loadSubscriptions()
            return
        }
        
        println("🔍 Repository: Searching subscriptions with query: '$query'")
        try {
            _isLoading.value = true
            _error.value = null
            val subscriptions = apiService.searchSubscriptions(query)
            println("✅ Repository: Found ${subscriptions.size} subscriptions matching '$query'")
            _subscriptions.value = subscriptions
        } catch (e: Exception) {
            println("❌ Repository: Failed to search subscriptions: ${e.message}")
            _error.value = "Failed to search subscriptions: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createSubscription(subscription: Subscription): Boolean {
        println("➕ Repository: Creating subscription: ${subscription.name}")
        return try {
            _isLoading.value = true
            _error.value = null
            val newSubscription = apiService.createSubscription(subscription)
            println("✅ Repository: Created subscription with ID: ${newSubscription.id}")
            _subscriptions.value = _subscriptions.value + newSubscription
            loadTotals()
            true
        } catch (e: Exception) {
            println("❌ Repository: Failed to create subscription: ${e.message}")
            _error.value = "Failed to create subscription: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateSubscription(subscription: Subscription): Boolean {
        println("✏️ Repository: Updating subscription ID: ${subscription.id}")
        return try {
            _isLoading.value = true
            _error.value = null
            val updatedSubscription = apiService.updateSubscription(subscription.id, subscription)
            if (updatedSubscription != null) {
                println("✅ Repository: Updated subscription ID: ${updatedSubscription.id}")
                _subscriptions.value = _subscriptions.value.map { 
                    if (it.id == subscription.id) updatedSubscription else it 
                }
                loadTotals()
                true
            } else {
                println("❌ Repository: Failed to update subscription - no response")
                _error.value = "Failed to update subscription"
                false
            }
        } catch (e: Exception) {
            println("❌ Repository: Failed to update subscription: ${e.message}")
            _error.value = "Failed to update subscription: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun toggleSubscriptionActive(id: Long): Boolean {
        println("🔄 Repository: Toggling subscription active state for ID: $id")
        return try {
            _isLoading.value = true
            _error.value = null
            val updatedSubscription = apiService.toggleSubscriptionActive(id)
            if (updatedSubscription != null) {
                println("✅ Repository: Toggled subscription ID: $id, new active state: ${updatedSubscription.active}")
                _subscriptions.value = _subscriptions.value.map { 
                    if (it.id == id) updatedSubscription else it 
                }
                loadTotals()
                true
            } else {
                println("❌ Repository: Failed to toggle subscription - no response")
                _error.value = "Failed to toggle subscription"
                false
            }
        } catch (e: Exception) {
            println("❌ Repository: Failed to toggle subscription: ${e.message}")
            _error.value = "Failed to toggle subscription: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteSubscription(id: Long): Boolean {
        println("🗑️ Repository: Deleting subscription ID: $id")
        return try {
            _isLoading.value = true
            _error.value = null
            val success = apiService.deleteSubscription(id)
            if (success) {
                println("✅ Repository: Deleted subscription ID: $id")
                _subscriptions.value = _subscriptions.value.filter { it.id != id }
                loadTotals()
                true
            } else {
                println("❌ Repository: Failed to delete subscription - no response")
                _error.value = "Failed to delete subscription"
                false
            }
        } catch (e: Exception) {
            println("❌ Repository: Failed to delete subscription: ${e.message}")
            _error.value = "Failed to delete subscription: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        println("🧹 Repository: Clearing error state")
        _error.value = null
    }
} 