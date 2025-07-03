package com.alirezaiyan.subscriptionmanager.di

import com.alirezaiyan.subscriptionmanager.ApiService
import com.alirezaiyan.subscriptionmanager.SubscriptionRepository
import org.koin.dsl.module

val sharedModule = module {
    // API Service
    single { ApiService() }
    
    // Repository
    single { SubscriptionRepository(get()) }
} 