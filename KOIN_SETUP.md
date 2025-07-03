# Koin Dependency Injection Setup

This project uses Koin for dependency injection across all platforms in the Kotlin Multiplatform (KMP) project.

## Project Structure

### Shared Module (`shared/src/commonMain/kotlin/com/alirezaiyan/subscriptionmanager/di/`)
- **SharedModule.kt**: Contains common dependencies used across all platforms
  - `ApiService`: HTTP client for communicating with the server
  - `SubscriptionRepository`: Repository for managing subscription data

### Android Module (`composeApp/src/androidMain/kotlin/com/alirezaiyan/subscriptionmanager/di/`)
- **AndroidModule.kt**: Contains Android-specific dependencies
  - `SubscriptionViewModel`: Android ViewModel for managing UI state

### iOS Module (`composeApp/src/iosMain/kotlin/com/alirezaiyan/subscriptionmanager/di/`)
- **IosModule.kt**: Contains iOS-specific dependencies (placeholder for now)

## Dependencies

### Shared Dependencies
```kotlin
// In shared/build.gradle.kts
implementation(libs.koin.core)
```

### Android Dependencies
```kotlin
// In composeApp/build.gradle.kts
implementation(libs.koin.android)
implementation(libs.koin.compose)
```

## Usage

### Android
1. **Application Setup**: `KoinApplication.kt` initializes Koin with shared and Android modules
2. **ViewModel Injection**: Use `koinViewModel()` in Compose functions
3. **Lifecycle Management**: `ApiServiceLifecycle.kt` handles cleanup when the app is destroyed

### iOS
1. **Koin Initialization**: Initialize Koin in the `App()` composable using `LaunchedEffect`
2. **Dependency Injection**: Use `get()` or `inject()` to retrieve dependencies

### Shared Code
- All shared dependencies are available across platforms
- Use `get()` to retrieve dependencies in shared code

## Example Usage

### Android ViewModel Injection
```kotlin
@Composable
fun MyScreen() {
    val viewModel: SubscriptionViewModel = koinViewModel()
    // Use viewModel...
}
```

### Shared Service Injection
```kotlin
class MyService {
    private val apiService: ApiService by inject()
    private val repository: SubscriptionRepository by inject()
    
    // Use injected dependencies...
}
```

### Manual Dependency Retrieval
```kotlin
val apiService = get<ApiService>()
val repository = get<SubscriptionRepository>()
```

## Benefits

1. **Platform Agnostic**: Same DI framework across all platforms
2. **Lightweight**: Koin is lightweight and doesn't require code generation
3. **Type Safe**: Compile-time dependency resolution
4. **Easy Testing**: Easy to mock dependencies for testing
5. **Lifecycle Aware**: Proper lifecycle management for Android

## Adding New Dependencies

1. **Shared Dependencies**: Add to `SharedModule.kt`
2. **Platform-Specific Dependencies**: Add to respective platform modules
3. **Update Dependencies**: Add any new Koin dependencies to `libs.versions.toml`

## Testing

Koin provides excellent testing support:
- Use `koinTest` for testing
- Mock dependencies easily
- Test modules independently 