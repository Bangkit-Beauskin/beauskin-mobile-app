package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dicoding.bangkitcapstone.MainActivity
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

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnVerifyOtp.setOnClickListener {
                val otp = edtOtpCode.text.toString()
                if (validateOtp(otp)) {
                    viewModel.verifyOtp(otp)
                }
            }

            btnResendOtp.isEnabled = false
            startCountdownTimer()

            btnResendOtp.setOnClickListener {
                viewModel.resendOtp()
                btnResendOtp.isEnabled = false
                startCountdownTimer()
            }
        }
    }

    private fun validateOtp(otp: String): Boolean {
        return if (otp.length != 6) {
            binding.edtOtpCode.error = "Please enter a valid 6-digit OTP code"
            false
        } else {
            true
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

    private fun observeViewModel() {
        viewModel.otpState.observe(this) { state ->
            when (state) {
                is OtpState.Loading -> {
                    showLoading(true)
                }
                is OtpState.Success -> {
                    showLoading(false)
                    if (state.response.success) {
                        Log.d("OtpVerification", "Verification successful, preparing navigation")

                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putBoolean("is_verified", true)
                            .apply()

                        Handler(Looper.getMainLooper()).post {
                            try {
                                Log.d("OtpVerification", "Starting MainActivity")

                                val intent = Intent(this@OtpVerificationActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                                startActivity(intent)
                                finishAffinity()

                                Log.d("OtpVerification", "Navigation complete")
                            } catch (e: Exception) {
                                Log.e("OtpVerification", "Navigation failed", e)
                                showError("Navigation failed: ${e.message}")
                            }
                        }
                    } else {
                        showError(state.response.message)
                    }
                }
                is OtpState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            btnVerifyOtp.isEnabled = !isLoading
            edtOtpCode.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        getSharedPreferences("auth", MODE_PRIVATE).edit()
            .remove("password")
            .apply()
    }
}