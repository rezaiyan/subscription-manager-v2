package com.github.rezaiyan.subscriptionmanager

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class KafkaEventPublisher {

    private val logger = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    companion object {
        const val SUBSCRIPTION_CREATED_TOPIC = "subscription-created"
    }

    fun publishSubscriptionCreatedEvent(event: SubscriptionCreatedEvent) {
        logger.info("Publishing subscription created event: {}", event)
        val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(SUBSCRIPTION_CREATED_TOPIC, event.subscriptionId.toString(), event)
        future.whenComplete { result, ex ->
            if (ex != null) {
                logger.error("Failed to publish subscription created event: {}", ex.message, ex)
            } else {
                logger.info("Successfully published subscription created event to topic: {}, partition: {}, offset: {}", 
                    result.recordMetadata.topic(), 
                    result.recordMetadata.partition(), 
                    result.recordMetadata.offset())
            }
        }
    }
} 