package com.example.myapplication;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reports")
data class WeeklyReport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val reportDate: String,

    val efficiencyGrowth: Float, // например, 68.0
    val potentialSavings: Float, // например, 2.4

    val lowRiskPercent: Int,
    val mediumRiskPercent: Int,
    val highRiskPercent: Int
)