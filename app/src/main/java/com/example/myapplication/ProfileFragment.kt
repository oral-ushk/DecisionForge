package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            sessionManager.sessionFlow.collect { session ->
                if (!session.isLoggedIn) {
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.dashboardFragment, false)
                            .build()
                    )
                    return@collect
                }
                binding.tvUserName.text = session.name
                binding.tvAvatarInitials.text = session.initials
                if (session.email.isNotEmpty()) {
                    binding.tvUserEmail.text = session.email
                    binding.tvUserEmail.visibility = View.VISIBLE
                } else {
                    binding.tvUserEmail.visibility = View.GONE
                }
                binding.tvProviderBadge.text = if (session.provider == "google") "Google Account" else "Guest"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sessionManager.notificationsFlow.collect { enabled ->
                binding.switchNotifications.isChecked = enabled
            }
        }

        binding.rowPersonalDetails.setOnClickListener {
            findNavController().navigate(R.id.personalDetailsFragment)
        }
        binding.rowLanguage.setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }
        binding.rowSecurity.setOnClickListener {
            findNavController().navigate(R.id.securityFragment)
        }
        binding.rowAppearance.setOnClickListener {
            findNavController().navigate(R.id.appearanceFragment)
        }
        binding.rowRateApp.setOnClickListener {
            Toast.makeText(requireContext(), "Thank you for your support!", Toast.LENGTH_SHORT).show()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                sessionManager.setNotificationsEnabled(isChecked)
            }
        }

        binding.btnSignOut.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                sessionManager.clearSession()
                findNavController().navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.dashboardFragment, inclusive = true)
                        .build()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
