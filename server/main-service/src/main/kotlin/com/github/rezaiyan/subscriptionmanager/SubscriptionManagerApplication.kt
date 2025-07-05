package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant

@SpringBootApplication
@EnableDiscoveryClient
class SubscriptionManagerApplication {
    private val logger = LoggerFactory.getLogger(SubscriptionManagerApplication::class.java)
    
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600)
            }
        }
    }
    
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
    
    @Bean
    fun dataLoader(repository: SubscriptionRepository) = CommandLineRunner {
        logger.info("SubscriptionManagerApplication - Starting data loader")
        
        // Clean up any existing subscriptions with null frequency
        val subscriptionsWithNullFrequency = repository.findAll().filter { it.frequency == null }
        if (subscriptionsWithNullFrequency.isNotEmpty()) {
            logger.info("SubscriptionManagerApplication - Found ${subscriptionsWithNullFrequency.size} subscriptions with null frequency, fixing them")
            val fixedSubscriptions = subscriptionsWithNullFrequency.map { 
                it.copy(frequency = SubscriptionFrequency.MONTHLY) 
            }
            repository.saveAll(fixedSubscriptions)
            logger.info("SubscriptionManagerApplication - Fixed ${fixedSubscriptions.size} subscriptions with null frequency")
        } else {
            logger.info("SubscriptionManagerApplication - No subscriptions with null frequency found")
        }
        
        // Note: Initial data loading is now handled by the create-subscription-service
        // This ensures data consistency across services
        logger.info("SubscriptionManagerApplication - Data loading completed. New subscriptions should be created via the microservice.")
    }
}

fun main(args: Array<String>) {
    runApplication<SubscriptionManagerApplication>(*args)
}