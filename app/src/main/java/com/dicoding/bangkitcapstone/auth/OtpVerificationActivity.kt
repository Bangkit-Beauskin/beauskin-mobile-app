package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.main.MainActivity
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.OtpResponse
import com.dicoding.bangkitcapstone.databinding.ActivityOtpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private val viewModel: AuthViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private val countdownDuration = 60000L

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupOtpInputs()
        observeViewModel()
        startCountdownTimer()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            btnVerifyOtp.setOnClickListener {
                val otp = getOtpValue()
                if (validateOtp(otp)) viewModel.verifyOtp(otp)
            }

            btnResendOtp.setOnClickListener {
                viewModel.resendOtp()
                btnResendOtp.isEnabled = false
                startCountdownTimer()
            }
            tvInstructions.setText(R.string.otp_verification_instruction)
        }
    }

    private fun setupOtpInputs() {
        val editTexts = with(binding) {
            listOf(edtOtp1, edtOtp2, edtOtp3, edtOtp4, edtOtp5, edtOtp6)
        }

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    when {
                        s?.length == 1 && i < editTexts.size - 1 -> editTexts[i + 1].requestFocus()
                        s?.length == 0 && i > 0 -> editTexts[i - 1].requestFocus()
                    }

                    binding.btnVerifyOtp.isEnabled = editTexts.all { it.text.length == 1 }
                }
            })
        }
    }

    private fun getOtpValue() = with(binding) {
        edtOtp1.text.toString() + edtOtp2.text.toString() +
                edtOtp3.text.toString() + edtOtp4.text.toString() +
                edtOtp5.text.toString() + edtOtp6.text.toString()
    }

    private fun observeViewModel() {
        viewModel.otpState.observe(this) { state ->
            when (state) {
                is OtpState.Loading -> showLoading(true)
                is OtpState.Success -> {
                    showLoading(false)
                    handleOtpSuccess(state.response)
                }
                is OtpState.Error -> {
                    showLoading(false)
                    val userFriendlyMessage = when {
                        state.message.contains("401") ->
                            "Invalid verification code. Please check and try again"
                        state.message.contains("expired") ->
                            "Verification code has expired. Please request a new one"
                        state.message.contains("network") ||
                                state.message.contains("connection") ->
                            "Unable to verify. Please check your internet connection"
                        else -> "Unable to verify code. Please try again"
                    }
                    showError(userFriendlyMessage)
                }
            }
        }

        viewModel.resendState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.btnResendOtp.isEnabled = false
                is AuthState.Success -> {
                    Toast.makeText(
                        this,
                        "New verification code sent to your email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is AuthState.Error -> {
                    binding.btnResendOtp.isEnabled = true
                    val userFriendlyMessage = when {
                        state.message.contains("network") ||
                                state.message.contains("connection") ->
                            "Unable to send code. Please check your internet connection"
                        state.message.contains("too many") ||
                                state.message.contains("limit") ->
                            "Too many attempts. Please try again later"
                        else -> "Unable to send new code. Please try again later"
                    }
                    showError(userFriendlyMessage)
                }
            }
        }
    }

    private fun handleOtpSuccess(response: OtpResponse) {
        if (response.success) {
            getSharedPreferences("auth", MODE_PRIVATE).let { prefs ->
                prefs.getString("token", null)?.let { tokenManager.saveSessionToken(it) }
                prefs.edit().putBoolean("is_verified", true).apply()
            }
            navigateToMain()
        } else {
            val userFriendlyMessage = when {
                response.message.contains("invalid", ignoreCase = true) ->
                    "Incorrect verification code. Please try again"
                response.message.contains("expired", ignoreCase = true) ->
                    "Code has expired. Please request a new one"
                else -> "Verification failed. Please try again"
            }
            showError(userFriendlyMessage)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun startCountdownTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(countdownDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResendOtp.text = getString(
                    R.string.resend_otp_countdown,
                    millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                binding.btnResendOtp.apply {
                    isEnabled = true
                    setText(R.string.resend_otp)
                }
            }
        }.start()
    }

    private fun validateOtp(otp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                showError("Please enter the verification code sent to your email")
                false
            }
            otp.length != 6 -> {
                showError("Please enter all 6 digits of the verification code")
                false
            }
            !otp.all { it.isDigit() } -> {
                showError("Verification code should only contain numbers")
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnVerifyOtp.isEnabled = !isLoading
            listOf(edtOtp1, edtOtp2, edtOtp3, edtOtp4, edtOtp5, edtOtp6).forEach {
                it.isEnabled = !isLoading
            }
            btnResendOtp.isEnabled = !isLoading
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}