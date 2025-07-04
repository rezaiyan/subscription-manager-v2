package com.alirezaiyan.subscriptionmanager.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                    lastUpdate = java.time.LocalTime.now().toString().substring(0, 8)
                    isLoading = false
                    
                    // Add logs for each service
                    data?.services?.forEach { result ->
                        val status = result.health.status
                        val responseTime = result.health.responseTime
                        logs = logs.take(49) + LogEntry(
                            timestamp = java.time.LocalTime.now().toString().substring(0, 8),
                            message = "${result.service.name}: ${status.uppercase()} (${responseTime}ms)",
                            type = if (status == "up") "info" else "error"
                        )
                    }
                    
                    data?.let {
                        logs = logs.take(49) + LogEntry(
                            timestamp = java.time.LocalTime.now().toString().substring(0, 8),
                            message = "Health check completed: ${it.overall.up}/${it.overall.total} services UP",
                            type = "info"
                        )
                    }
                }
            }
            delay(30000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                )
            )
            .padding(20.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔍 Service Monitor",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Subscription Manager - Real-time Health Check Dashboard",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Status Bar
        StatusBar(
            healthData = healthData,
            lastUpdate = lastUpdate,
            onRefresh = {
                scope.launch {
                    isLoading = true
                    checkAllServices { data ->
                        healthData = data
                        lastUpdate = java.time.LocalTime.now().toString().substring(0, 8)
                        isLoading = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Services Grid
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading services...", color = Color.White)
            }
        } else if (healthData != null) {
            ServicesGrid(services = healthData!!.services)
        } else {
            ErrorCard(message = "Health check failed. Make sure the health checker is running on port 8083.")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Logs Section
        LogsSection(
            logs = logs,
            onClearLogs = {
                logs = emptyList()
                logs = listOf(LogEntry(
                    timestamp = java.time.LocalTime.now().toString().substring(0, 8),
                    message = "Logs cleared",
                    type = "info"
                ))
            }
        )
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
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(15.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusIndicator(
                    status = healthData?.overall?.status ?: "unknown"
                )
                Text(
                    text = healthData?.let {
                        when {
                            it.overall.up == it.overall.total -> "All ${it.overall.total} services are UP"
                            it.overall.up == 0 -> "All ${it.overall.total} services are DOWN"
                            else -> "${it.overall.up}/${it.overall.total} services are UP"
                        }
                    } ?: "Checking services...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Last update: $lastUpdate",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("🔄 Refresh", color = Color.White)
                }
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
fun ServicesGrid(services: List<ServiceHealthResponse>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(services) { serviceHealth ->
            ServiceCard(serviceHealth = serviceHealth)
        }
    }
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
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, statusColor, RoundedCornerShape(15.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(25.dp)
        ) {
            // Service Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                StatusBadge(status = health.status)
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Service Details
            Text(
                text = service.url,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Response time: ${health.responseTime}ms",
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Service Components
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
                StatusIndicator(status = health.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = service.category,
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
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
    
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFffebee)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 20.dp)
                    .background(Color(0xFFf44336), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFc62828),
                fontSize = 14.sp
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
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(25.dp)
        ) {
            // Logs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                Button(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Clear Logs", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logs List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(4.dp, borderColor, RoundedCornerShape(8.dp), start = true),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = log.timestamp,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = log.message,
                fontSize = 14.sp,
                color = Color(0xFF333333),
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

private suspend fun checkAllServices(onResult: (HealthData?) -> Unit) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
    try {
        val response = client.get("http://localhost:8082/health")
        if (response.status.value in 200..299) {
            val healthData = response.body<HealthData>()
            onResult(healthData)
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    } finally {
        client.close()
    }
} 