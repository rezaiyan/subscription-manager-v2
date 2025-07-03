package com.alirezaiyan.subscriptionmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform