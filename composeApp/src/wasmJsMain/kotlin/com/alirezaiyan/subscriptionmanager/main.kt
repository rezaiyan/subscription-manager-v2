package com.alirezaiyan.subscriptionmanager

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import com.alirezaiyan.subscriptionmanager.monitoring.MonitoringDashboard

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        ComposeViewport(document.body!!) {
            val path = window.location.pathname
            when {
                path == "/monitoring" -> MonitoringDashboard()
                else -> App()
            }
        }
    } catch (e: Exception) {
        // Handle error silently for WASM
    }
}