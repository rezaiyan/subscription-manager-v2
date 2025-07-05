package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/subscriptions")
class CreateSubscriptionController(
    private val createSubscriptionRepository: CreateSubscriptionRepository,
    private val kafkaEventPublisher: KafkaEventPublisher
) {
    private val logger = LoggerFactory.getLogger(CreateSubscriptionController::class.java)

    @GetMapping
    fun getAllSubscriptions(): ResponseEntity<List<SubscriptionResponse>> {
        logger.info("GET /api/subscriptions - Fetching all subscriptions")
        try {
            val subscriptions = createSubscriptionRepository.findAll()
            val responses = subscriptions.map { SubscriptionResponse.fromSubscription(it) }
            logger.info("GET /api/subscriptions - Successfully fetched ${subscriptions.size} subscriptions")
            return ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("GET /api/subscriptions - Error fetching subscriptions: ${e.message}", e)
            return ResponseEntity.status(503).build()
        }
    }

    @PostMapping
    fun createSubscription(@RequestBody request: CreateSubscriptionRequest): ResponseEntity<SubscriptionResponse> {
        logger.info("POST /api/subscriptions - Creating subscription: ${request.name}, amount: ${request.amount}, frequency: ${request.frequency}, isActive: ${request.isActive}")
        try {
            // Basic validation
            if (request.frequency == null) {
                logger.warn("POST /api/subscriptions - Validation failed: frequency is null")
                return ResponseEntity.badRequest().build()
            }
            if (request.name.isBlank()) {
                logger.warn("POST /api/subscriptions - Validation failed: name is blank")
                return ResponseEntity.badRequest().build()
            }
            if (request.amount <= BigDecimal.ZERO) {
                logger.warn("POST /api/subscriptions - Validation failed: amount <= 0")
                return ResponseEntity.badRequest().build()
            }

            logger.info("POST /api/subscriptions - Validation passed, converting request to subscription")
            val subscription = request.toSubscription()
            
            logger.info("POST /api/subscriptions - Saving subscription")
            val savedSubscription = createSubscriptionRepository.save(subscription)
            logger.info("POST /api/subscriptions - Subscription saved with ID: ${savedSubscription.id}")
            
            // Publish event for data synchronization
            logger.info("POST /api/subscriptions - Publishing Kafka event for subscription ID: ${savedSubscription.id}")
            val event = SubscriptionCreatedEvent(
                subscriptionId = savedSubscription.id,
                name = savedSubscription.name,
                description = savedSubscription.description,
                amount = savedSubscription.amount,
                frequency = savedSubscription.frequency,
                startDate = savedSubscription.startDate ?: Instant.now(),
                active = savedSubscription.active,
                createdAt = savedSubscription.createdAt ?: Instant.now()
            )
            kafkaEventPublisher.publishSubscriptionCreatedEvent(event)
            logger.info("POST /api/subscriptions - Kafka event published successfully")
            
            val response = SubscriptionResponse.fromSubscription(savedSubscription)
            logger.info("POST /api/subscriptions - Successfully created subscription: ${savedSubscription.id}")
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("POST /api/subscriptions - Error creating subscription: ${e.message}", e)
            return ResponseEntity.status(503).build()
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        logger.info("GET /api/subscriptions/health - Health check requested")
        return ResponseEntity.ok(mapOf("status" to "UP", "service" to "create-subscription-service"))
    }
} 