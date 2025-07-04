package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KafkaEventListener {

    private val logger = LoggerFactory.getLogger(KafkaEventListener::class.java)

    @Autowired
    private lateinit var subscriptionRepository: SubscriptionRepository

    @KafkaListener(
        topics = ["subscription-created"],
        groupId = "subscription-manager-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleSubscriptionCreatedEvent(event: SubscriptionCreatedEvent) {
        logger.info("Received subscription created event: {}", event)
        
        try {
            // Convert the event to a Subscription entity and save to local database
            val subscription = Subscription(
                id = event.subscriptionId,
                name = event.name,
                description = event.description,
                amount = event.amount,
                frequency = event.frequency,
                active = event.active,
                startDate = event.startDate,
                nextBillingDate = null, // Will be calculated by the service
                createdAt = event.createdAt
            )
            
            subscriptionRepository.save(subscription)
            logger.info("Successfully synced subscription {} to local database", event.subscriptionId)
            
        } catch (e: Exception) {
            logger.error("Failed to sync subscription {} to local database: {}", event.subscriptionId, e.message, e)
        }
    }
} 