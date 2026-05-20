package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        val insightsViewModel = ViewModelProvider(requireActivity())[InsightsViewModel::class.java]

        binding.fabUploadReport.setOnClickListener {
            AddEntryDialogFragment().show(childFragmentManager, "add_entry")
        }

        binding.btnActionSummary.setOnClickListener { openAiChat() }
        binding.btnActionTrends.setOnClickListener { openAiChat() }
        binding.btnActionAnomalies.setOnClickListener { openAiChat() }

        sharedViewModel.entries.observe(viewLifecycleOwner) { entries ->
            if (entries.isEmpty()) return@observe
            val latest = entries.last()
            val previous = if (entries.size >= 2) entries[entries.size - 2] else null

            updateKpiCards(latest, previous)
            updatePerformanceTrend(entries)
            updateRiskChart(latest)

            insightsViewModel.currentDataContext = buildString {
                appendLine("Weekly data (${entries.size} entries):")
                entries.forEach { e ->
                    appendLine("${e.date}: Revenue=${e.revenue}%, Conv=${e.conversion}%, " +
                        "Retn=${e.retention}%, Effic=${e.efficiency}%, Savings=${e.potentialSavings}M, " +
                        "Risk Low=${e.lowRisk}% Med=${e.mediumRisk}% High=${e.highRisk}%")
                }
            }
        }
    }

    private fun updateKpiCards(latest: WeeklyEntry, previous: WeeklyEntry?) {
        val green = resources.getColor(android.R.color.holo_green_dark, null)
        val red = resources.getColor(android.R.color.holo_red_dark, null)

        fun trend(cur: Float, prev: Float?): Pair<String, Boolean> {
            if (prev == null || prev == 0f) return "—" to true
            val diff = cur - prev
            val positive = diff >= 0
            val pct = diff / prev * 100
            return ("${if (positive) "↗ +" else "↘ "}${"%.1f".format(pct)}% vs last week") to positive
        }

        val (rt, rp) = trend(latest.revenue, previous?.revenue)
        binding.tvRevenueValue.text = "${latest.revenue.toInt()}%"
        binding.tvRevenueTrend.text = rt
        binding.tvRevenueTrend.setTextColor(if (rp) green else red)

        val (ct, cp) = trend(latest.conversion, previous?.conversion)
        binding.tvConversionValue.text = "${latest.conversion.toInt()}%"
        binding.tvConversionTrend.text = ct
        binding.tvConversionTrend.setTextColor(if (cp) green else red)

        val (ret, rep) = trend(latest.retention, previous?.retention)
        binding.tvRetentionValue.text = "${latest.retention.toInt()}%"
        binding.tvRetentionTrend.text = ret
        binding.tvRetentionTrend.setTextColor(if (rep) green else red)

        val (ft, fp) = trend(latest.forecastAccuracy, previous?.forecastAccuracy)
        binding.tvForecastValue.text = "${latest.forecastAccuracy.toInt()}%"
        binding.tvForecastTrend.text = ft
        binding.tvForecastTrend.setTextColor(if (fp) green else red)
    }

    private fun updatePerformanceTrend(entries: List<WeeklyEntry>) {
        val chart = binding.lineChart
        val labels = entries.map { it.date }

        val effPts = entries.mapIndexed { i, e -> Entry(i.toFloat(), e.efficiency) }
        val revPts = entries.mapIndexed { i, e -> Entry(i.toFloat(), e.revenue) }

        val effDs = LineDataSet(ArrayList(effPts), "Efficiency").apply {
            color = Color.parseColor("#2155D6")
            setCircleColor(Color.parseColor("#2155D6"))
            lineWidth = 2.5f
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#2155D6")
            fillAlpha = 20
        }
        val revDs = LineDataSet(ArrayList(revPts), "Revenue").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 2f
            circleRadius = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            enableDashedLine(10f, 5f, 0f)
        }

        chart.data = LineData(effDs, revDs)
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textColor = Color.parseColor("#8A92A6")
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.textColor = Color.parseColor("#8A92A6")
        chart.xAxis.textSize = 10f
        chart.axisRight.isEnabled = false
        chart.axisLeft.gridColor = Color.parseColor("#EEEEEE")
        chart.axisLeft.textColor = Color.parseColor("#8A92A6")
        chart.invalidate()
        chart.animateX(600)
    }

    private fun updateRiskChart(latest: WeeklyEntry) {
        val chart = binding.riskPieChart ?: return
        val entries = listOf(
            PieEntry(latest.lowRisk.toFloat(), "Low"),
            PieEntry(latest.mediumRisk.toFloat(), "Med"),
            PieEntry(latest.highRisk.toFloat(), "High")
        )
        val ds = PieDataSet(ArrayList(entries), "").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#F44336")
            )
            sliceSpace = 3f
            selectionShift = 4f
        }
        chart.data = PieData(ds)
        chart.holeRadius = 62f
        chart.transparentCircleRadius = 67f
        chart.setDrawEntryLabels(false)
        chart.description.isEnabled = false
        chart.legend.textColor = Color.parseColor("#8A92A6")
        chart.notifyDataSetChanged()
        chart.animateY(600)
    }

    private fun openAiChat() {
        requireActivity()
            .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
            .selectedItemId = R.id.insightsFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
