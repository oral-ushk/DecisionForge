package com.example.myapplication
interface AnalyticsRepository {
    fun calculateRevenue(periodDays: Int): Double
    fun calculateConversion(periodDays: Int): Double
    fun calculateRetention(periodDays: Int): Double
    fun calculateForecastAccuracy(): Double
}
