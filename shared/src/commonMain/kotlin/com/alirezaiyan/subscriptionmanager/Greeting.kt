package com.alirezaiyan.subscriptionmanager

class Greeting {
    private val platform = Platform()

    fun greet(): String {
        return "Hello, ${platform.platform}!"
    }
}