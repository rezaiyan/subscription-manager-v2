package com.alirezaiyan.subscriptionmanager

import io.ktor.client.HttpClient

expect class Platform() {
    val platform: String
}

expect fun createHttpClient(): HttpClient