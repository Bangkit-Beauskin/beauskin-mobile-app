package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.databinding.ActivityWelcomeBinding
import com.dicoding.bangkitcapstone.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.dicoding.bangkitcapstone.data.local.TokenManager


@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthAnimationUtils().playWelcomeAnimation(binding)

        // Hide action bar
        supportActionBar?.hide()

        // Check authentication status
        if (checkAuthentication()) {
            navigateToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            btnSignIn.setOnClickListener {
                val intent = Intent(this@WelcomeActivity, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkAuthentication(): Boolean {
        return tokenManager.isLoggedIn() &&
                (tokenManager.isSessionTokenValid() || tokenManager.isAccessTokenValid())
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}