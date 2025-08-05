package com.alirezaiyan.subscriptionmanager

import android.app.Application
import com.alirezaiyan.subscriptionmanager.di.NetworkMonitor
import com.alirezaiyan.subscriptionmanager.di.androidModule
import com.alirezaiyan.subscriptionmanager.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.get

class SubscriptionManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        println("🚀 SubscriptionManagerApp onCreate")
        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@SubscriptionManagerApp)
            modules(listOf(sharedModule, androidModule))
        }
        // Now it's safe to use Koin
        val networkMonitor: NetworkMonitor = get(NetworkMonitor::class.java)
        val isNetworkAvailable = networkMonitor.isNetworkAvailable()
        val networkType = networkMonitor.getNetworkType()
        println("📡 App startup - Network available: $isNetworkAvailable, Type: $networkType")
        networkMonitor.registerNetworkCallback { isAvailable ->
            println("📡 Network status changed - Available: $isAvailable")
        }
    }
}