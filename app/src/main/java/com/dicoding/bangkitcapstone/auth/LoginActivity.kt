package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthAnimationUtils.playLoginAnimation(binding)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        val editText = binding.edtPassword
        val isPasswordVisible = editText.transformationMethod == null

        if (isPasswordVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.btnTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24)
        } else {
            editText.transformationMethod = null
            binding.btnTogglePassword.setImageResource(R.drawable.baseline_visibility_24)
        }
        editText.setSelection(editText.text.length)
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Success -> {
                    showLoading(false)
                    val response = state.response

                    if (response.data?.tokenInfo?.access != null) {
                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("token", response.data.tokenInfo.access)
                            .putString("email", binding.edtEmail.text.toString())
                            .putBoolean("is_verified", response.isVerified)
                            .apply()

                        navigateToOtpVerification()
                    } else {
                        showError("Unable to log in. Please check your email and password")
                    }
                }
                is AuthState.Error -> {
                    showLoading(false)

                    val userFriendlyMessage = when {
                        state.message.contains("401") ->
                            "Invalid email or password. Please try again"
                        state.message.contains("404") ->
                            "Account not found. Please check your email or sign up"
                        state.message.contains("network") ||
                                state.message.contains("connection") ->
                            "Unable to connect. Please check your internet connection"
                        else -> "Something went wrong. Please try again later"
                    }

                    showError(userFriendlyMessage)

                    getSharedPreferences("auth", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Please enter a valid email"
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            binding.edtPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun navigateToOtpVerification() {
        val intent = Intent(this, OtpVerificationActivity::class.java).apply {
            putExtra("email", binding.edtEmail.text.toString())
        }
        startActivity(intent)
        finish()
    }
}