package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.api.ApiService
import com.dicoding.bangkitcapstone.api.AuthResponse
import com.google.android.material.button.MaterialButton

class OtpVerificationActivity : AppCompatActivity() {
    private val apiService = ApiService()
    private lateinit var progressBar: ProgressBar
    private lateinit var btnResendOtp: TextView
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var edtOtpCode: EditText
    private lateinit var btnVerifyOtp: MaterialButton
    private var timeLeft: Long = 60000
    private var isFromRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        isFromRegister = intent.getBooleanExtra("FROM_REGISTER", false)
        initializeViews()
        setClickListeners()
        startCountDownTimer()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        btnResendOtp = findViewById(R.id.btnResendOtp)
        edtOtpCode = findViewById(R.id.edtOtpCode)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)

        val email = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("temp_email", "your email")
        findViewById<TextView>(R.id.tvInstructions).text =
            "We have sent the verification code to $email"
    }

    private fun setClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnVerifyOtp.setOnClickListener {
            val otp = edtOtpCode.text.toString()
            if (validateOtp(otp)) {
                verifyOtp(otp)
            }
        }

        btnResendOtp.setOnClickListener {
            if (timeLeft == 0L) {
                resendOtp()
            }
        }
    }

    private fun validateOtp(otp: String): Boolean {
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
            return false
        }
        if (otp.length != 6) {
            Toast.makeText(this, "Please enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun verifyOtp(otp: String) {
        showLoading(true)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("temp_token", null)
        val email = prefs.getString("temp_email", null)
        val password = prefs.getString("temp_password", null)

        if (token == null || email == null) {
            Log.e("OtpVerification", "Missing token or email")
            handleSessionExpired()
            return
        }

        Log.d("OtpVerification", "Attempting verification with token: $token")

        apiService.verifyOtp(otp, token) { result ->
            runOnUiThread {
                showLoading(false)
                result.fold(
                    onSuccess = { response ->
                        Log.d("OtpVerification", "Verification successful: $response")
                        handleSuccessfulVerification(response)
                    },
                    onFailure = { exception ->
                        Log.e("OtpVerification", "Verification failed", exception)
                        when {
                            exception.message?.contains("Session expired") == true -> {
                                // Try to refresh token or re-login
                                if (password != null) {
                                    reAuthenticate(email, password)
                                } else {
                                    handleSessionExpired()
                                }
                            }
                            else -> {
                                Toast.makeText(
                                    this,
                                    exception.message ?: "Verification failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleSessionExpired() {
        Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun handleSuccessfulVerification(response: AuthResponse) {
        if (response.token != null) {
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .putString("token", response.token)
                .putBoolean("is_verified", true)
                .remove("temp_token")
                .remove("temp_email")
                .remove("temp_password")
                .apply()

            if (isFromRegister) {
                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }
            finishAffinity()
        } else {
            Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reAuthenticate(email: String, password: String) {
        apiService.login(email, password) { result ->
            runOnUiThread {
                result.fold(
                    onSuccess = { response ->
                        if (response.token != null) {
                            // Save new token and retry OTP verification
                            getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("temp_token", response.token)
                                .apply()
                            verifyOtp(edtOtpCode.text.toString())
                        } else {
                            handleSessionExpired()
                        }
                    },
                    onFailure = {
                        handleSessionExpired()
                    }
                )
            }
        }
    }

    private fun resendOtp() {
        showLoading(true)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val email = prefs.getString("temp_email", null)
        val password = prefs.getString("temp_password", null)

        if (email == null || (!isFromRegister && password == null)) {
            Toast.makeText(this, "Session expired, please try again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        val apiCallback: (Result<AuthResponse>) -> Unit = { result ->
            runOnUiThread {
                showLoading(false)
                result.fold(
                    onSuccess = { response ->
                        response.token?.let { token ->
                            prefs.edit()
                                .putString("temp_token", token)
                                .apply()
                            startCountDownTimer()
                            Toast.makeText(this, "New OTP sent to your email", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            this,
                            exception.message ?: "Failed to resend OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        if (isFromRegister) {
            apiService.register(email, password ?: "", apiCallback)
        } else {
            apiService.login(email, password ?: "", apiCallback)
        }
    }

    private fun startCountDownTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        btnResendOtp.isEnabled = false
        timeLeft = 60000

        countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                btnResendOtp.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timeLeft = 0L
                btnResendOtp.isEnabled = true
                btnResendOtp.text = "Resend"
            }
        }.start()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnVerifyOtp.isEnabled = !isLoading
        edtOtpCode.isEnabled = !isLoading
        btnResendOtp.isEnabled = !isLoading && timeLeft == 0L
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}