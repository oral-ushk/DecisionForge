package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // Добавь этот импорт
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.ui.dashboard.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Объявляем ViewModel
    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализируем ViewModel
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        // 2. ПОДПИСЫВАЕМСЯ на обновления данных (LiveData)
        viewModel.kpiList.observe(viewLifecycleOwner) { dataList ->
            // Как только ViewModel обновляет данные, срабатывает этот код

            // Наполняем 1-ю карточку (Revenue)
            val revenue = dataList[0]
            binding.tvRevenueValue.text = revenue.value
            binding.tvRevenueTrend.text = revenue.trendText
            binding.tvRevenueTrend.setTextColor(resources.getColor(if (revenue.isPositive) android.R.color.holo_green_dark else android.R.color.holo_red_dark, null))

            // Наполняем 2-ю карточку (Conversion)
            val conversion = dataList[1]
            binding.tvConversionValue.text = conversion.value
            binding.tvConversionTrend.text = conversion.trendText
            binding.tvConversionTrend.setTextColor(resources.getColor(if (conversion.isPositive) android.R.color.holo_green_dark else android.R.color.holo_red_dark, null))

            // Наполняем 3-ю карточку (Retention)
            val retention = dataList[2]
            binding.tvRetentionValue.text = retention.value
            binding.tvRetentionTrend.text = retention.trendText
            binding.tvRetentionTrend.setTextColor(resources.getColor(if (retention.isPositive) android.R.color.holo_green_dark else android.R.color.holo_red_dark, null))

            // Наполняем 4-ю карточку (Forecast)
            val forecast = dataList[3]
            binding.tvForecastValue.text = forecast.value
            binding.tvForecastTrend.text = forecast.trendText
            binding.tvForecastTrend.setTextColor(resources.getColor(if (forecast.isPositive) android.R.color.holo_green_dark else android.R.color.holo_red_dark, null))
        }

        // Кнопки ИИ остаются на месте
        binding.btnActionSummary.setOnClickListener { openAiChat() }
        binding.btnActionTrends.setOnClickListener { openAiChat() }
        binding.btnActionAnomalies.setOnClickListener { openAiChat() }
        setupLineChart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openAiChat() {
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.insightsFragment
    }
    private fun setupLineChart() {
        val chart = binding.lineChart

        // 1. Создаем точки для первой линии (Факт - синяя)
        val entriesFact = ArrayList<com.github.mikephil.charting.data.Entry>()
        entriesFact.add(com.github.mikephil.charting.data.Entry(1f, 65f)) // Янв
        entriesFact.add(com.github.mikephil.charting.data.Entry(2f, 72f)) // Фев
        entriesFact.add(com.github.mikephil.charting.data.Entry(3f, 68f)) // Мар
        entriesFact.add(com.github.mikephil.charting.data.Entry(4f, 78f)) // Апр
        entriesFact.add(com.github.mikephil.charting.data.Entry(5f, 82f)) // Май

        // 2. Создаем точки для второй линии (Прогноз - зеленая пунктирная)
        val entriesForecast = ArrayList<com.github.mikephil.charting.data.Entry>()
        entriesForecast.add(com.github.mikephil.charting.data.Entry(1f, 60f))
        entriesForecast.add(com.github.mikephil.charting.data.Entry(2f, 70f))
        entriesForecast.add(com.github.mikephil.charting.data.Entry(3f, 75f))
        entriesForecast.add(com.github.mikephil.charting.data.Entry(4f, 80f))
        entriesForecast.add(com.github.mikephil.charting.data.Entry(5f, 85f))
        entriesForecast.add(com.github.mikephil.charting.data.Entry(6f, 91f)) // Июн (будущее)

        // 3. Настраиваем дизайн первой линии (Синяя с плавными изгибами)
        val dataSetFact = com.github.mikephil.charting.data.LineDataSet(entriesFact, "Факт")
        dataSetFact.color = android.graphics.Color.parseColor("#1976D2") // Синий
        dataSetFact.setCircleColor(android.graphics.Color.parseColor("#1976D2"))
        dataSetFact.lineWidth = 2f
        dataSetFact.circleRadius = 4f
        dataSetFact.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER // Плавные изгибы
        dataSetFact.setDrawValues(false) // Убираем цифры над точками

        // 4. Настраиваем дизайн второй линии (Зеленая пунктирная)
        val dataSetForecast = com.github.mikephil.charting.data.LineDataSet(entriesForecast, "Прогноз")
        dataSetForecast.color = android.graphics.Color.parseColor("#4CAF50") // Зеленый
        dataSetForecast.setCircleColor(android.graphics.Color.parseColor("#4CAF50"))
        dataSetForecast.lineWidth = 2f
        dataSetForecast.enableDashedLine(10f, 10f, 0f) // Делаем линию пунктирной
        dataSetForecast.setDrawValues(false)

        // 5. Загружаем данные в график
        val lineData = com.github.mikephil.charting.data.LineData(dataSetFact, dataSetForecast)
        chart.data = lineData

        // 6. Наводим красоту: убираем лишние сетки и подписи
        chart.description.isEnabled = false // Убираем текст описания в углу
        chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM // Ось X вниз
        chart.xAxis.setDrawGridLines(false) // Убираем вертикальную сетку
        chart.axisRight.isEnabled = false // Отключаем правую шкалу Y
        chart.axisLeft.gridColor = android.graphics.Color.parseColor("#EEEEEE") // Светлая горизонтальная сетка

        // Перерисовываем график
        chart.invalidate()
    }
}