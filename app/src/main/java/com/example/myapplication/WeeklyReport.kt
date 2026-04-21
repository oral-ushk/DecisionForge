package com.example.myapplication // Убедись, что тут твой пакет!

// Это наша структура данных из Excel файла
data class WeeklyReport(
    val dateId: String, // Дата отчета, например "2026-04-21" или "Неделя 16"
    val efficiencyGrowth: Float, // Рост эффективности (+68%)
    val potentialSavings: Float, // Экономия (2.4)
    val lowRiskPercent: Int,     // Низкий риск (33)
    val mediumRiskPercent: Int,  // Средний риск (50)
    val highRiskPercent: Int     // Высокий риск (17)
)