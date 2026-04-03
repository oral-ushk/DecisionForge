package com.example.myapplication

// Главный контейнер для всех данных приложения
data class DashboardData(
    val efficiencyGrowth: String, // Например, "+68%"
    val savingsAmount: String,    // Например, "2.4M"
    val risks: RiskDistribution,
    val efficiencyChartPoints: List<Float> // Точки для графика
)

// Структура для бублика рисков
data class RiskDistribution(
    val lowRisk: Int,
    val mediumRisk: Int,
    val highRisk: Int
)