package com.github.rezaiyan.subscriptionmanager

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Component
class SubscriptionEventListener(
    private val subscriptionRepository: SubscriptionRepository
) {

    @EventListener
    @Transactional
    fun handleSubscriptionCreatedEvent(event: SubscriptionCreatedEvent) {
        // Create subscription in main application database
        val subscription = Subscription(
            id = event.subscriptionId,
            name = event.name,
            description = event.description,
            amount = event.amount,
            frequency = event.frequency,
            startDate = event.startDate,
            active = event.active,
            createdAt = event.createdAt
        )
        
        subscriptionRepository.save(subscription)
        println("Synced subscription ${event.subscriptionId} to main database")
    }
} 