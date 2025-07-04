package com.alirezaiyan.subscriptionmanager.monitoring

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringDashboard() {
    var healthData by remember { mutableStateOf<HealthData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var lastUpdate by remember { mutableStateOf("Never") }
    var logs by remember { mutableStateOf(listOf<LogEntry>()) }
    val scope = rememberCoroutineScope()

    // Auto-refresh every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            scope.launch {
                checkAllServices { data ->
                    healthData = data
                    lastUpdate = getCurrentTime()
                    isLoading = false
                    
                    // Add logs for each service
                    data?.services?.forEach { result ->
                        val status = result.health.status
                        val responseTime = result.health.responseTime
                        logs = logs.take(49) + LogEntry(
                            timestamp = getCurrentTime(),
                            message = "${result.service.name}: ${status.uppercase()} (${responseTime}ms)",
                            type = if (status == "up") "info" else "error"
                        )
                    }
                    
                    data?.let {
                        logs = logs.take(49) + LogEntry(
                            timestamp = getCurrentTime(),
                            message = "Health check completed: ${it.overall.up}/${it.overall.total} services UP",
                            type = "info"
                        )
                    }
                }
            }
            delay(30000)
        }
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
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                "🔍 Service Monitor",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        actions = {
                            IconButton(
                                onClick = { 
                                    navigateToMainApp()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, 
                                    contentDescription = "Back to Main App",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Bar
                    item {
                        StatusBar(
                            healthData = healthData,
                            lastUpdate = lastUpdate,
                            onRefresh = {
                                scope.launch {
                                    isLoading = true
                                    checkAllServices { data ->
                                        healthData = data
                                        lastUpdate = getCurrentTime()
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }

                    // Services Grid
                    if (isLoading) {
                        item {
                            LoadingCard()
                        }
                    } else if (healthData != null) {
                        items(healthData!!.services) { serviceHealth ->
                            ServiceCard(serviceHealth = serviceHealth)
                        }
                    } else {
                        item {
                            ErrorCard(message = "Health check failed. Make sure the health checker is running on port 8082.")
                        }
                    }

                    // Logs Section
                    item {
                        LogsSection(
                            logs = logs,
                            onClearLogs = {
                                logs = emptyList()
                                logs = listOf(LogEntry(
                                    timestamp = getCurrentTime(),
                                    message = "Logs cleared",
                                    type = "info"
                                ))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBar(
    healthData: HealthData?,
    lastUpdate: String,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusIndicator(status = healthData?.overall?.status ?: "unknown")
                Column {
                    Text(
                        text = healthData?.let {
                            when {
                                it.overall.up == it.overall.total -> "All ${it.overall.total} services are UP"
                                it.overall.up == 0 -> "All ${it.overall.total} services are DOWN"
                                else -> "${it.overall.up}/${it.overall.total} services are UP"
                            }
                        } ?: "Checking services...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Last update: $lastUpdate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(
                onClick = onRefresh,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

@Composable
fun StatusIndicator(status: String) {
    val color = when (status) {
        "up" -> Color(0xFF4CAF50)
        "down" -> Color(0xFFf44336)
        else -> Color(0xFFff9800)
    }
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(6.dp))
    )
}

@Composable
fun ServiceCard(serviceHealth: ServiceHealthResponse) {
    val service = serviceHealth.service
    val health = serviceHealth.health
    val statusColor = when (health.status) {
        "up" -> Color(0xFF4CAF50)
        "down" -> Color(0xFFf44336)
        else -> Color(0xFFff9800)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Service Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = health.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Service Details
            Text(
                text = service.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Response time: ${health.responseTime}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Service Components
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusIndicator(status = health.status)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = service.category,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "up" -> Color(0xFFe8f5e8) to Color(0xFF2e7d32)
        "down" -> Color(0xFFffebee) to Color(0xFFc62828)
        else -> Color(0xFFfff3e0) to Color(0xFFef6c00)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading services...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LogsSection(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Logs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearLogs) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Logs")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logs List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogEntryCard(log = log)
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(log: LogEntry) {
    val borderColor = when (log.type) {
        "error" -> Color(0xFFf44336)
        "warning" -> Color(0xFFff9800)
        else -> Color(0xFF007bff)
    }
    
    val backgroundColor = when (log.type) {
        "error" -> Color(0xFFffebee)
        "warning" -> Color(0xFFfff3e0)
        else -> Color(0xFFf8f9fa)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: String
)

@JsFun("() => { window.location.href = '/' }")
external fun navigateToMainApp()

@JsFun("() => new Date().toLocaleTimeString()")
external fun getCurrentTime(): String

private fun checkAllServices(onResult: (HealthData?) -> Unit) {
    try {
        // This would be replaced with actual HTTP call to the health endpoint
        // For now, we'll simulate the call
        onResult(null)
    } catch (e: Exception) {
        onResult(null)
    }
} 