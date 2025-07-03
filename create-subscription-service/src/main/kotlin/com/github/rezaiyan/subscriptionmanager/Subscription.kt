package com.github.rezaiyan.subscriptionmanager

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

enum class SubscriptionFrequency {
    MONTHLY,
    YEARLY
}

@Entity
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val name: String,
    val description: String? = null,
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val frequency: SubscriptionFrequency,

    val startDate: Instant = Instant.now(),
    val nextBillingDate: Instant? = null,
    val active: Boolean = true,

    val createdAt: Instant = Instant.now()
) {
    // Calculate monthly amount for yearly subscriptions
    fun getMonthlyAmount(): BigDecimal {
        return when (frequency) {
            SubscriptionFrequency.MONTHLY -> amount
            SubscriptionFrequency.YEARLY -> amount.divide(BigDecimal(12), 2, BigDecimal.ROUND_HALF_UP)
        }
    }

    // Calculate yearly amount for monthly subscriptions
    fun getYearlyAmount(): BigDecimal {
        return when (frequency) {
            SubscriptionFrequency.MONTHLY -> amount.multiply(BigDecimal(12))
            SubscriptionFrequency.YEARLY -> amount
        }
    }
} 