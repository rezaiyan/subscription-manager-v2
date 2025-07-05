package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionRepository: SubscriptionRepository,
    private val createSubscriptionServiceClient: CreateSubscriptionServiceClient
) {
    private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)

    @GetMapping
    fun getAllSubscriptions(): ResponseEntity<List<SubscriptionResponse>> {
        logger.info("GET /api/subscriptions - Fetching all subscriptions from local database")
        val subscriptions = subscriptionRepository.findAll()
        val responses = subscriptions.map { SubscriptionResponse.fromSubscription(it) }
        logger.info("GET /api/subscriptions - Found ${subscriptions.size} subscriptions")
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/active")
    fun getActiveSubscriptions(): ResponseEntity<List<SubscriptionResponse>> {
        // Get active subscriptions from local database (synced via Kafka events)
        val subscriptions = subscriptionRepository.findByActiveTrue()
        val responses = subscriptions.map { SubscriptionResponse.fromSubscription(it) }
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/{id}")
    fun getSubscriptionById(@PathVariable id: Long): ResponseEntity<SubscriptionResponse> {
        val subscription = subscriptionRepository.findById(id)
        return if (subscription.isPresent) {
            ResponseEntity.ok(SubscriptionResponse.fromSubscription(subscription.get()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    fun searchSubscriptions(@RequestParam name: String): List<SubscriptionResponse> {
        val subscriptions = subscriptionRepository.findByNameContainingIgnoreCase(name)
        return subscriptions.map { SubscriptionResponse.fromSubscription(it) }
    }

    @GetMapping("/by-frequency/{frequency}")
    fun getSubscriptionsByFrequency(@PathVariable frequency: SubscriptionFrequency): List<SubscriptionResponse> {
        val subscriptions = subscriptionRepository.findByFrequency(frequency)
        return subscriptions.map { SubscriptionResponse.fromSubscription(it) }
    }

    @GetMapping("/totals")
    fun getSubscriptionTotals(): ResponseEntity<Map<String, BigDecimal>> {
        // Get totals from local database (synced via Kafka events)
        val monthlyTotal = subscriptionRepository.calculateTotalMonthlyAmount() ?: BigDecimal.ZERO
        val yearlyTotal = subscriptionRepository.calculateTotalYearlyAmount() ?: BigDecimal.ZERO
        
        return ResponseEntity.ok(mapOf(
            "monthlyTotal" to monthlyTotal,
            "yearlyTotal" to yearlyTotal
        ))
    }

    @PostMapping
    fun createSubscription(@RequestBody request: CreateSubscriptionRequest): ResponseEntity<SubscriptionResponse> {
        logger.info("POST /api/subscriptions - Creating subscription directly: ${request.name}, amount: ${request.amount}, isActive: ${request.isActive}")
        try {
            // Create subscription directly in this service
            val subscription = request.toSubscription()
            val savedSubscription = subscriptionRepository.save(subscription)
            val response = SubscriptionResponse.fromSubscription(savedSubscription)
            logger.info("POST /api/subscriptions - Successfully created subscription with ID: ${savedSubscription.id}")
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("POST /api/subscriptions - Error creating subscription: ${e.message}", e)
            return ResponseEntity.status(503).build()
        }
    }

    @PutMapping("/{id}")
    fun updateSubscription(
        @PathVariable id: Long,
        @RequestBody subscription: Subscription
    ): ResponseEntity<SubscriptionResponse> {
        requireNotNull(subscription.frequency) { "Frequency is required" }
        return if (subscriptionRepository.existsById(id)) {
            // Create a new subscription with the ID from the path
            val updatedSubscription = subscription.copy(id = id)
            val savedSubscription = subscriptionRepository.save(updatedSubscription)
            val response = SubscriptionResponse.fromSubscription(savedSubscription)
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}/toggle-active")
    fun toggleSubscriptionActive(@PathVariable id: Long): ResponseEntity<SubscriptionResponse> {
        val subscriptionOptional = subscriptionRepository.findById(id)
        
        return if (subscriptionOptional.isPresent) {
            val subscription = subscriptionOptional.get()
            val updatedSubscription = subscription.copy(active = !subscription.active)
            val savedSubscription = subscriptionRepository.save(updatedSubscription)
            val response = SubscriptionResponse.fromSubscription(savedSubscription)
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteSubscription(@PathVariable id: Long): ResponseEntity<Void> {
        return if (subscriptionRepository.existsById(id)) {
            subscriptionRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}