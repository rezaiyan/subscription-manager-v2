package com.alirezaiyan.subscriptionmanager

import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Composable
@Preview
actual fun App() {
    // Initialize Koin for iOS
    LaunchedEffect(Unit) {
        startKoin {
            modules(
                sharedModule,
                iosModule
            )
        }
    }
    
    MaterialTheme {
        // iOS-specific UI implementation
        // For now, we'll use a simple placeholder
        // In a real app, you'd implement the full subscription management UI
        SubscriptionManagerApp()
    }
}

@Composable
fun SubscriptionManagerApp() {
    // iOS implementation would go here
    // For now, this is a placeholder
} 