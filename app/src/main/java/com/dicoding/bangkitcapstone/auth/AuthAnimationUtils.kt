package com.dicoding.bangkitcapstone.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.dicoding.bangkitcapstone.databinding.ActivityLoginBinding
import com.dicoding.bangkitcapstone.databinding.ActivitySigninBinding
import com.dicoding.bangkitcapstone.databinding.ActivityWelcomeBinding

class AuthAnimationUtils {
    fun playWelcomeAnimation(binding: ActivityWelcomeBinding) {
        binding.apply {
            arrayOf(welcomeTitle, welcomeDescription, btnLogin, btnSignIn).forEach {
                it.alpha = 0f
            }
        }

        val imageFloat = ObjectAnimator.ofFloat(binding.welcomeImage, View.TRANSLATION_Y, -30f, 30f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val titleSlide = ObjectAnimator.ofFloat(binding.welcomeTitle, View.TRANSLATION_Y, 50f, 0f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
        }
        val titleFade = ObjectAnimator.ofFloat(binding.welcomeTitle, View.ALPHA, 0f, 1f).apply {
            duration = 1000
        }

        val descriptionFade = ObjectAnimator.ofFloat(binding.welcomeDescription, View.ALPHA, 0f, 1f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
        }

        val loginButtonAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_X, 0.5f, 1f),
                ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_Y, 0.5f, 1f),
                ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 0f, 1f)
            )
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        val signInButtonAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.btnSignIn, View.SCALE_X, 0.5f, 1f),
                ObjectAnimator.ofFloat(binding.btnSignIn, View.SCALE_Y, 0.5f, 1f),
                ObjectAnimator.ofFloat(binding.btnSignIn, View.ALPHA, 0f, 1f)
            )
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        val titleAnimSet = AnimatorSet().apply {
            playTogether(titleSlide, titleFade)
        }

        imageFloat.start()

        AnimatorSet().apply {
            playSequentially(
                titleAnimSet,
                descriptionFade,
                loginButtonAnim,
                signInButtonAnim
            )
            start()
        }
    }
    companion object {
        fun playLoginAnimation(binding: ActivityLoginBinding) {
            // Initial setup - make views invisible
            binding.apply {
                arrayOf(edtEmail, edtPassword, btnLogin, btnSignIn, progressBar).forEach {
                    it.alpha = 0f
                }
            }

            // Email field animation
            val emailField = ObjectAnimator.ofFloat(binding.edtEmail, View.ALPHA, 0f, 1f).apply {
                duration = 500
                interpolator = DecelerateInterpolator()
            }

            // Password field animation
            val passwordField = ObjectAnimator.ofFloat(binding.edtPassword, View.ALPHA, 0f, 1f).apply {
                duration = 500
                interpolator = DecelerateInterpolator()
            }

            // Login button animation with scale and alpha
            val loginButtonScale = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_X, 0.5f, 1f),
                    ObjectAnimator.ofFloat(binding.btnLogin, View.SCALE_Y, 0.5f, 1f),
                    ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 0f, 1f)
                )
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }

            // Sign in button fade animation
            val signInButton = ObjectAnimator.ofFloat(binding.btnSignIn, View.ALPHA, 0f, 1f).apply {
                duration = 500
            }

            // Play animations in sequence
            AnimatorSet().apply {
                playSequentially(
                    emailField,
                    passwordField,
                    loginButtonScale,
                    signInButton
                )
                start()
            }
        }

        fun playSignInAnimation(binding: ActivitySigninBinding) {
            // Initial setup - make views invisible
            binding.apply {
                arrayOf(edtEmail, edtPassword, btnSignIn, btnLogin, progressBar).forEach {
                    it.alpha = 0f
                }
            }

            // Email field slide and fade in
            val emailFieldSlide = ObjectAnimator.ofFloat(binding.edtEmail, View.TRANSLATION_X, -100f, 0f).apply {
                duration = 500
                interpolator = DecelerateInterpolator()
            }
            val emailFieldFade = ObjectAnimator.ofFloat(binding.edtEmail, View.ALPHA, 0f, 1f).apply {
                duration = 500
            }

            // Password field slide and fade in
            val passwordFieldSlide = ObjectAnimator.ofFloat(binding.edtPassword, View.TRANSLATION_X, -100f, 0f).apply {
                duration = 500
                interpolator = DecelerateInterpolator()
            }
            val passwordFieldFade = ObjectAnimator.ofFloat(binding.edtPassword, View.ALPHA, 0f, 1f).apply {
                duration = 500
            }

            // Sign in button animation with bounce effect
            val signInButtonBounce = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.btnSignIn, View.SCALE_X, 0.5f, 1.1f, 1f),
                    ObjectAnimator.ofFloat(binding.btnSignIn, View.SCALE_Y, 0.5f, 1.1f, 1f),
                    ObjectAnimator.ofFloat(binding.btnSignIn, View.ALPHA, 0f, 1f)
                )
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
            }

            // Login button fade in
            val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 0f, 1f).apply {
                duration = 500
                interpolator = AccelerateInterpolator()
            }

            // Combine slide and fade animations for fields
            val emailAnimSet = AnimatorSet().apply {
                playTogether(emailFieldSlide, emailFieldFade)
            }
            val passwordAnimSet = AnimatorSet().apply {
                playTogether(passwordFieldSlide, passwordFieldFade)
            }

            // Play all animations in sequence
            AnimatorSet().apply {
                playSequentially(
                    emailAnimSet,
                    passwordAnimSet,
                    signInButtonBounce,
                    loginButton
                )
                start()
            }
        }

        // Function to animate loading state
        fun showLoading(view: View, show: Boolean) {
            ObjectAnimator.ofFloat(view, View.ALPHA, if (show) 0f else 1f, if (show) 1f else 0f).apply {
                duration = 200
                start()
            }
        }
    }
}