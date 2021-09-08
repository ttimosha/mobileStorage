package com.example.mobilestorage

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilestorage.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        auth = Firebase.auth
        if (auth!!.currentUser != null) {
            startActivity(Intent(this, MainMenu::class.java))
        } else {
            // Продолжить
        }
    }

    fun onClickLogin(view: View?) {
        if (binding.email.text.toString().isEmpty()) {
            binding.email.error = "Пожалуйста, введите свою почту"
            binding.email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            binding.email.error = "Пожалуйста, введите корректную почту"
            binding.email.requestFocus()
            return
        }

        if (binding.password.text.toString().isEmpty()) {
            binding.password.error = "Пожалуйста, введите пароль"
            binding.password.requestFocus()
            return
        }

        auth?.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth!!.currentUser
                    startActivity(Intent(this, MainMenu::class.java))
                } else {
                    Toast.makeText(baseContext, "Ошибка входа. Попробуйте ещё раз",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

}