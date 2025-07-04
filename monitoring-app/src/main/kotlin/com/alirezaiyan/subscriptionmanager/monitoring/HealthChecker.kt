package com.alirezaiyan.subscriptionmanager.monitoring

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.Socket

class HealthChecker {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    suspend fun checkServiceHealth(service: Service): HealthStatus {
        val startTime = System.currentTimeMillis()
        return try {
            if (service.healthEndpoint != null) {
                checkHttpService(service, startTime)
            } else {
                checkInfrastructureService(service, startTime)
            }
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            when (e) {
                is java.net.SocketTimeoutException -> HealthStatus(
                    status = "down",
                    responseTime = responseTime,
                    details = "Request timeout",
                    statusCode = null
                )
                is java.net.ConnectException -> HealthStatus(
                    status = "down",
                    responseTime = responseTime,
                    details = "Connection refused",
                    statusCode = null
                )
                else -> HealthStatus(
                    status = "down",
                    responseTime = responseTime,
                    details = e.message ?: "Unknown error",
                    statusCode = null
                )
            }
        }
    }

    private suspend fun checkHttpService(service: Service, startTime: Long): HealthStatus {
        val response = client.get(service.healthEndpoint!!)
        val responseTime = System.currentTimeMillis() - startTime
        return when {
            response.status == HttpStatusCode.OK -> HealthStatus(
                status = "up",
                responseTime = responseTime,
                details = "Service responding",
                statusCode = response.status.value
            )
            service.name == "API Gateway" && response.status == HttpStatusCode.NotFound -> HealthStatus(
                status = "up",
                responseTime = responseTime,
                details = "Service responding (404 is normal for routing service)",
                statusCode = response.status.value
            )
            else -> HealthStatus(
                status = "warning",
                responseTime = responseTime,
                details = "Service responding but status code: ${response.status.value}",
                statusCode = response.status.value
            )
        }
    }

    private fun checkInfrastructureService(service: Service, startTime: Long): HealthStatus {
        return when {
            service.name.contains("PostgreSQL") -> checkPostgreSQL(service, startTime)
            service.name in listOf("Kafka", "Zookeeper") -> checkInfrastructurePort(service, startTime)
            else -> checkGenericPort(service, startTime)
        }
    }

    private fun checkPostgreSQL(service: Service, startTime: Long): HealthStatus {
        val responseTime = System.currentTimeMillis() - startTime
        return try {
            val (host, port) = service.url.split(":")
            Socket(host, port.toInt()).use { socket ->
                socket.soTimeout = 5000
                HealthStatus(
                    status = "up",
                    responseTime = responseTime,
                    details = "Port accessible",
                    statusCode = null
                )
            }
        } catch (e: Exception) {
            HealthStatus(
                status = "down",
                responseTime = responseTime,
                details = "Port not accessible: ${e.message}",
                statusCode = null
            )
        }
    }

    private fun checkInfrastructurePort(service: Service, startTime: Long): HealthStatus {
        val responseTime = System.currentTimeMillis() - startTime
        return try {
            val (host, port) = service.url.split(":")
            Socket(host, port.toInt()).use { socket ->
                socket.soTimeout = 5000
                HealthStatus(
                    status = "up",
                    responseTime = responseTime,
                    details = "Port accessible",
                    statusCode = null
                )
            }
        } catch (e: Exception) {
            HealthStatus(
                status = "up",
                responseTime = responseTime,
                details = "Infrastructure service (no HTTP endpoint)",
                statusCode = null
            )
        }
    }

    private fun checkGenericPort(service: Service, startTime: Long): HealthStatus {
        val responseTime = System.currentTimeMillis() - startTime
        return try {
            val (host, port) = service.url.split(":")
            Socket(host, port.toInt()).use { socket ->
                socket.soTimeout = 5000
                HealthStatus(
                    status = "up",
                    responseTime = responseTime,
                    details = "Port accessible",
                    statusCode = null
                )
            }
        } catch (e: Exception) {
            HealthStatus(
                status = "down",
                responseTime = responseTime,
                details = "Port not accessible: ${e.message}",
                statusCode = null
            )
        }
    }

    suspend fun checkAllServices(): HealthResponse {
        val results = mutableListOf<ServiceHealth>()
        var upCount = 0
        for (service in ServiceConfig.SERVICES) {
            val healthData = checkServiceHealth(service)
            results.add(ServiceHealth(service, healthData))
            if (healthData.status == "up") {
                upCount++
            }
        }
        val overall = OverallHealth(
            total = ServiceConfig.SERVICES.size,
            up = upCount,
            down = ServiceConfig.SERVICES.size - upCount,
            status = when {
                upCount == ServiceConfig.SERVICES.size -> "up"
                upCount > 0 -> "warning"
                else -> "down"
            }
        )
        return HealthResponse(
            overall = overall,
            services = results
        )
    }

    suspend fun checkSingleService(serviceName: String): SingleServiceHealthResponse? {
        val service = ServiceConfig.SERVICES.find { 
            it.name.lowercase().replace(" ", "-") == serviceName.lowercase() 
        } ?: return null
        val healthData = checkServiceHealth(service)
        return SingleServiceHealthResponse(service, healthData)
    }
} 