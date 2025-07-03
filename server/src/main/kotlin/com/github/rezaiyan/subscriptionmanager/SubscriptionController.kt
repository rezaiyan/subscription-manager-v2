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
    fun getAllSubscriptions(): ResponseEntity<List<Subscription>> {
        logger.info("GET /api/subscriptions - Fetching all subscriptions from local database")
        val subscriptions = subscriptionRepository.findAll()
        logger.info("GET /api/subscriptions - Found ${subscriptions.size} subscriptions")
        return ResponseEntity.ok(subscriptions)
    }

    @GetMapping("/active")
    fun getActiveSubscriptions(): ResponseEntity<List<Subscription>> {
        // Get active subscriptions from local database (synced via Kafka events)
        return ResponseEntity.ok(subscriptionRepository.findByActiveTrue())
    }

    @GetMapping("/{id}")
    fun getSubscriptionById(@PathVariable id: Long): ResponseEntity<Subscription> {
        val subscription = subscriptionRepository.findById(id)
        return if (subscription.isPresent) {
            ResponseEntity.ok(subscription.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/search")
    fun searchSubscriptions(@RequestParam name: String): List<Subscription> {
        return subscriptionRepository.findByNameContainingIgnoreCase(name)
    }

    @GetMapping("/by-frequency/{frequency}")
    fun getSubscriptionsByFrequency(@PathVariable frequency: SubscriptionFrequency): List<Subscription> {
        return subscriptionRepository.findByFrequency(frequency)
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
    fun createSubscription(@RequestBody subscription: Subscription): ResponseEntity<Subscription> {
        logger.info("POST /api/subscriptions - Creating subscription directly: ${subscription.name}, amount: ${subscription.amount}")
        try {
            // Create subscription directly in this service
            val savedSubscription = subscriptionRepository.save(subscription)
            logger.info("POST /api/subscriptions - Successfully created subscription with ID: ${savedSubscription.id}")
            return ResponseEntity.ok(savedSubscription)
        } catch (e: Exception) {
            logger.error("POST /api/subscriptions - Error creating subscription: ${e.message}", e)
            return ResponseEntity.status(503).build()
        }
    }

    @PutMapping("/{id}")
    fun updateSubscription(
        @PathVariable id: Long,
        @RequestBody subscription: Subscription
    ): ResponseEntity<Subscription> {
        requireNotNull(subscription.frequency) { "Frequency is required" }
        return if (subscriptionRepository.existsById(id)) {
            // Create a new subscription with the ID from the path
            val updatedSubscription = subscription.copy(id = id)
            ResponseEntity.ok(subscriptionRepository.save(updatedSubscription))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}/toggle-active")
    fun toggleSubscriptionActive(@PathVariable id: Long): ResponseEntity<Subscription> {
        val subscriptionOptional = subscriptionRepository.findById(id)
        
        return if (subscriptionOptional.isPresent) {
            val subscription = subscriptionOptional.get()
            val updatedSubscription = subscription.copy(active = !subscription.active)
            ResponseEntity.ok(subscriptionRepository.save(updatedSubscription))
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