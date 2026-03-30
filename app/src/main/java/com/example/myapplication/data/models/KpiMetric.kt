package com.example.myapplication.data.models

data class KpiMetric(
    val title: String,
    val value: String,
    val changePercent: Double,
    val isPositive: Boolean
)