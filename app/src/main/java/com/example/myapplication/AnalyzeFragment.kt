package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentAnalyzeBinding
import com.example.myapplication.databinding.ItemWeeklyHistoryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AnalyzeFragment : Fragment() {

    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    private val historyAdapter = WeeklyHistoryAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter
        binding.rvHistory.isNestedScrollingEnabled = false

        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.entries.observe(viewLifecycleOwner) { entries ->
            if (entries.isEmpty()) return@observe
            val latest = entries.last()
            val previous = if (entries.size >= 2) entries[entries.size - 2] else null
            updateSummaryCards(latest, previous)
            updateTrendsChart(entries)
            updateRiskBarChart(entries)
            historyAdapter.submitList(entries.reversed())
        }
    }

    private fun updateSummaryCards(latest: WeeklyEntry, previous: WeeklyEntry?) {
        fun trend(cur: Float, prev: Float?): Pair<String, Boolean> {
            if (prev == null || prev == 0f) return "—" to true
            val diff = cur - prev
            val positive = diff >= 0
            return ("${if (positive) "↗ +" else "↘ "}${"%.1f".format(diff)}%") to positive
        }

        val green = Color.parseColor("#4CAF50")
        val red = Color.parseColor("#F44336")

        binding.tvAnalyzeRevenue.text = "${latest.revenue.toInt()}%"
        val (rt, rp) = trend(latest.revenue, previous?.revenue)
        binding.tvAnalyzeRevenueTrend.text = rt
        binding.tvAnalyzeRevenueTrend.setTextColor(if (rp) green else red)

        binding.tvAnalyzeConversion.text = "${latest.conversion.toInt()}%"
        val (ct, cp) = trend(latest.conversion, previous?.conversion)
        binding.tvAnalyzeConversionTrend.text = ct
        binding.tvAnalyzeConversionTrend.setTextColor(if (cp) green else red)

        binding.tvAnalyzeRetention.text = "${latest.retention.toInt()}%"
        val (ret, rep) = trend(latest.retention, previous?.retention)
        binding.tvAnalyzeRetentionTrend.text = ret
        binding.tvAnalyzeRetentionTrend.setTextColor(if (rep) green else red)

        binding.tvAnalyzeForecast.text = "${latest.forecastAccuracy.toInt()}%"
        val (ft, fp) = trend(latest.forecastAccuracy, previous?.forecastAccuracy)
        binding.tvAnalyzeForecastTrend.text = ft
        binding.tvAnalyzeForecastTrend.setTextColor(if (fp) green else red)
    }

    private fun updateTrendsChart(entries: List<WeeklyEntry>) {
        val chart = binding.lineChartTrends
        val labels = entries.map { it.date }

        fun makeDataSet(values: List<Float>, label: String, color: String): LineDataSet {
            val pts = values.mapIndexed { i, v -> Entry(i.toFloat(), v) }
            return LineDataSet(ArrayList(pts), label).apply {
                this.color = Color.parseColor(color)
                setCircleColor(Color.parseColor(color))
                lineWidth = 2f
                circleRadius = 3.5f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(false)
            }
        }

        val ds1 = makeDataSet(entries.map { it.revenue }, "Revenue", "#1976D2")
        val ds2 = makeDataSet(entries.map { it.conversion }, "Conv.", "#4CAF50")
        val ds3 = makeDataSet(entries.map { it.retention }, "Retn.", "#9C27B0")
        val ds4 = makeDataSet(entries.map { it.forecastAccuracy }, "Forecast", "#FFB300")

        chart.data = LineData(ds1, ds2, ds3, ds4)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.textColor = Color.parseColor("#8A92A6")
        chart.xAxis.textSize = 10f
        chart.axisRight.isEnabled = false
        chart.axisLeft.gridColor = Color.parseColor("#EEEEEE")
        chart.axisLeft.textColor = Color.parseColor("#8A92A6")
        chart.axisLeft.textSize = 10f
        chart.setTouchEnabled(true)
        chart.invalidate()
        chart.animateX(700)
    }

    private fun updateRiskBarChart(entries: List<WeeklyEntry>) {
        val chart = binding.barChartRisk
        val labels = entries.map { it.date }

        val barEntries = entries.mapIndexed { i, e ->
            BarEntry(i.toFloat(), floatArrayOf(e.lowRisk.toFloat(), e.mediumRisk.toFloat(), e.highRisk.toFloat()))
        }

        val dataSet = BarDataSet(ArrayList(barEntries), "Risk").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#F44336")
            )
            stackLabels = arrayOf("Low", "Med", "High")
            setDrawValues(false)
        }

        chart.data = BarData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.textColor = Color.parseColor("#8A92A6")
        chart.xAxis.textSize = 10f
        chart.axisRight.isEnabled = false
        chart.axisLeft.gridColor = Color.parseColor("#EEEEEE")
        chart.axisLeft.textColor = Color.parseColor("#8A92A6")
        chart.axisLeft.textSize = 10f
        chart.setFitBars(true)
        chart.invalidate()
        chart.animateY(700)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class WeeklyHistoryAdapter :
    ListAdapter<WeeklyEntry, WeeklyHistoryAdapter.VH>(object : DiffUtil.ItemCallback<WeeklyEntry>() {
        override fun areItemsTheSame(a: WeeklyEntry, b: WeeklyEntry) = a.date == b.date
        override fun areContentsTheSame(a: WeeklyEntry, b: WeeklyEntry) = a == b
    }) {

    inner class VH(val binding: ItemWeeklyHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemWeeklyHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvEntryDate.text = item.date
            tvEntrySavings.text = "${"%.1f".format(item.potentialSavings)}M"
            tvEntryRevenue.text = "${item.revenue.toInt()}%"
            tvEntryConversion.text = "${item.conversion.toInt()}%"
            tvEntryRetention.text = "${item.retention.toInt()}%"
            tvEntryEfficiency.text = "${item.efficiency.toInt()}%"
            tvEntryLowRisk.text = "Low ${item.lowRisk}%"
            tvEntryMedRisk.text = "Med ${item.mediumRisk}%"
            tvEntryHighRisk.text = "High ${item.highRisk}%"
        }
    }
}
