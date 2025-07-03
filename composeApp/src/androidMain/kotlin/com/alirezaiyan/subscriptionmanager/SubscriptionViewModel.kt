package com.alirezaiyan.subscriptionmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> = repository.subscriptions
    val totals: StateFlow<SubscriptionTotals> = repository.totals
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    init {
        println("📱 SubscriptionViewModel initialized")
        loadSubscriptions()
    }

    fun loadSubscriptions() {
        println("🔄 ViewModel: Loading subscriptions")
        viewModelScope.launch {
            repository.loadSubscriptions()
        }
    }

    fun loadActiveSubscriptions() {
        println("🔄 ViewModel: Loading active subscriptions")
        viewModelScope.launch {
            repository.loadActiveSubscriptions()
        }
    }

    fun searchSubscriptions(query: String) {
        println("🔍 ViewModel: Searching subscriptions with query: '$query'")
        viewModelScope.launch {
            repository.searchSubscriptions(query)
        }
    }

    fun createSubscription(
        name: String,
        description: String?,
        amount: Double,
        frequency: SubscriptionFrequency
    ) {
        println("➕ ViewModel: Creating subscription - Name: $name, Amount: $amount, Frequency: $frequency")
        viewModelScope.launch {
            val subscription = Subscription(
                name = name,
                description = description,
                amount = amount,
                frequency = frequency,
                startDate = java.time.Instant.now().toString(),
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
} 