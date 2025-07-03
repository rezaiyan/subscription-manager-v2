package com.alirezaiyan.subscriptionmanager

import android.app.Application
import com.alirezaiyan.subscriptionmanager.di.androidModule
import com.alirezaiyan.subscriptionmanager.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@KoinApplication)
            modules(
                sharedModule,
                androidModule
            )
        }
        
        // Initialize lifecycle management
        ApiServiceLifecycle(this).start()
    }
} 