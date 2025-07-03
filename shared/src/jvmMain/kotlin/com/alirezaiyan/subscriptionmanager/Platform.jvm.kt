package com.alirezaiyan.subscriptionmanager

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual class Platform actual constructor() {
    actual val platform: String = "JVM ${System.getProperty("java.version")}"
}

actual fun createHttpClient(): HttpClient {
    return HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }
    }
}