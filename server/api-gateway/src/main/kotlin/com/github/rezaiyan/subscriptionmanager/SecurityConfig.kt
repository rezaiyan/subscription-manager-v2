package com.github.rezaiyan.subscriptionmanager

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration(exclude = [ReactiveSecurityAutoConfiguration::class])
class SecurityConfig 