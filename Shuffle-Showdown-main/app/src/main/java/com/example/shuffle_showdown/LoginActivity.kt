package com.example.shuffle_showdown

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.account.Result
import com.example.shuffle_showdown.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            login()
        }

    }

    // Referencing https://firebase.google.com/docs/auth/android/password-auth
    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = accountViewModel.getCurrentUser()
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

    private fun login() {
        val email = binding.loginEmail.text.toString()
        val password = binding.loginPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            // Referencing https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
            // Checks if the email input is valid
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                accountViewModel.getAccount(email, password)
                accountViewModel.account.observe(this) {
                    when (it) {
                        is Result.Loading -> {
                            // Loading
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            // Login success, go into app
                            binding.progressBar.visibility = View.GONE

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            Toast.makeText(this, "The login is successful.", Toast.LENGTH_SHORT).show()

                            // Remove the observer so there can only be one
                            accountViewModel.account.removeObservers(this)
                        }
                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "The password is invalid.", Toast.LENGTH_SHORT).show()

                            // Remove the observer so there can only be one
                            accountViewModel.account.removeObservers(this)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "The email is invalid.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }
}