package com.github.rezaiyan.subscriptionmanager

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    // Find subscriptions by name (case-insensitive)
    fun findByNameContainingIgnoreCase(name: String): List<Subscription>
    
    // Find active subscriptions
    fun findByActiveTrue(): List<Subscription>
    
    // Find subscriptions by frequency
    fun findByFrequency(frequency: SubscriptionFrequency): List<Subscription>
    
    // Custom query to calculate total monthly amount
    @Query("SELECT SUM(CASE WHEN s.frequency = 'MONTHLY' THEN s.amount ELSE s.amount / 12 END) FROM Subscription s WHERE s.active = true")
    fun calculateTotalMonthlyAmount(): BigDecimal?
    
    // Custom query to calculate total yearly amount
    @Query("SELECT SUM(CASE WHEN s.frequency = 'YEARLY' THEN s.amount ELSE s.amount * 12 END) FROM Subscription s WHERE s.active = true")
    fun calculateTotalYearlyAmount(): BigDecimal?
}