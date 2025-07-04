import androidx.compose.runtime.Composable
import androidx.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        MonitoringDashboard()
    }
} 