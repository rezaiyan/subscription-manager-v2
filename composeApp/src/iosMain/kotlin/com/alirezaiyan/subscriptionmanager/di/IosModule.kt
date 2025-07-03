package com.alirezaiyan.subscriptionmanager.di

import com.alirezaiyan.subscriptionmanager.SubscriptionRepository
import org.koin.dsl.module

val iosModule = module {
    // iOS-specific dependencies can be added here
    // For now, we'll use the shared repository
    // In a real app, you might have iOS-specific services or repositories
} 