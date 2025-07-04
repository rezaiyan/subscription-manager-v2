package com.github.rezaiyan.subscriptionmanager

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CreateSubscriptionRepository : JpaRepository<Subscription, Long> {
    // Basic CRUD operations inherited from JpaRepository
    // This service only handles creation, so we keep it simple
} 