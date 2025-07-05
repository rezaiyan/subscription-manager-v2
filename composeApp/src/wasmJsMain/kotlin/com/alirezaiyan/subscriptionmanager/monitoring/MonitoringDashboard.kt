package com.alirezaiyan.subscriptionmanager.monitoring

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.w3c.fetch.Response
import kotlinx.browser.window

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringDashboard(onBackToMain: () -> Unit = {}) {
    println("🔍 MonitoringDashboard: Starting to render")
    
    LaunchedEffect(Unit) {
        println("🔍 MonitoringDashboard: LaunchedEffect triggered")
    }
    var healthData by remember { mutableStateOf<HealthData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var lastUpdate by remember { mutableStateOf("Never") }
    var logs by remember { 
        mutableStateOf(
            listOf(
                LogEntry(
                    timestamp = getCurrentTime(),
                    message = "🚀 Monitoring dashboard initialized",
                    type = "info"
                ),
                LogEntry(
                    timestamp = getCurrentTime(),
                    message = "📊 Starting health check cycle",
                    type = "info"
                )
            )
        ) 
    }
    val scope = rememberCoroutineScope()

    // Auto-refresh every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            checkAllServices { data ->
                healthData = data
                lastUpdate = getCurrentTime()
                isLoading = false
                
                // Add logs for each service
                data?.services?.forEach { result ->
                    val status = result.health.status
                    val responseTime = result.health.responseTime
                    val emoji = if (status == "up") "✅" else "❌"
                    logs = logs.take(49) + LogEntry(
                        timestamp = getCurrentTime(),
                        message = "$emoji ${result.service.name}: ${status.uppercase()} (${responseTime}ms)",
                        type = if (status == "up") "info" else "error"
                    )
                }
                
                data?.let {
                    val upCount = it.overall.up
                    val totalCount = it.overall.total
                    val emoji = if (upCount == totalCount) "🎉" else if (upCount == 0) "💥" else "⚠️"
                    logs = logs.take(49) + LogEntry(
                        timestamp = getCurrentTime(),
                        message = "$emoji Health check completed: $upCount/$totalCount services UP",
                        type = "info"
                    )
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
                            Color(0xFF1a237e),
                            Color(0xFF0d47a1),
                            Color(0xFF1565c0)
                        )
                    )
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    "Service Monitor",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        actions = {
                            IconButton(
                                onClick = onBackToMain
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Overview Stats
                    item {
                        OverviewStatsCard(healthData = healthData, lastUpdate = lastUpdate)
                    }

                    // Services Grid
                    if (isLoading) {
                        item {
                            LoadingCard()
                        }
                    } else if (healthData != null) {
                        item {
                            Text(
                                text = "🔧 Service Status",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(healthData!!.services) { serviceHealth ->
                            ServiceStatusCard(serviceHealth = serviceHealth)
                        }
                    } else {
                        item {
                            ErrorCard(message = "Unable to load service health data. This is a demo dashboard showing mock data.")
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
                                    message = "🧹 Logs cleared",
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
fun OverviewStatsCard(healthData: HealthData?, lastUpdate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a237e)
                    )
                    Text(
                        text = "Last updated: $lastUpdate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
                
                healthData?.let { data ->
                    val status = data.overall.status
                    val upCount = data.overall.up
                    val totalCount = data.overall.total
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusIndicator(status = status, size = 16.dp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$upCount/$totalCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (status == "up") Color(0xFF2e7d32) else Color(0xFFc62828)
                            )
                            Text(
                                text = "Services UP",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress bar
            healthData?.let { data ->
                val progress = data.overall.total.toFloat() / data.overall.total.toFloat()
                val upProgress = data.overall.up.toFloat() / data.overall.total.toFloat()
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Health Score",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(upProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (upProgress >= 0.8f) Color(0xFF2e7d32) else Color(0xFFc62828)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = upProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (upProgress >= 0.8f) Color(0xFF4caf50) else Color(0xFFf44336),
                        trackColor = Color(0xFFe0e0e0)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceStatusCard(serviceHealth: ServiceHealthResponse) {
    val service = serviceHealth.service
    val health = serviceHealth.health
    val statusColor = when (health.status) {
        "up" -> Color(0xFF4caf50)
        "down" -> Color(0xFFf44336)
        else -> Color(0xFFff9800)
    }
    
    val backgroundColor = when (health.status) {
        "up" -> Color(0xFFe8f5e8)
        "down" -> Color(0xFFffebee)
        else -> Color(0xFFfff3e0)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        backgroundColor,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (health.status) {
                        "up" -> Icons.Default.CheckCircle
                        "down" -> Icons.Default.Error
                        else -> Icons.Default.Warning
                    },
                    contentDescription = health.status,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Service details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1a237e)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = service.url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusBadge(status = health.status)
                    Text(
                        text = "${health.responseTime}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = service.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        modifier = Modifier
                            .background(
                                Color(0xFFf5f5f5),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Status indicator
            StatusIndicator(status = health.status, size = 12.dp)
        }
    }
}

@Composable
fun StatusIndicator(status: String, size: Dp = 12.dp) {
    val color = when (status) {
        "up" -> Color(0xFF4caf50)
        "down" -> Color(0xFFf44336)
        else -> Color(0xFFff9800)
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .background(color, RoundedCornerShape(size / 2))
    )
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
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color(0xFF1a237e)
            )
            Text(
                text = "Loading services...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1a237e)
            )
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFffebee)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                tint = Color(0xFFc62828),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                color = Color(0xFFc62828),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
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
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Logs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        tint = Color(0xFF1a237e),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Activity Log",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a237e)
                    )
                }
                TextButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF1a237e)
                    )
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logs List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(logs.reversed()) { log ->
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
        else -> Color(0xFF4caf50)
    }
    
    val backgroundColor = when (log.type) {
        "error" -> Color(0xFFffebee)
        "warning" -> Color(0xFFfff3e0)
        else -> Color(0xFFe8f5e8)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (log.type) {
                    "error" -> Icons.Default.Error
                    "warning" -> Icons.Default.Warning
                    else -> Icons.Default.Info
                },
                contentDescription = log.type,
                tint = borderColor,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: String
)

fun getCurrentTime(): String {
    return Clock.System.now().toString()
}

private suspend fun checkAllServices(onResult: (HealthData?) -> Unit) {
    try {
        val services = listOf(
            Service(
                name = "API Gateway",
                url = "http://localhost:8080",
                category = "Gateway"
            ),
            Service(
                name = "Eureka Server",
                url = "http://localhost:8761",
                category = "Service Discovery"
            ),
            Service(
                name = "Config Server",
                url = "http://localhost:8888",
                category = "Configuration"
            ),
            Service(
                name = "Main Service",
                url = "http://localhost:8081",
                category = "Business Logic"
            ),
            Service(
                name = "Create Subscription Service",
                url = "http://localhost:8082",
                category = "Business Logic"
            )
        )
        
        val serviceHealthResponses = mutableListOf<ServiceHealthResponse>()
        var upCount = 0
        
        services.forEach { service ->
            val health = checkServiceHealth(service.url)
            serviceHealthResponses.add(
                ServiceHealthResponse(
                    service = service,
                    health = health
                )
            )
            if (health.status == "up") {
                upCount++
            }
        }
        
        val overallStatus = if (upCount == services.size) "up" else "down"
        
        val healthData = HealthData(
            overall = OverallHealth(
                status = overallStatus,
                up = upCount,
                total = services.size
            ),
            services = serviceHealthResponses
        )
        
        onResult(healthData)
    } catch (e: Exception) {
        println("❌ Error checking services: ${e.message}")
        onResult(null)
    }
}

private suspend fun checkServiceHealth(url: String): Health {
    return try {
        val startTime = Clock.System.now()
        val response = window.fetch(url).await() as Response
        val endTime = Clock.System.now()
        val responseTime = (endTime - startTime).inWholeMilliseconds

        if (response.ok) {
            Health(status = "up", responseTime = responseTime)
        } else {
            Health(status = "down", responseTime = responseTime)
        }
    } catch (e: Exception) {
        println("❌ Service $url is down: ${e.message}")
        Health(status = "down", responseTime = 0)
    }
} 