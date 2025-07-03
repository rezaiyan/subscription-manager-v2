package com.alirezaiyan.subscriptionmanager

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
actual fun App() {
    val viewModel: SubscriptionViewModel = org.koin.java.KoinJavaComponent.get(SubscriptionViewModel::class.java)
    val context = LocalContext.current
    val networkMonitor: NetworkMonitor = remember { NetworkMonitor(context) }
    var showNetworkInfo by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        println("🎨 App composable launched")
        val isNetworkAvailable = networkMonitor.isNetworkAvailable()
        val networkType = networkMonitor.getNetworkType()
        println("📡 App UI - Network available: $isNetworkAvailable, Type: $networkType")
    }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Network status bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (networkMonitor.isNetworkAvailable()) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (networkMonitor.isNetworkAvailable()) "🌐 Connected" else "❌ No Network",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Type: ${networkMonitor.getNetworkType()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = { showNetworkInfo = !showNetworkInfo }
                        ) {
                            Text(if (showNetworkInfo) "Hide" else "Debug")
                        }
                    }
                    if (showNetworkInfo) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🔧 Network Debug Info:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("• Target Server: 192.168.178.100:3000")
                            Text("• Network Available: ${networkMonitor.isNetworkAvailable()}")
                            Text("• Network Type: ${networkMonitor.getNetworkType()}")
                            Text("• App Package: ${context.packageName}")
                        }
                    }
                }
                // Main content
                SubscriptionListScreen(
                    subscriptions = viewModel.subscriptions.collectAsState().value,
                    totals = viewModel.totals.collectAsState().value,
                    isLoading = viewModel.isLoading.collectAsState().value,
                    error = viewModel.error.collectAsState().value,
                    onRefresh = { viewModel.loadSubscriptions() },
                    onToggleActive = { id -> viewModel.toggleSubscriptionActive(id) },
                    onDelete = { id -> viewModel.deleteSubscription(id) },
                    onAddNew = { showAddDialog = true }
                )
                if (showAddDialog) {
                    AddSubscriptionDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name, description, amount, frequency ->
                            viewModel.createSubscription(name, description, amount, frequency)
                            showAddDialog = false
                        }
                    )
                }
            }
        }
    }
} 