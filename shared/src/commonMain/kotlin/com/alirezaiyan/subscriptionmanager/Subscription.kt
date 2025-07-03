package com.alirezaiyan.subscriptionmanager

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class SubscriptionFrequency {
    @SerialName("MONTHLY")
    MONTHLY,
    @SerialName("YEARLY")
    YEARLY
}

@Serializable
data class Subscription(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    @SerialName("amount")
    val price: Double,
    val frequency: SubscriptionFrequency,
    @SerialName("startDate")
    val startDate: String? = null,
    @SerialName("nextBillingDate")
    val nextBillingDate: String? = null,
    @SerialName("active")
    val isActive: Boolean = true,
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("monthlyAmount")
    val monthlyAmount: Double,
    @SerialName("yearlyAmount")
    val yearlyAmount: Double
)

@Serializable
data class SubscriptionTotals(
    @SerialName("monthlyTotal")
    val monthlyTotal: Double,
    @SerialName("yearlyTotal")
    val yearlyTotal: Double
) 