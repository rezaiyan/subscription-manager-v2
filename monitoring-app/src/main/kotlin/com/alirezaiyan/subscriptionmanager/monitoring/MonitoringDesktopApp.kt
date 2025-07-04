package com.alirezaiyan.subscriptionmanager.monitoring

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.delay

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Subscription Manager - Service Monitor",
        state = rememberWindowState()
    ) {
        MonitoringDashboard()
    }
}

// Update the checkAllServices function to make actual HTTP calls
private suspend fun checkAllServices(onResult: (HealthData?) -> Unit) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
    try {
        val response = client.get("http://localhost:8082/health")
        if (response.status.value in 200..299) {
            val healthData = response.body<HealthData>()
            onResult(healthData)
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    } finally {
        client.close()
    }
} 