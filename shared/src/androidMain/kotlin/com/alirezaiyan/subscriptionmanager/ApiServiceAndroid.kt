package com.alirezaiyan.subscriptionmanager

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiServiceAndroid : ApiService {
    private val client = createHttpClient()

    // Use the local network IP address instead of localhost
    // This allows Android devices to connect to the server running on the development machine
    private val baseUrl = "http://192.168.178.100:3000/api"

    init {
        println("🔗 Android ApiService initialized with baseUrl: $baseUrl")
    }

    override suspend fun getAllSubscriptions(): List<Subscription> {
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

    override suspend fun getActiveSubscriptions(): List<Subscription> {
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

    override suspend fun getSubscriptionById(id: Long): Subscription? {
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

    override suspend fun searchSubscriptions(name: String): List<Subscription> {
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

    override suspend fun getSubscriptionsByFrequency(frequency: SubscriptionFrequency): List<Subscription> {
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

    override suspend fun getSubscriptionTotals(): SubscriptionTotals {
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

    override suspend fun createSubscription(subscription: Subscription): Subscription {
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

    override suspend fun updateSubscription(id: Long, subscription: Subscription): Subscription? {
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

    override suspend fun toggleSubscriptionActive(id: Long): Subscription? {
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

    override suspend fun deleteSubscription(id: Long): Boolean {
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

    override fun close() {
        println("🔌 Closing ApiService HTTP client")
        client.close()
    }
} 