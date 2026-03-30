package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentAlertsBinding // Проверь правильность импорта

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    // Свойство доступно только между onCreateView и onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // В будущем здесь будет логика загрузки списка уведомлений с сервера
        // и скрытия этого экрана-заглушки (Empty State), если уведомления есть.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Обязательно зануляем, чтобы не было утечек памяти
        _binding = null
    }
}