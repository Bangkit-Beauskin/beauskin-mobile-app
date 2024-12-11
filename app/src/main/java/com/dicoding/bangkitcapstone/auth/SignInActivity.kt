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
import com.dicoding.bangkitcapstone.databinding.ActivitySigninBinding

import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthAnimationUtils.playSignInAnimation(binding)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (validateInputs(email, password)) {
                viewModel.register(email, password)
            }
        }

        binding.btnLogin.setOnClickListener {
            finish()
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
                        showError("Unable to register. Please try again")
                    }
                }
                is AuthState.Error -> {
                    showLoading(false)

                    val userFriendlyMessage = when {
                        state.message.contains("409") ||
                                state.message.toLowerCase().contains("already exists") ||
                                state.message.toLowerCase().contains("already registered") -> {
                            showEmailExistsDialog()
                            "This email is already registered. Please login instead"
                        }
                        state.message.contains("network") ||
                                state.message.contains("connection") ->
                            "Unable to connect. Please check your internet connection"
                        state.message.contains("invalid") ->
                            "Invalid email format. Please try again"
                        else -> "Something went wrong. Please try again later"
                    }

                    showError(userFriendlyMessage)
                }
            }
        }
    }

    private fun showEmailExistsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Account Already Exists")
            .setMessage("An account with this email already exists. Would you like to login instead?")
            .setPositiveButton("Login") { _, _ ->
                // Navigate to login
                finish()
            }
            .setNegativeButton("Try Another Email", null)
            .show()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.edtEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Please enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.edtPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.edtPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else if (!password.any { it.isDigit() }) {
            binding.edtPassword.error = "Password must contain at least one number"
            isValid = false
        } else if (!password.any { it.isUpperCase() }) {
            binding.edtPassword.error = "Password must contain at least one uppercase letter"
            isValid = false
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSignIn.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToOtpVerification() {
        val intent = Intent(this, OtpVerificationActivity::class.java).apply {
            putExtra("email", binding.edtEmail.text.toString())
        }
        startActivity(intent)
        finish()
    }
}