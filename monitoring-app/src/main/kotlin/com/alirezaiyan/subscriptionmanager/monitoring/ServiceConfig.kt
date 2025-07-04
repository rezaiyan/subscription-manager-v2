package com.alirezaiyan.subscriptionmanager.monitoring

object ServiceConfig {
    val SERVICES = listOf(
        Service(
            name = "Zookeeper",
            url = "localhost:2181",
            healthEndpoint = null,
            category = "infrastructure"
        ),
        Service(
            name = "Kafka",
            url = "localhost:9092",
            healthEndpoint = null,
            category = "infrastructure"
        ),
        Service(
            name = "PostgreSQL Main",
            url = "localhost:5432",
            healthEndpoint = null,
            category = "infrastructure"
        ),
        Service(
            name = "PostgreSQL Create",
            url = "localhost:5433",
            healthEndpoint = null,
            category = "infrastructure"
        ),
        Service(
            name = "Eureka Server",
            url = "http://localhost:8761",
            healthEndpoint = "http://localhost:8761/actuator/health",
            category = "service-discovery"
        ),
        Service(
            name = "Config Server",
            url = "http://localhost:8888",
            healthEndpoint = "http://localhost:8888/actuator/health",
            category = "configuration"
        ),
        Service(
            name = "Create Subscription Service",
            url = "http://localhost:3001",
            healthEndpoint = "http://localhost:3001/actuator/health",
            category = "business-service"
        ),
        Service(
            name = "Main Server",
            url = "http://localhost:3000",
            healthEndpoint = "http://localhost:3000/actuator/health",
            category = "business-service"
        ),
        Service(
            name = "API Gateway",
            url = "http://localhost:8080",
            healthEndpoint = "http://localhost:8080/",
            category = "gateway"
        ),
        Service(
            name = "Website",
            url = "http://localhost:8081",
            healthEndpoint = "http://localhost:8081",
            category = "frontend"
        )
    )
} 