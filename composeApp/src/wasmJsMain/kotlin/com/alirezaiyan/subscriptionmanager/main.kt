package com.alirezaiyan.subscriptionmanager

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import com.alirezaiyan.subscriptionmanager.monitoring.MonitoringDashboard
import com.alirezaiyan.subscriptionmanager.di.webModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        ComposeViewport(document.body!!) {
            var showMonitoring by remember { mutableStateOf(false) }
            
            if (showMonitoring) {
                MonitoringDashboard(
                    onBackToMain = {
                        showMonitoring = false
                    }
                )
            } else {
                MainAppWithMonitoring(onOpenMonitoring = {
                    showMonitoring = true
                })
            }
        }
    } catch (e: Exception) {
        // Handle error silently for WASM
//        console.error("WASM Error:", e)
    }
}

@Composable
private fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.size(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Color(0xFF667eea)
                )
                Text(
                    text = "Loading Subscription Manager...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun MainAppWithMonitoring(onOpenMonitoring: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var viewModel by remember { mutableStateOf<SubscriptionViewModel?>(null) }

    // Initialize Koin for WASM
    LaunchedEffect(Unit) {
        println("🚀 WASM App: Starting Koin initialization")
        println("📦 WASM App: Loading webModule: $webModule")

        // Create inline module as fallback
        startKoin {
            modules(webModule)
        }
        println("✅ WASM App: Koin initialized successfully")

        // Get the ViewModel instance
        viewModel = org.koin.core.context.GlobalContext.get().get<SubscriptionViewModel>()
        println("✅ WASM App: ViewModel retrieved successfully")

        // Load initial data
        viewModel?.loadSubscriptions()
        println("🔄 WASM App: Loading subscriptions...")

        // Simulate loading time
        isLoading = false
        println("✅ WASM App: Loading screen completed")
    }

    if (isLoading || viewModel == null) {
        LoadingScreen()
    } else {
        MainAppContent(viewModel!!, onOpenMonitoring)
    }
}

@Composable
private fun MainAppContent(viewModel: SubscriptionViewModel, onOpenMonitoring: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        println("🎨 WASM App: MainApp composable launched")
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                )
        ) {
            SubscriptionListScreen(
                subscriptions = viewModel.subscriptions.collectAsState().value,
                totals = viewModel.totals.collectAsState().value,
                isLoading = viewModel.isLoading.collectAsState().value,
                error = viewModel.error.collectAsState().value,
                onRefresh = {
                    println("🔄 WASM App: Manual refresh triggered")
                    viewModel.loadSubscriptions()
                },
                onToggleActive = { id -> viewModel.toggleSubscriptionActive(id) },
                onDelete = { id -> viewModel.deleteSubscription(id) },
                onAddNew = { showAddDialog = true },
                onOpenMonitoring = onOpenMonitoring
            )

            // Add Subscription Dialog
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