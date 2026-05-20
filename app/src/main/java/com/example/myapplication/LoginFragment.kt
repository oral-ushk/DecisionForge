package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: UserSessionManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                lifecycleScope.launch {
                    sessionManager.saveUser(
                        UserSession(
                            userId = account.id ?: System.currentTimeMillis().toString(),
                            name = account.displayName ?: "User",
                            email = account.email ?: "",
                            photoUrl = account.photoUrl?.toString() ?: "",
                            provider = "google"
                        )
                    )
                    navigateToApp()
                }
            } catch (e: ApiException) {
                showError("Google sign-in failed. Please try again.")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = UserSessionManager(requireContext())

        lifecycleScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (session.isLoggedIn) {
                navigateToApp()
                return@launch
            }
            binding.progressBar.visibility = View.GONE
            binding.loginContent.visibility = View.VISIBLE
        }

        binding.btnGoogleSignIn.setOnClickListener { startGoogleSignIn() }
        binding.btnGuest.setOnClickListener { continueAsGuest() }
    }

    private fun startGoogleSignIn() {
        binding.tvError.visibility = View.GONE
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun continueAsGuest() {
        lifecycleScope.launch {
            sessionManager.saveUser(
                UserSession(
                    userId = "guest_${System.currentTimeMillis()}",
                    name = "Guest User",
                    email = "",
                    photoUrl = "",
                    provider = "guest"
                )
            )
            navigateToApp()
        }
    }

    private fun navigateToApp() {
        findNavController().navigate(
            R.id.dashboardFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, inclusive = true)
                .build()
        )
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
