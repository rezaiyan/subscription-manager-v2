package com.github.rezaiyan.subscriptionmanager

import java.math.BigDecimal
import java.time.Instant

data class SubscriptionCreatedEvent(
    val subscriptionId: Long,
    val name: String,
    val description: String?,
    val amount: BigDecimal,
    val frequency: SubscriptionFrequency,
    val startDate: Instant,
    val active: Boolean,
    val createdAt: Instant,
    val eventTimestamp: Instant = Instant.now()
) 