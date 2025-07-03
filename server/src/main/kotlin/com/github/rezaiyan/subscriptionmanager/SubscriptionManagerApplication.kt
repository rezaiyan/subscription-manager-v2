package com.github.rezaiyan.subscriptionmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.math.BigDecimal
import java.time.Instant

@SpringBootApplication
class SubscriptionManagerApplication {
    
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:8080", "http://localhost:3000", "http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }
        }
    }
    
    @Bean
    fun dataLoader(repository: SubscriptionRepository) = CommandLineRunner {
        // Clean up any existing subscriptions with null frequency
        val subscriptionsWithNullFrequency = repository.findAll().filter { it.frequency == null }
        if (subscriptionsWithNullFrequency.isNotEmpty()) {
            val fixedSubscriptions = subscriptionsWithNullFrequency.map { 
                it.copy(frequency = SubscriptionFrequency.MONTHLY) 
            }
            repository.saveAll(fixedSubscriptions)
            println("Fixed ${fixedSubscriptions.size} subscriptions with null frequency")
        }
        
        if (repository.count() == 0L) {
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
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SubscriptionManagerApplication>(*args)
}