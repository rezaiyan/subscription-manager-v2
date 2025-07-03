package com.alirezaiyan.subscriptionmanager

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiServiceWasm : ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    private val baseUrl = "http://localhost:3000"

    init {
        println("🔗 WASM ApiService initialized with baseUrl: $baseUrl")
    }

    override suspend fun getAllSubscriptions(): List<Subscription> {
        return try {
            client.get("$baseUrl/api/subscriptions").body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions - Failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getActiveSubscriptions(): List<Subscription> {
        return try {
            client.get("$baseUrl/api/subscriptions/active").body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions/active - Failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSubscriptionById(id: Long): Subscription? {
        return try {
            client.get("$baseUrl/api/subscriptions/$id").body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions/$id - Failed: ${e.message}")
            null
        }
    }

    override suspend fun searchSubscriptions(name: String): List<Subscription> {
        return try {
            client.get("$baseUrl/api/subscriptions/search") {
                parameter("name", name)
            }.body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions/search?name=$name - Failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSubscriptionsByFrequency(frequency: SubscriptionFrequency): List<Subscription> {
        return try {
            client.get("$baseUrl/api/subscriptions/frequency/${frequency.name.lowercase()}").body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions/frequency/${frequency.name.lowercase()} - Failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSubscriptionTotals(): SubscriptionTotals {
        return try {
            client.get("$baseUrl/api/subscriptions/totals").body()
        } catch (e: Exception) {
            println("❌ WASM: GET /api/subscriptions/totals - Failed: ${e.message}")
            e.printStackTrace()
            SubscriptionTotals(0.0, 0.0)
        }
    }

    override suspend fun createSubscription(subscription: Subscription): Subscription {
        return try {
            client.post("$baseUrl/api/subscriptions") {
                contentType(ContentType.Application.Json)
                setBody(subscription)
            }.body()
        } catch (e: Exception) {
            println("❌ WASM: POST /api/subscriptions - Failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun updateSubscription(id: Long, subscription: Subscription): Subscription? {
        return try {
            client.put("$baseUrl/api/subscriptions/$id") {
                contentType(ContentType.Application.Json)
                setBody(subscription)
            }.body()
        } catch (e: Exception) {
            println("❌ WASM: PUT /api/subscriptions/$id - Failed: ${e.message}")
            null
        }
    }

    override suspend fun toggleSubscriptionActive(id: Long): Subscription? {
        return try {
            client.patch("$baseUrl/api/subscriptions/$id/toggle").body()
        } catch (e: Exception) {
            println("❌ WASM: PATCH /api/subscriptions/$id/toggle - Failed: ${e.message}")
            null
        }
    }

    override suspend fun deleteSubscription(id: Long): Boolean {
        return try {
            client.delete("$baseUrl/api/subscriptions/$id").status.isSuccess()
        } catch (e: Exception) {
            println("❌ WASM: DELETE /api/subscriptions/$id - Failed: ${e.message}")
            false
        }
    }

    override fun close() {
        println("🔌 WASM: Closing ApiService HTTP client")
        client.close()
    }
} 