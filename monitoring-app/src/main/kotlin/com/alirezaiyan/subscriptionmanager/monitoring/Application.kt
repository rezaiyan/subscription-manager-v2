package com.alirezaiyan.subscriptionmanager.monitoring

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

fun main() {
    embeddedServer(Netty, port = 8082, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val healthChecker = HealthChecker()

    install(ContentNegotiation) {
        jackson()
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    routing {
        static("/") {
            resources("") // Serve index.html and main.js from resources
        }
        get("/health") {
            val healthData = healthChecker.checkAllServices()
            call.respond(healthData)
        }
        get("/health/{serviceName}") {
            val serviceName = call.parameters["serviceName"]
            if (serviceName != null) {
                val service = ServiceConfig.SERVICES.find { it.name.equals(serviceName, ignoreCase = true) }
                if (service != null) {
                    val healthData = healthChecker.checkServiceHealth(service)
                    val response = SingleServiceHealthResponse(service, healthData)
                    call.respond(response)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Service not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Service name required")
            }
        }
    }
}

 