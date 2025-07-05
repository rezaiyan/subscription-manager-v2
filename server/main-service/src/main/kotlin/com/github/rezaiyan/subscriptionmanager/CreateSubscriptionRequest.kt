package com.github.rezaiyan.subscriptionmanager

import java.math.BigDecimal

data class CreateSubscriptionRequest(
    val name: String,
    val description: String? = null,
    val amount: BigDecimal,
    val frequency: SubscriptionFrequency,
    val isActive: Boolean = true
) {
    fun toSubscription(): Subscription {
        return Subscription(
            name = name,
            description = description,
            amount = amount,
            frequency = frequency,
            active = isActive
        )
    }
} 