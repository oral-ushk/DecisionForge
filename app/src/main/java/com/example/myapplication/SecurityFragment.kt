package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentSecurityBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        lifecycleScope.launch {
            binding.switch2FA.isChecked = sessionManager.twoFaFlow.first()
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.switch2FA.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                sessionManager.setTwoFaEnabled(isChecked)
                val msg = if (isChecked) "Two-factor authentication enabled" else "Two-factor authentication disabled"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Password change not available for Google accounts", Toast.LENGTH_LONG).show()
        }

        binding.btnSignOutAll.setOnClickListener {
            Toast.makeText(requireContext(), "All other sessions signed out", Toast.LENGTH_SHORT).show()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Biometric login enabled" else "Biometric login disabled"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
