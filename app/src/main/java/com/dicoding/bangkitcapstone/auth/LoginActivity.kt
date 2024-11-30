package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    private val apiService = ApiService()
    private lateinit var progressBar: ProgressBar
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setClickListeners()
        checkExistingSession()
    }

    private fun initializeViews() {
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        progressBar = findViewById(R.id.progressBar)

        setupPasswordToggle()

        // Pre-fill email if coming back from registration
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        edtEmail.setText(prefs.getString("last_email", ""))
    }

    private fun setupPasswordToggle() {
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24)
            }

            edtPassword.setSelection(edtPassword.text.length)
        }
    }

    private fun setClickListeners() {
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        findViewById<TextView>(R.id.btnSignIn).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun checkExistingSession() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val isVerified = prefs.getBoolean("is_verified", false)

        if (token != null && isVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        apiService.login(email, password) { result ->
            runOnUiThread {
                showLoading(false)
                result.fold(
                    onSuccess = { response ->
                        Log.d("LoginActivity", "Login success: $response")
                        handleLoginSuccess(response.token, email, password)
                    },
                    onFailure = { exception ->
                        Log.e("LoginActivity", "Login failed", exception)
                        Toast.makeText(
                            this,
                            exception.message ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    private fun handleLoginSuccess(token: String?, email: String, password: String) {
        if (token == null) {
            Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show()
            return
        }

        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putString("temp_token", token)
            .putString("temp_email", email)
            .putString("temp_password", password)
            .putString("last_email", email)
            .apply()

        val intent = Intent(this, OtpVerificationActivity::class.java)
        intent.putExtra("FROM_REGISTER", false)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        edtEmail.isEnabled = !isLoading
        edtPassword.isEnabled = !isLoading
        findViewById<MaterialButton>(R.id.btnLogin).isEnabled = !isLoading
        btnTogglePassword.isEnabled = !isLoading
    }
}