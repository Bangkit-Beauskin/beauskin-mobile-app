// SignInActivity.kt

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
import com.dicoding.bangkitcapstone.R
import com.dicoding.bangkitcapstone.api.ApiService
import com.google.android.material.button.MaterialButton

class SignInActivity : AppCompatActivity() {
    private val apiService = ApiService()
    private lateinit var progressBar: ProgressBar
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        initializeViews()
        setClickListeners()
    }

    private fun initializeViews() {
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        progressBar = findViewById(R.id.progressBar)

        setupPasswordToggle()
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
        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            if (validateInput(email, password)) {
                performRegister(email, password)
            }
        }

        findViewById<TextView>(R.id.btnLogin).setOnClickListener {
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

    private fun performRegister(email: String, password: String) {
        showLoading(true)

        apiService.register(email, password) { result ->
            runOnUiThread {
                showLoading(false)
                result.fold(
                    onSuccess = { response ->
                        Log.d("SignInActivity", "Register success: $response")
                        handleRegisterSuccess(response.token, email, password)
                    },
                    onFailure = { exception ->
                        Log.e("SignInActivity", "Register failed", exception)
                        val errorMessage = when {
                            exception.message?.contains("email already exists", ignoreCase = true) == true ->
                                "Email already registered"
                            exception.message?.contains("invalid email", ignoreCase = true) == true ->
                                "Invalid email format"
                            exception.message?.contains("password", ignoreCase = true) == true ->
                                "Invalid password format"
                            else -> exception.message ?: "Registration failed"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun handleRegisterSuccess(token: String?, email: String, password: String) {
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
        intent.putExtra("FROM_REGISTER", true)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        edtEmail.isEnabled = !isLoading
        edtPassword.isEnabled = !isLoading
        findViewById<MaterialButton>(R.id.btnSignIn).isEnabled = !isLoading
        btnTogglePassword.isEnabled = !isLoading
    }
}