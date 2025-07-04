package com.alirezaiyan.subscriptionmanager.monitoring

data class HealthData(
    val overall: OverallHealth,
    val services: List<ServiceHealthResponse>
)

data class OverallHealth(
    val status: String,
    val up: Int,
    val total: Int
)

data class ServiceHealthResponse(
    val service: Service,
    val health: Health
)

data class Service(
    val name: String,
    val url: String,
    val category: String
)

data class Health(
    val status: String,
    val responseTime: Long
) 