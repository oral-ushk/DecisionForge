package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentLanguageBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager
    private var selectedLanguage = "en"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        lifecycleScope.launch {
            selectedLanguage = sessionManager.languageFlow.first()
            updateLanguageSelection(selectedLanguage)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.rbEnglish.setOnClickListener { selectLanguage("en") }
        binding.rbRussian.setOnClickListener { selectLanguage("ru") }
        binding.rbKazakh.setOnClickListener { selectLanguage("kk") }
        binding.rbDeutsch.setOnClickListener { selectLanguage("de") }

        binding.btnApply.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.saveLanguage(selectedLanguage)
                requireActivity().recreate()
            }
        }
    }

    private fun selectLanguage(lang: String) {
        selectedLanguage = lang
        updateLanguageSelection(lang)
    }

    private fun updateLanguageSelection(lang: String) {
        binding.rbEnglish.isChecked = lang == "en"
        binding.rbRussian.isChecked = lang == "ru"
        binding.rbKazakh.isChecked = lang == "kk"
        binding.rbDeutsch.isChecked = lang == "de"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
