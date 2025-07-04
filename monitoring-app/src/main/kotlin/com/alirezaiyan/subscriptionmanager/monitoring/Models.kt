package com.alirezaiyan.subscriptionmanager.monitoring

import java.time.Instant

data class Service(
    val name: String,
    val url: String,
    val healthEndpoint: String? = null,
    val category: String
)

data class HealthStatus(
    val status: String, // "up", "down", "warning"
    val responseTime: Long,
    val details: String,
    val statusCode: Int? = null
)

data class ServiceHealth(
    val service: Service,
    val health: HealthStatus
)

data class OverallHealth(
    val total: Int,
    val up: Int,
    val down: Int,
    val status: String // "up", "warning", "down"
)

data class HealthResponse(
    val timestamp: Long = Instant.now().epochSecond,
    val overall: OverallHealth,
    val services: List<ServiceHealth>
)

data class SingleServiceHealthResponse(
    val service: Service,
    val health: HealthStatus,
    val timestamp: Long = Instant.now().epochSecond
) 