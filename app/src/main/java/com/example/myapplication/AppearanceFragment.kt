package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAppearanceBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppearanceFragment : Fragment() {

    private var _binding: FragmentAppearanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager
    private var selectedTheme = "system"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        lifecycleScope.launch {
            selectedTheme = sessionManager.themeFlow.first()
            updateThemeSelection(selectedTheme)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.cardSystem.setOnClickListener { selectTheme("system") }
        binding.cardLight.setOnClickListener { selectTheme("light") }
        binding.cardDark.setOnClickListener { selectTheme("dark") }

        binding.btnApply.setOnClickListener { applyTheme() }
    }

    private fun selectTheme(theme: String) {
        selectedTheme = theme
        updateThemeSelection(theme)
    }

    private fun updateThemeSelection(theme: String) {
        val unselected = R.drawable.bg_theme_option
        val selected = R.drawable.bg_theme_option_selected

        binding.cardSystem.setBackgroundResource(if (theme == "system") selected else unselected)
        binding.cardLight.setBackgroundResource(if (theme == "light") selected else unselected)
        binding.cardDark.setBackgroundResource(if (theme == "dark") selected else unselected)

        binding.checkSystem.visibility = if (theme == "system") View.VISIBLE else View.INVISIBLE
        binding.checkLight.visibility = if (theme == "light") View.VISIBLE else View.INVISIBLE
        binding.checkDark.visibility = if (theme == "dark") View.VISIBLE else View.INVISIBLE
    }

    private fun applyTheme() {
        lifecycleScope.launch {
            sessionManager.saveTheme(selectedTheme)
            val mode = when (selectedTheme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
