package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dicoding.bangkitcapstone.MainActivity
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.databinding.ActivityOtpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private val viewModel: AuthViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private val COUNTDOWN_TIME = 60000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("OtpVerification", "Activity created")

        setupViews()
        observeViewModel()
        startCountdownTimer()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            btnVerifyOtp.setOnClickListener {
                val otp = edtOtpCode.text.toString()
                Log.d("OtpVerification", "Verify button clicked with OTP: $otp")
                if (validateOtp(otp)) {
                    viewModel.verifyOtp(otp)
                }
            }

            btnResendOtp.setOnClickListener {
                Log.d("OtpVerification", "Resend OTP clicked")
                viewModel.resendOtp()
                btnResendOtp.isEnabled = false
                startCountdownTimer()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.otpState.observe(this) { state ->
            Log.d("OtpVerification", "OTP state changed: $state")
            when (state) {
                is OtpState.Loading -> {
                    showLoading(true)
                }
                is OtpState.Success -> {
                    showLoading(false)
                    handleOtpSuccess(state.response)
                }
                is OtpState.Error -> {
                    Log.e("OtpVerification", "Error state: ${state.message}")
                    showLoading(false)
                    showError(state.message)
                }
            }
        }

        viewModel.resendState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnResendOtp.isEnabled = false
                }
                is AuthState.Success -> {
                    Toast.makeText(this, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                }
                is AuthState.Error -> {
                    binding.btnResendOtp.isEnabled = true
                    showError(state.message)
                }
            }
        }
    }

    private fun handleOtpSuccess(response: OtpResponse) {
        Log.d("OtpVerification", "Handling OTP success: ${response.message}")

        if (response.message.equals("User verified", ignoreCase = true)) {
            Log.d("OtpVerification", "User verified successfully")

            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .putBoolean("is_verified", true)
                .apply()

            navigateToMain()
        } else {
            Log.d("OtpVerification", "OTP verification response indicates failure")
            showError(response.message)
        }
    }

    private fun navigateToMain() {
        Log.d("OtpVerification", "Attempting to navigate to MainActivity")
        try {
            runOnUiThread {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
                startActivity(intent)
                finish()
            }
            Log.d("OtpVerification", "Navigation to MainActivity initiated")
        } catch (e: Exception) {
            Log.e("OtpVerification", "Navigation failed", e)
            showError("Failed to navigate to main screen: ${e.message}")
        }
    }

    private fun startCountdownTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(COUNTDOWN_TIME, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.btnResendOtp.text = "Resend OTP (${secondsRemaining}s)"
            }

            override fun onFinish() {
                binding.btnResendOtp.isEnabled = true
                binding.btnResendOtp.text = "Resend OTP"
            }
        }.start()
    }

    private fun validateOtp(otp: String): Boolean {
        return if (otp.length != 6) {
            binding.edtOtpCode.error = "Please enter a valid 6-digit OTP code"
            false
        } else {
            true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            btnVerifyOtp.isEnabled = !isLoading
            edtOtpCode.isEnabled = !isLoading
            btnResendOtp.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        Log.e("OtpVerification", "Showing error: $message")
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d("OtpVerification", "Activity destroyed")
    }
}