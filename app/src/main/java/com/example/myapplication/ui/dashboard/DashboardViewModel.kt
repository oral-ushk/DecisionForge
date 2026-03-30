package com.example.myapplication.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.KpiMetric

class DashboardViewModel : ViewModel() {

    private val _kpiMetrics = MutableLiveData<List<KpiMetric>>()
    val kpiMetrics: LiveData<List<KpiMetric>> = _kpiMetrics

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val mockData = listOf(
            KpiMetric("Revenue", "78%", 12.0, true),
            KpiMetric("Conversion", "33%", 8.0, true),
            KpiMetric("Retention", "48%", -3.0, false)
        )
        _kpiMetrics.value = mockData
    }
}