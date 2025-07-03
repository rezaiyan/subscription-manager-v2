package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.math.BigDecimal
import java.time.Instant

@SpringBootApplication
@EnableDiscoveryClient
class CreateSubscriptionServiceApplication {
    private val logger = LoggerFactory.getLogger(CreateSubscriptionServiceApplication::class.java)
    
    @Bean
    fun dataLoader(repository: CreateSubscriptionRepository) = CommandLineRunner {
        logger.info("CreateSubscriptionServiceApplication - Starting data loader")
        if (repository.count() == 0L) {
            logger.info("CreateSubscriptionServiceApplication - Database is empty, loading initial data")
            repository.saveAll(
                listOf(
                    Subscription(
                        name = "Netflix",
                        description = "Streaming Service",
                        amount = BigDecimal("15.99"),
                        frequency = SubscriptionFrequency.MONTHLY,
                        startDate = Instant.now().minusSeconds(60*60*24*30),
                        active = true
                    ),
                    Subscription(
                        name = "Spotify",
                        description = "Music Streaming",
                        amount = BigDecimal("9.99"),
                        frequency = SubscriptionFrequency.MONTHLY,
                        startDate = Instant.now().minusSeconds(60*60*24*60),
                        active = true
                    ),
                    Subscription(
                        name = "Amazon Prime",
                        description = "Shopping + Video",
                        amount = BigDecimal("119.00"),
                        frequency = SubscriptionFrequency.YEARLY,
                        startDate = Instant.now().minusSeconds(60*60*24*365),
                        active = true
                    ),
                    Subscription(
                        name = "Adobe Creative Cloud",
                        description = "Design Tools",
                        amount = BigDecimal("52.99"),
                        frequency = SubscriptionFrequency.MONTHLY,
                        startDate = Instant.now().minusSeconds(60*60*24*90),
                        active = false
                    )
                )
            )
            logger.info("CreateSubscriptionServiceApplication - Initial data loaded successfully")
        } else {
            logger.info("CreateSubscriptionServiceApplication - Database already contains data, skipping initial load")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CreateSubscriptionServiceApplication>(*args)
} 