package com.alirezaiyan.subscriptionmanager

const val SERVER_PORT = 8080

fun formatCurrency(amount: Double): String {
    val cents = (amount * 100).toInt()
    val dollars = cents / 100
    val centsRemainder = cents % 100
    return "$$dollars.${centsRemainder.toString().padStart(2, '0')}"
}