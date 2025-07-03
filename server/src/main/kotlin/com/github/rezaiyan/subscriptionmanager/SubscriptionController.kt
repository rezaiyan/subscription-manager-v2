package com.github.rezaiyan.subscriptionmanager

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(private val subscriptionRepository: SubscriptionRepository) {

    @GetMapping
    fun getAllSubscriptions(): List<Subscription> {
        return subscriptionRepository.findAll()
    }

    @GetMapping("/active")
    fun getActiveSubscriptions(): List<Subscription> {
        return subscriptionRepository.findByActiveTrue()
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
    fun getSubscriptionTotals(): Map<String, BigDecimal> {
        val monthlyTotal = subscriptionRepository.calculateTotalMonthlyAmount() ?: BigDecimal.ZERO
        val yearlyTotal = subscriptionRepository.calculateTotalYearlyAmount() ?: BigDecimal.ZERO
        
        return mapOf(
            "monthlyTotal" to monthlyTotal,
            "yearlyTotal" to yearlyTotal
        )
    }

    @PostMapping
    fun createSubscription(@RequestBody subscription: Subscription): Subscription {
        requireNotNull(subscription.frequency) { "Frequency is required" }
        return subscriptionRepository.save(subscription)
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