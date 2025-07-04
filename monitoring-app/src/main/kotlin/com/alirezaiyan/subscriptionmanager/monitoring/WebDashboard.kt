package com.alirezaiyan.subscriptionmanager.monitoring

object WebDashboard {
    fun createHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subscription Manager - Service Monitor</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }

        .header h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        .header p {
            font-size: 1.1rem;
            opacity: 0.9;
        }

        .status-bar {
            background: rgba(255,255,255,0.1);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            padding: 20px;
            margin-bottom: 30px;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
        }

        .overall-status {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .status-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }

        .status-up { background: #4CAF50; }
        .status-down { background: #f44336; }
        .status-warning { background: #ff9800; }

        @keyframes pulse {
            0% { opacity: 1; }
            50% { opacity: 0.5; }
            100% { opacity: 1; }
        }

        .refresh-btn {
            background: rgba(255,255,255,0.2);
            border: none;
            color: white;
            padding: 10px 20px;
            border-radius: 25px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-size: 14px;
        }

        .refresh-btn:hover {
            background: rgba(255,255,255,0.3);
            transform: translateY(-2px);
        }

        .services-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .service-card {
            background: rgba(255,255,255,0.95);
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }

        .service-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.15);
        }

        .service-card.up {
            border-color: #4CAF50;
        }

        .service-card.down {
            border-color: #f44336;
        }

        .service-card.warning {
            border-color: #ff9800;
        }

        .service-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }

        .service-name {
            font-size: 1.3rem;
            font-weight: 600;
            color: #333;
        }

        .service-status {
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            text-transform: uppercase;
        }

        .service-status.up {
            background: #e8f5e8;
            color: #2e7d32;
        }

        .service-status.down {
            background: #ffebee;
            color: #c62828;
        }

        .service-status.warning {
            background: #fff3e0;
            color: #ef6c00;
        }

        .service-details {
            margin-bottom: 15px;
        }

        .service-url {
            color: #666;
            font-size: 0.9rem;
            margin-bottom: 5px;
        }

        .service-response-time {
            color: #888;
            font-size: 0.8rem;
        }

        .service-components {
            margin-top: 15px;
        }

        .component {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
        }

        .component:last-child {
            border-bottom: none;
        }

        .component-name {
            font-size: 0.9rem;
            color: #555;
        }

        .component-status {
            width: 8px;
            height: 8px;
            border-radius: 50%;
        }

        .logs-section {
            background: rgba(255,255,255,0.95);
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.1);
        }

        .logs-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .logs-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #333;
        }

        .log-entry {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 10px;
            border-left: 4px solid #007bff;
        }

        .log-entry.error {
            border-left-color: #f44336;
            background: #ffebee;
        }

        .log-entry.warning {
            border-left-color: #ff9800;
            background: #fff3e0;
        }

        .log-time {
            color: #666;
            font-size: 0.8rem;
            margin-bottom: 5px;
        }

        .log-message {
            color: #333;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
        }

        .loading {
            text-align: center;
            color: #666;
            padding: 20px;
        }

        .error-message {
            background: #ffebee;
            color: #c62828;
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
            border-left: 4px solid #f44336;
        }

        @media (max-width: 768px) {
            .services-grid {
                grid-template-columns: 1fr;
            }
            
            .status-bar {
                flex-direction: column;
                text-align: center;
            }
        }
    </style>
</head>
<body>
    <div id="root"></div>
    <script>
        let healthData = null;
        let isLoading = true;
        let lastUpdate = "Never";
        let logs = [];

        function addLog(message, type = 'info') {
            const timestamp = new Date().toLocaleTimeString();
            logs.unshift({ timestamp, message, type });
            if (logs.length > 50) logs.pop();
            updateUI();
        }

        function updateUI() {
            const root = document.getElementById('root');
            root.innerHTML = `
                <div style="max-width: 1200px; margin: 0 auto;">
                    <!-- Header -->
                    <div style="text-align: center; color: white; margin-bottom: 30px;">
                        <h1 style="font-size: 2.5rem; margin-bottom: 10px; text-shadow: 2px 2px 4px rgba(0,0,0,0.3);">
                            🔍 Service Monitor
                        </h1>
                        <p style="font-size: 1.1rem; opacity: 0.9;">
                            Subscription Manager - Real-time Health Check Dashboard
                        </p>
                    </div>

                    <!-- Status Bar -->
                    <div class="status-bar">
                        <div class="overall-status">
                            <div class="status-indicator \${healthData?.overall?.status === 'up' ? 'status-up' : 
                                                         healthData?.overall?.status === 'down' ? 'status-down' : 'status-warning'}"></div>
                            <span style="font-size: 16px;">
                                \${healthData ? 
                                    (healthData.overall.up === healthData.overall.total ? 
                                        `All \${healthData.overall.total} services are UP` :
                                        healthData.overall.up === 0 ? 
                                            `All \${healthData.overall.total} services are DOWN` :
                                            `\${healthData.overall.up}/\${healthData.overall.total} services are UP`) :
                                    "Checking services..."}
                            </span>
                        </div>
                        
                        <div>
                            <span style="margin-right: 10px;">Last update: \${lastUpdate}</span>
                            <button class="refresh-btn" onclick="checkAllServices()">
                                🔄 Refresh
                            </button>
                        </div>
                    </div>

                    <!-- Services Grid -->
                    \${isLoading ? 
                        '<div class="loading">Loading services...</div>' :
                        healthData ? 
                            `<div class="services-grid">\${healthData.services.map(serviceHealth => {
                                    const service = serviceHealth.service;
                                    const health = serviceHealth.health;
                                    const statusClass = health.status;
                                    
                                    return `
                                        <div class="service-card \${statusClass}">
                                            <div class="service-header">
                                                <div class="service-name">\${service.name}</div>
                                                <div class="service-status \${statusClass}">\${health.status.toUpperCase()}</div>
                                            </div>
                                            <div class="service-details">
                                                <div class="service-url">\${service.url}</div>
                                                <div class="service-response-time">Response time: \${health.responseTime}ms</div>
                                            </div>
                                            <div class="service-components">
                                                <div class="component">
                                                    <span class="component-name">Status</span>
                                                    <div class="component-status \${statusClass}"></div>
                                                </div>
                                                <div class="component">
                                                    <span class="component-name">Category</span>
                                                    <span>\${service.category}</span>
                                                </div>
                                            </div>
                                        </div>
                                    `;
                                }).join('')}</div>` :
                            '<div class="error-message">Health check failed. Make sure the health checker is running on port 8082.</div>'
                    }

                    <!-- Logs Section -->
                    <div class="logs-section">
                        <div class="logs-header">
                            <div class="logs-title">📋 Recent Activity</div>
                            <button class="refresh-btn" onclick="clearLogs()">
                                Clear Logs
                            </button>
                        </div>
                        <div>
                            \${logs.map(log => `
                                <div class="log-entry \${log.type}">
                                    <div class="log-time">\${log.timestamp}</div>
                                    <div class="log-message">\${log.message}</div>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
            `;
        }

        function clearLogs() {
            logs = [];
            addLog('Logs cleared');
        }

        async function checkAllServices() {
            addLog('Starting health check of all services...');
            isLoading = true;
            updateUI();
            
            try {
                const response = await fetch('/health');
                if (!response.ok) {
                    throw new Error(`Health checker responded with status: \${response.status}`);
                }
                
                healthData = await response.json();
                lastUpdate = new Date().toLocaleTimeString();
                
                // Add logs for each service
                healthData.services.forEach(result => {
                    const status = result.health.status;
                    const responseTime = result.health.responseTime;
                    addLog(`\${result.service.name}: \${status.toUpperCase()} (\${responseTime}ms)`, 
                           status === 'up' ? 'info' : 'error');
                });
                
                addLog(`Health check completed: \${healthData.overall.up}/\${healthData.overall.total} services UP`);
                
            } catch (error) {
                addLog(`Health check failed: \${error.message}`, 'error');
            } finally {
                isLoading = false;
                updateUI();
            }
        }

        // Auto-refresh every 30 seconds
        setInterval(checkAllServices, 30000);

        // Initial check
        checkAllServices();
    </script>
</body>
</html>
        """.trimIndent()
    }
} 