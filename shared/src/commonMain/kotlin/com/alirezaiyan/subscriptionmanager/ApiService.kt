package com.alirezaiyan.subscriptionmanager

interface ApiService {
    suspend fun getAllSubscriptions(): List<Subscription>
    suspend fun getActiveSubscriptions(): List<Subscription>
    suspend fun getSubscriptionById(id: Long): Subscription?
    suspend fun searchSubscriptions(name: String): List<Subscription>
    suspend fun getSubscriptionsByFrequency(frequency: SubscriptionFrequency): List<Subscription>
    suspend fun getSubscriptionTotals(): SubscriptionTotals
    suspend fun createSubscription(subscription: Subscription): Subscription
    suspend fun updateSubscription(id: Long, subscription: Subscription): Subscription?
    suspend fun toggleSubscriptionActive(id: Long): Subscription?
    suspend fun deleteSubscription(id: Long): Boolean
    fun close()
} 