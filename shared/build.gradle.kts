import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Koin 4.1.0 supports WASM, so we can include it in common
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.kotlinxDatetime)
        }
        androidMain.dependencies {
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientAndroid)
            implementation(libs.koin.core)
            implementation(libs.kotlinxDatetime)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientDarwin)
            implementation(libs.koin.core)
            implementation(libs.kotlinxDatetime)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientJava)
            implementation(libs.koin.core)
            implementation(libs.kotlinxDatetime)
        }
        // WASM target with Koin 4.1.0 and Ktor 3.2.0 support
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.ktor.clientJs)
            implementation(libs.kotlinxDatetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.alirezaiyan.subscriptionmanager.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    kotlin {
        jvmToolchain(17)
    }
}
