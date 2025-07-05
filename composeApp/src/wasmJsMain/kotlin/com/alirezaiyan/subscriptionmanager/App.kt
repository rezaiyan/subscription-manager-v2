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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.alirezaiyan.subscriptionmanager.di.webModule
import com.alirezaiyan.subscriptionmanager.monitoring.MonitoringDashboard
import org.koin.core.context.startKoin
import kotlinx.browser.window
import org.w3c.dom.events.Event

enum class Screen(val path: String) {
    MAIN("/"),
    MONITORING("/monitoring");

    companion object {
        fun fromPath(path: String): Screen = entries.firstOrNull { it.path == path } ?: MAIN
    }
}

@Composable
actual fun App() {
    var isLoading by remember { mutableStateOf(true) }
    var viewModel by remember { mutableStateOf<SubscriptionViewModel?>(null) }
    var currentScreen by remember { mutableStateOf(Screen.fromPath(window.location.pathname)) }

    // Initialize Koin and ViewModel
    LaunchedEffect(Unit) {
        println("🚀 WASM App: Starting Koin initialization")
        startKoin {
            modules(webModule)
        }
        println("✅ WASM App: Koin initialized successfully")

        viewModel = org.koin.core.context.GlobalContext.get().get<SubscriptionViewModel>()
        println("✅ WASM App: ViewModel retrieved successfully")

        viewModel?.loadSubscriptions()
        println("🔄 WASM App: Loading subscriptions...")

        kotlinx.coroutines.delay(1000)
        isLoading = false
        println("✅ WASM App: Loading screen completed")
    }

    // Listen for browser navigation changes
    DisposableEffect(Unit) {
        val popStateListener: (Event) -> Unit = {
            currentScreen = Screen.fromPath(window.location.pathname)
        }
        window.addEventListener("popstate", popStateListener)
        onDispose {
            window.removeEventListener("popstate", popStateListener)
        }
    }

    if (isLoading || viewModel == null) {
        LoadingScreen()
    } else {
        when (currentScreen) {
            Screen.MAIN -> MainApp(
                viewModel = viewModel!!,
                onNavigateToMonitoring = {
                    window.history.pushState(null, "", "/monitoring")
                    window.dispatchEvent(Event("popstate"))
                }
            )

            Screen.MONITORING -> MonitoringDashboard(
                onBackToMain = {
                    window.history.back()
                }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Text(
                text = "Subscription Manager",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun MainApp(viewModel: SubscriptionViewModel, onNavigateToMonitoring: () -> Unit) {
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
                onOpenMonitoring = onNavigateToMonitoring
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