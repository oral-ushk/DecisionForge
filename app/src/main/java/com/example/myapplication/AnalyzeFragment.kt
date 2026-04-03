package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentAnalyzeBinding
import androidx.lifecycle.ViewModelProvider

class AnalyzeFragment : Fragment() {

    private var _binding: FragmentAnalyzeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Здесь в будущем можно будет анимировать заполнение ProgressBar (графика)
        val sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

// 2. Подписываемся на обновления данных
        sharedViewModel.appData.observe(viewLifecycleOwner) { data ->
            // Этот блок кода будет срабатывать КАЖДЫЙ РАЗ, когда данные меняются!

            // Обновляем текст в зеленых/синих карточках
            binding.tvEfficiencyValue.text = data.efficiencyGrowth
            binding.tvSavingsValue.text = data.savingsAmount

            // Обновляем данные для графика рисков
            val lowRiskText = "${data.risks.lowRisk}%"
            // binding.tvLowRiskValue.text = lowRiskText (и так далее для остальных)

            // Здесь же мы передаем список data.efficiencyChartPoints в библиотеку MPAndroidChart
            // updateChart(data.efficiencyChartPoints)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}