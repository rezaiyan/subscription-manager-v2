package com.alirezaiyan.subscriptionmanager.di

import com.alirezaiyan.subscriptionmanager.ApiService
import com.alirezaiyan.subscriptionmanager.ApiServiceWasm
import com.alirezaiyan.subscriptionmanager.SubscriptionRepository
import com.alirezaiyan.subscriptionmanager.SubscriptionViewModel
import org.koin.dsl.module


val webModule = module {
    single<ApiService> {
        ApiServiceWasm()
    }
    single {
        SubscriptionRepository(get())
    }
    single {
        SubscriptionViewModel(get())
    }
}
