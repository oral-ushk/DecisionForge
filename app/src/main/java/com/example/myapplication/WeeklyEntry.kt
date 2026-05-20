package com.example.myapplication

data class WeeklyEntry(
    val date: String,
    val revenue: Float,
    val conversion: Float,
    val retention: Float,
    val forecastAccuracy: Float,
    val efficiency: Float,
    val potentialSavings: Float,
    val lowRisk: Int,
    val mediumRisk: Int,
    val highRisk: Int
)
