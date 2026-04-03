package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    // Скрытая переменная для записи данных
    private val _appData = MutableLiveData<DashboardData>()

    // Открытая переменная для чтения (Фрагменты будут смотреть только на нее)
    val appData: LiveData<DashboardData> = _appData

    init {
        // Загружаем стартовые данные при запуске
        loadInitialData()
    }

    private fun loadInitialData() {
        _appData.value = DashboardData(
            efficiencyGrowth = "+68%",
            savingsAmount = "2.4M",
            risks = RiskDistribution(33, 50, 17),
            efficiencyChartPoints = listOf(45f, 52f, 60f, 68f, 75f, 88f)
        )
    }

    // ЭТУ ФУНКЦИЮ МЫ БУДЕМ ВЫЗЫВАТЬ, КОГДА ПРИДУТ НОВЫЕ ВХОДНЫЕ ДАННЫЕ
    fun updateDataFromNetwork(newData: DashboardData) {
        _appData.value = newData // UI обновится автоматически!
    }
}