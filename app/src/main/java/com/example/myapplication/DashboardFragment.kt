package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    // Переменная для доступа к элементам дизайна
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем макет через ViewBinding
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Пример логики для карточки
        val trendValue = 12.0 // Допустим, это данные с сервера

        if (trendValue > 0) {
            binding.tvRevenueTrend.text = "↗ +$trendValue% vs last week"
            // Красим в зеленый
            binding.tvRevenueTrend.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.tvRevenueTrend.text = "↘ $trendValue% vs last week"
            // Красим в красный
            binding.tvRevenueTrend.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        }
        binding.btnActionSummary.setOnClickListener { openAiChat() }
        binding.btnActionTrends.setOnClickListener { openAiChat() }
        binding.btnActionAnomalies.setOnClickListener { openAiChat() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Обязательно очищаем binding, чтобы избежать утечек памяти
        _binding = null
    }
    private fun openAiChat() {
        // Находим BottomNavigationView в главной активити и программно "нажимаем" на вкладку Insights
        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)

        // ВНИМАНИЕ: Замени R.id.insightsFragment на тот ID, который у тебя прописан в файле menu_bottom_nav.xml для вкладки Insights (где иконка лампочки/чата)
        bottomNav.selectedItemId = R.id.insightsFragment
    }
}

