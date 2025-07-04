package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient

@Service
class CreateSubscriptionServiceClient(
    private val restTemplate: RestTemplate,
    private val discoveryClient: DiscoveryClient
) {
    private val logger = LoggerFactory.getLogger(CreateSubscriptionServiceClient::class.java)

    fun createSubscription(subscription: Subscription): ResponseEntity<Subscription> {
        logger.info("CreateSubscriptionServiceClient - Attempting to create subscription: ${subscription.name}")
        try {
            // Get service instance from Eureka
            val instances = discoveryClient.getInstances("create-subscription-service")
            logger.info("CreateSubscriptionServiceClient - Found ${instances.size} instances of create-subscription-service")
            
            if (instances.isEmpty()) {
                logger.error("CreateSubscriptionServiceClient - No instances of create-subscription-service available")
                throw RuntimeException("Create subscription service not available")
            }
            
            val serviceInstance = instances[0]
            val url = "${serviceInstance.uri}/api/subscriptions"
            logger.info("CreateSubscriptionServiceClient - Calling URL: $url")
            
            // Prepare headers
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            
            // Create HTTP entity
            val entity = HttpEntity(subscription, headers)
            
            // Make the call
            val response = restTemplate.postForEntity(url, entity, Subscription::class.java)
            logger.info("CreateSubscriptionServiceClient - Response status: ${response.statusCode}")
            return response
        } catch (e: Exception) {
            logger.error("CreateSubscriptionServiceClient - Error creating subscription: ${e.message}", e)
            // Fallback: return error response
            return ResponseEntity.status(503).build()
        }
    }

    fun getAllSubscriptions(): ResponseEntity<List<Subscription>> {
        try {
            // Get service instance from Eureka
            val instances = discoveryClient.getInstances("create-subscription-service")
            if (instances.isEmpty()) {
                throw RuntimeException("Create subscription service not available")
            }
            
            val serviceInstance = instances[0]
            val url = "${serviceInstance.uri}/api/subscriptions"
            
            // Make the call with proper type
            val response = restTemplate.getForEntity(url, Array<Subscription>::class.java)
            return ResponseEntity.ok(response.body?.toList() ?: emptyList())
        } catch (e: Exception) {
            // Fallback: return error response
            return ResponseEntity.status(503).build()
        }
    }
} 