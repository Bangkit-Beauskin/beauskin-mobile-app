package com.dicoding.bangkitcapstone.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.bangkitcapstone.MainActivity
import com.dicoding.bangkitcapstone.R

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        findViewById<TextView>(R.id.btnLogin).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            val email = findViewById<EditText>(R.id.edtEmail).text.toString()
            val password = findViewById<EditText>(R.id.edtPassword).text.toString()

            // Handle sign in logic here
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Closes all activities in the stack
        }
    }
}