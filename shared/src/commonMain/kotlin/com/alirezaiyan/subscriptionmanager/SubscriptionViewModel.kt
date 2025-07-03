package com.alirezaiyan.subscriptionmanager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    val subscriptions: StateFlow<List<Subscription>> = repository.subscriptions
    val totals: StateFlow<SubscriptionTotals> = repository.totals
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    // Add dialog state
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        println("📱 SubscriptionViewModel initialized")
        loadSubscriptions()
    }

    fun loadSubscriptions() {
        println("🔄 ViewModel: Loading subscriptions")
        coroutineScope.launch {
            repository.loadSubscriptions()
        }
    }

    fun loadActiveSubscriptions() {
        println("🔄 ViewModel: Loading active subscriptions")
        coroutineScope.launch {
            repository.loadActiveSubscriptions()
        }
    }

    fun searchSubscriptions(query: String) {
        println("🔍 ViewModel: Searching subscriptions with query: '$query'")
        coroutineScope.launch {
            repository.searchSubscriptions(query)
        }
    }

    fun addSubscription(subscription: Subscription) {
        println("➕ ViewModel: Adding subscription - Name: ${subscription.name}, Price: ${subscription.price}, Frequency: ${subscription.frequency}")
        coroutineScope.launch {
            val success = repository.createSubscription(subscription)
            if (success) {
                println("✅ ViewModel: Successfully created subscription")
            } else {
                println("❌ ViewModel: Failed to create subscription")
            }
        }
    }

    fun createSubscription(
        name: String,
        description: String?,
        amount: Double,
        frequency: SubscriptionFrequency
    ) {
        println("➕ ViewModel: Creating subscription - Name: $name, Amount: $amount, Frequency: $frequency")
        coroutineScope.launch {
            val subscription = Subscription(
                name = name,
                description = description,
                price = amount,
                frequency = frequency,
                startDate = getCurrentTimestamp(),
                monthlyAmount = if (frequency == SubscriptionFrequency.MONTHLY) amount else amount / 12.0,
                yearlyAmount = if (frequency == SubscriptionFrequency.YEARLY) amount else amount * 12.0
            )
            val success = repository.createSubscription(subscription)
            if (success) {
                println("✅ ViewModel: Successfully created subscription")
            } else {
                println("❌ ViewModel: Failed to create subscription")
            }
        }
    }

    fun updateSubscription(subscription: Subscription) {
        println("✏️ ViewModel: Updating subscription ID: ${subscription.id}")
        coroutineScope.launch {
            val success = repository.updateSubscription(subscription)
            if (success) {
                println("✅ ViewModel: Successfully updated subscription")
            } else {
                println("❌ ViewModel: Failed to update subscription")
            }
        }
    }

    fun toggleSubscriptionActive(id: Long) {
        println("🔄 ViewModel: Toggling subscription active state for ID: $id")
        coroutineScope.launch {
            val success = repository.toggleSubscriptionActive(id)
            if (success) {
                println("✅ ViewModel: Successfully toggled subscription")
            } else {
                println("❌ ViewModel: Failed to toggle subscription")
            }
        }
    }

    fun deleteSubscription(id: Long) {
        println("🗑️ ViewModel: Deleting subscription ID: $id")
        coroutineScope.launch {
            val success = repository.deleteSubscription(id)
            if (success) {
                println("✅ ViewModel: Successfully deleted subscription")
            } else {
                println("❌ ViewModel: Failed to delete subscription")
            }
        }
    }

    fun clearError() {
        println("🧹 ViewModel: Clearing error")
        repository.clearError()
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    private fun getCurrentTimestamp(): String {
        return Clock.System.now().toString()
    }
} 