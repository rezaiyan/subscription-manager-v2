package com.alirezaiyan.subscriptionmanager.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.alirezaiyan.subscriptionmanager.ApiService
import com.alirezaiyan.subscriptionmanager.SubscriptionRepository
import com.alirezaiyan.subscriptionmanager.SubscriptionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { NetworkMonitor(androidContext()) }
    // Use shared ApiService, SubscriptionRepository, and SubscriptionViewModel
    single { ApiService() }
    single { SubscriptionRepository(get()) }
    single { SubscriptionViewModel(get()) }
}

class NetworkMonitor(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isAvailable = capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
        Log.d("NetworkDebug", "🌐 Network available: $isAvailable")
        return isAvailable
    }
    
    fun getNetworkType(): String {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Unknown"
        }.also { Log.d("NetworkDebug", "📡 Network type: $it") }
    }
    
    fun registerNetworkCallback(callback: (Boolean) -> Unit) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NetworkDebug", "✅ Network became available")
                callback(true)
            }
            
            override fun onLost(network: Network) {
                Log.d("NetworkDebug", "❌ Network was lost")
                callback(false)
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
} 