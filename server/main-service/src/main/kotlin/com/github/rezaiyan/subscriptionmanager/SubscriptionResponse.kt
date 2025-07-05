package com.github.rezaiyan.subscriptionmanager

import java.math.BigDecimal
import java.time.Instant

data class SubscriptionResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val amount: BigDecimal,
    val frequency: SubscriptionFrequency,
    val startDate: String?,
    val nextBillingDate: String?,
    val active: Boolean,
    val createdAt: String?,
    val monthlyAmount: BigDecimal,
    val yearlyAmount: BigDecimal
) {
    companion object {
        fun fromSubscription(subscription: Subscription): SubscriptionResponse {
            return SubscriptionResponse(
                id = subscription.id,
                name = subscription.name,
                description = subscription.description,
                amount = subscription.amount,
                frequency = subscription.frequency,
                startDate = subscription.startDate?.toString(),
                nextBillingDate = subscription.nextBillingDate?.toString(),
                active = subscription.active,
                createdAt = subscription.createdAt?.toString(),
                monthlyAmount = subscription.getMonthlyAmount(),
                yearlyAmount = subscription.getYearlyAmount()
            )
        }
    }
} 