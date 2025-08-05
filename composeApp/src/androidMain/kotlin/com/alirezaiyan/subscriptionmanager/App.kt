package com.alirezaiyan.subscriptionmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alirezaiyan.subscriptionmanager.di.NetworkMonitor
import org.koin.java.KoinJavaComponent.get

@Composable
actual fun App() {
    val viewModel = get<SubscriptionViewModel>(SubscriptionViewModel::class.java)
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
                            Text("• Target Server: localhost:3000")
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