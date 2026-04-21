package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class DashboardFragment : Fragment() {

    private lateinit var aiViewModel: InsightsViewModel
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

    // Специальный "перехватчик" для выбора файла
    private val filePickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Файл выбран! Читаем данные:
            parseCsvFile(uri)
        } else {
            android.widget.Toast.makeText(requireContext(), "Выбор файла отменен", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aiViewModel = ViewModelProvider(requireActivity())[InsightsViewModel::class.java]
        // Настраиваем кнопку загрузки
        binding.fabUploadReport.setOnClickListener {
            // Открываем окно выбора файлов.
            // "text/csv" означает, что мы ищем CSV файлы (можно поменять на "*/*" для любых)
            filePickerLauncher.launch("*/*")
        }

        // 1. Инициализируем ViewModel
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        // 2. ПОДПИСЫВАЕМСЯ на обновления данных (LiveData)
        viewModel.kpiList.observe(viewLifecycleOwner) { dataList ->
            if (dataList.size >= 4) {
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
        }

        // 3. Кнопки ИИ
        binding.btnActionSummary.setOnClickListener { openAiChat() }
        binding.btnActionTrends.setOnClickListener { openAiChat() }
        binding.btnActionAnomalies.setOnClickListener { openAiChat() }

        // 4. Отрисовка графиков
        setupLineChart()
        setupRiskChart()
    }

    private fun openAiChat() {
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        // Замени R.id.navigation_insights на ID твоей вкладки ИИ в bottom_nav_menu.xml
        bottomNav.selectedItemId = R.id.insightsFragment
    }

    private fun setupLineChart() {
        val chart = binding.lineChart

        // Точки для первой линии (Факт - синяя)
        val entriesFact = ArrayList<Entry>()
        entriesFact.add(Entry(1f, 65f)) // Янв
        entriesFact.add(Entry(2f, 72f)) // Фев
        entriesFact.add(Entry(3f, 68f)) // Мар
        entriesFact.add(Entry(4f, 78f)) // Апр
        entriesFact.add(Entry(5f, 82f)) // Май

        // Точки для второй линии (Прогноз - зеленая пунктирная)
        val entriesForecast = ArrayList<Entry>()
        entriesForecast.add(Entry(1f, 60f))
        entriesForecast.add(Entry(2f, 70f))
        entriesForecast.add(Entry(3f, 75f))
        entriesForecast.add(Entry(4f, 80f))
        entriesForecast.add(Entry(5f, 85f))
        entriesForecast.add(Entry(6f, 91f)) // Июн (будущее)

        // Настраиваем дизайн первой линии
        val dataSetFact = LineDataSet(entriesFact, "Факт")
        dataSetFact.color = Color.parseColor("#1976D2")
        dataSetFact.setCircleColor(Color.parseColor("#1976D2"))
        dataSetFact.lineWidth = 2f
        dataSetFact.circleRadius = 4f
        dataSetFact.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSetFact.setDrawValues(false)

        // Настраиваем дизайн второй линии
        val dataSetForecast = LineDataSet(entriesForecast, "Прогноз")
        dataSetForecast.color = Color.parseColor("#4CAF50")
        dataSetForecast.setCircleColor(Color.parseColor("#4CAF50"))
        dataSetForecast.lineWidth = 2f
        dataSetForecast.enableDashedLine(10f, 10f, 0f)
        dataSetForecast.setDrawValues(false)

        // Загружаем данные в график
        val lineData = LineData(dataSetFact, dataSetForecast)
        chart.data = lineData

        // Наводим красоту
        chart.description.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.axisLeft.gridColor = Color.parseColor("#EEEEEE")

        chart.invalidate()
    }

    private fun setupRiskChart() {
        // Проверяем, есть ли этот график в XML. Если пока нет - код не упадет
        val chart = binding.riskPieChart ?: return

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(50f, "Низкий"))
        entries.add(PieEntry(33f, "Средний"))
        entries.add(PieEntry(17f, "Высокий"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"), // Зеленый
            Color.parseColor("#FFEB3B"), // Желтый
            Color.parseColor("#F44336")  // Красный
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        chart.data = PieData(dataSet)
        chart.holeRadius = 60f
        chart.transparentCircleRadius = 65f
        chart.setDrawEntryLabels(false) // Убираем текст с самих кусков бублика
        chart.description.isEnabled = false

        // Легенда снизу
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun parseCsvFile(uri: android.net.Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
            val lines = reader.readLines()

            if (lines.size <= 1) {
                android.widget.Toast.makeText(requireContext(), "Файл пуст", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            val entriesFact = ArrayList<com.github.mikephil.charting.data.Entry>()
            var lastLow = 50f
            var lastMed = 33f
            var lastHigh = 17f

            val aiContextBuilder = StringBuilder("Свежие данные отчета:\n")

            // Читаем со второй строки (индекс 1), так как первая — заголовки
            for (i in 1 until lines.size) {
                val row = lines[i].split(",")
                if (row.size >= 5) {
                    val xIndex = row[0].toFloatOrNull() ?: i.toFloat() // Месяц по оси X
                    val efficiency = row[1].trim().toFloatOrNull() ?: 0f // Эффективность по оси Y

                    entriesFact.add(com.github.mikephil.charting.data.Entry(xIndex, efficiency))

                    // Запоминаем риски из самой ПОСЛЕДНЕЙ строки файла для кругового графика
                    lastLow = row[2].trim().toFloatOrNull() ?: 0f
                    lastMed = row[3].trim().toFloatOrNull() ?: 0f
                    lastHigh = row[4].trim().toFloatOrNull() ?: 0f
                    aiContextBuilder.append("- Месяц $xIndex: Эффективность $efficiency%, Риски: Низкий $lastLow%, Средний $lastMed%, Высокий $lastHigh%\n")
                }
            }
            aiViewModel.currentDataContext = aiContextBuilder.toString()
            // Запускаем перерисовку графиков с новыми данными!
            updateChartsDynamically(entriesFact, lastLow, lastMed, lastHigh)

            android.widget.Toast.makeText(requireContext(), "Отчет загружен!", android.widget.Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(requireContext(), "Ошибка чтения файла", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun updateChartsDynamically(
        newEntries: List<com.github.mikephil.charting.data.Entry>,
        lowR: Float, medR: Float, highR: Float
    ) {
        // 1. Обновляем график (Синяя линия факта)
        val lineChart = binding.lineChart
        if (lineChart.data != null && lineChart.data.dataSetCount > 0) {
            // Берем первую линию (Факт) и меняем ей точки
            val dataSet = lineChart.data.getDataSetByIndex(0) as com.github.mikephil.charting.data.LineDataSet
            dataSet.values = newEntries

            // Заставляем график обновиться с плавной анимацией
            lineChart.data.notifyDataChanged()
            lineChart.notifyDataSetChanged()
            lineChart.animateX(800)
        }

        // 2. Обновляем круговой график рисков
        val pieChart = binding.riskPieChart
        if (pieChart != null) {
            val pieEntries = ArrayList<com.github.mikephil.charting.data.PieEntry>()
            pieEntries.add(com.github.mikephil.charting.data.PieEntry(lowR, "Низкий"))
            pieEntries.add(com.github.mikephil.charting.data.PieEntry(medR, "Средний"))
            pieEntries.add(com.github.mikephil.charting.data.PieEntry(highR, "Высокий"))

            val pieDataSet = com.github.mikephil.charting.data.PieDataSet(pieEntries, "")
            pieDataSet.colors = listOf(
                android.graphics.Color.parseColor("#4CAF50"),
                android.graphics.Color.parseColor("#FFEB3B"),
                android.graphics.Color.parseColor("#F44336")
            )
            pieDataSet.sliceSpace = 3f

            pieChart.data = com.github.mikephil.charting.data.PieData(pieDataSet)
            pieChart.notifyDataSetChanged()
            pieChart.animateY(800)
        }
    }
}