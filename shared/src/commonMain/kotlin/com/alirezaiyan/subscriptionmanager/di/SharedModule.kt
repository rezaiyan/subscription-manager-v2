package com.alirezaiyan.subscriptionmanager.di

import com.alirezaiyan.subscriptionmanager.ApiService
import com.alirezaiyan.subscriptionmanager.SubscriptionRepository
import org.koin.dsl.module

val sharedModule = module {
    // API Service - will be provided by platform-specific modules
    single<ApiService> { 
        // This will be overridden by platform-specific modules
        throw IllegalStateException("ApiService must be provided by platform-specific module")
    }
    
    // Repository
    single { SubscriptionRepository(get()) }
} 