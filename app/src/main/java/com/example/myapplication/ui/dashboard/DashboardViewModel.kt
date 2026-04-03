package com.example.myapplication.ui.dashboard // Не забудь свой пакет!
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// Это наша модель данных. Она описывает структуру одной карточки.
data class KpiData(
    val title: String,
    val value: String,
    val trendText: String,
    val isPositive: Boolean // true = зеленый плюс, false = красный минус
)


class DashboardViewModel : ViewModel() {

    // _kpiList - приватная переменная, которую может менять только сама ViewModel
    private val _kpiList = MutableLiveData<List<KpiData>>()
    // kpiList - публичная переменная, которую Фрагмент будет только читать
    val kpiList: LiveData<List<KpiData>> = _kpiList

    init {
        // Как только ViewModel создается, загружаем данные
        loadData()
    }

    private fun loadData() {
        // Сейчас мы генерируем эти данные вручную (Mock Data).
        // В будущем именно здесь мы вызовем API сервера или базу данных!
        val data = listOf(
            KpiData("Revenue", "78%", "↗ +12% vs last week", true),
            KpiData("Conversion", "33%", "↗ +8% vs last week", true),
            KpiData("Retention", "48%", "↘ -3% vs last week", false),
            KpiData("Forecast Acc.", "75%", "↗ +5% vs last week", true)
        )
        // Кладем готовые данные в LiveData
        _kpiList.value = data
    }
}