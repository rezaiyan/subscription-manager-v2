package com.alirezaiyan.subscriptionmanager

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiService {
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
            logger = Logger.DEFAULT
            filter { request ->
                request.url.host.contains("192.168.178.100")
            }
        }
    }

    private val baseUrl = "http://192.168.178.100:3000/api"

    init {
        println("🔗 ApiService initialized with baseUrl: $baseUrl")
    }

    suspend fun getAllSubscriptions(): List<Subscription> {
        val url = "$baseUrl/subscriptions"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get(url)
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getActiveSubscriptions(): List<Subscription> {
        val url = "$baseUrl/subscriptions/active"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get(url)
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        val url = "$baseUrl/subscriptions/$id"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get(url)
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            null
        }
    }

    suspend fun searchSubscriptions(name: String): List<Subscription> {
        val url = "$baseUrl/subscriptions/search?name=$name"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get("$baseUrl/subscriptions/search") {
                parameter("name", name)
            }
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getSubscriptionsByFrequency(frequency: SubscriptionFrequency): List<Subscription> {
        val url = "$baseUrl/subscriptions/by-frequency/$frequency"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get(url)
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getSubscriptionTotals(): SubscriptionTotals {
        val url = "$baseUrl/subscriptions/totals"
        println("📡 Making GET request to: $url")
        
        return try {
            val response = client.get(url)
            println("✅ GET $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ GET $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun createSubscription(subscription: Subscription): Subscription {
        val url = "$baseUrl/subscriptions"
        println("📡 Making POST request to: $url")
        println("📦 Request body: $subscription")
        
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(subscription)
            }
            println("✅ POST $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ POST $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateSubscription(id: Long, subscription: Subscription): Subscription? {
        val url = "$baseUrl/subscriptions/$id"
        println("📡 Making PUT request to: $url")
        println("📦 Request body: $subscription")
        
        return try {
            val response = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(subscription)
            }
            println("✅ PUT $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ PUT $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            null
        }
    }

    suspend fun toggleSubscriptionActive(id: Long): Subscription? {
        val url = "$baseUrl/subscriptions/$id/toggle-active"
        println("📡 Making PATCH request to: $url")
        
        return try {
            val response = client.patch(url)
            println("✅ PATCH $url - Success (${response.status})")
            response.body()
        } catch (e: Exception) {
            println("❌ PATCH $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            null
        }
    }

    suspend fun deleteSubscription(id: Long): Boolean {
        val url = "$baseUrl/subscriptions/$id"
        println("📡 Making DELETE request to: $url")
        
        return try {
            client.delete(url)
            println("✅ DELETE $url - Success")
            true
        } catch (e: Exception) {
            println("❌ DELETE $url - Failed: ${e.message}")
            println("🔍 Error details: ${e.javaClass.simpleName}")
            false
        }
    }

    fun close() {
        println("🔌 Closing ApiService HTTP client")
        client.close()
    }
} 