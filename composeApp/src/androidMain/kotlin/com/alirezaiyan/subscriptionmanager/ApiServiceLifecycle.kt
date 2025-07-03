package com.alirezaiyan.subscriptionmanager

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ApiServiceLifecycle(private val application: Application) : KoinComponent {
    private val apiService: ApiService by inject()

    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                ProcessLifecycleOwner.get().lifecycleScope.launch {
                    apiService.close()
                }
            }
        })
    }
} 