package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentPersonalDetailsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PersonalDetailsFragment : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            binding.etName.setText(session.name)
            binding.etEmail.setText(session.email)
            binding.etEmail.isEnabled = session.provider != "google"
            binding.etPhone.setText(sessionManager.phoneFlow.first())
            binding.etCompany.setText(sessionManager.companyFlow.first())
            binding.etRole.setText(sessionManager.roleFlow.first())
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                sessionManager.updatePersonalDetails(
                    name = name,
                    phone = binding.etPhone.text.toString().trim(),
                    company = binding.etCompany.text.toString().trim(),
                    role = binding.etRole.text.toString().trim()
                )
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
