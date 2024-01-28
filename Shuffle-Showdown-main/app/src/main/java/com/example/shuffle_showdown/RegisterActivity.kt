package com.example.shuffle_showdown

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.account.Result
import com.example.shuffle_showdown.customAccount.CustomAccount
import com.example.shuffle_showdown.customAccount.CustomAccountViewModel
import com.example.shuffle_showdown.databinding.ActivityRegisterBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Enumeration

@AndroidEntryPoint
class RegisterActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val accountViewModel: AccountViewModel by viewModels()
    private val customAccountViewModel: CustomAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            registerAccount()
        }

        binding.textView.setOnClickListener {
            finish()
        }

    }

    // Referencing https://firebase.google.com/docs/auth/android/password-auth
    private fun registerAccount() {
        // Get the inputs
        val email = binding.registerEmail.text.toString()
        val password = binding.registerPassword.text.toString()
        val passwordConfirm = binding.registerPasswordConfirm.text.toString()

        // Check if inputs aren't empty
        if (email.isNotEmpty() &&
            password.isNotEmpty() &&
            passwordConfirm.isNotEmpty()) {

            if (password == passwordConfirm) {

                accountViewModel.createAccount(email, password)
                accountViewModel.register.observe(this) {
                    when (it) {
                        is Result.Loading -> {
                            // Loading
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            // Register successful
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "Successfully registered account.", Toast.LENGTH_SHORT).show()

                            createCustomAccount()

                            finish()

                            // Remove the observer so there can only be one
                            accountViewModel.register.removeObservers(this)
                        }
                        is Result.Error -> {
                            // Register failed
                            binding.progressBar.visibility = View.GONE

                            Toast.makeText(this, it.exception.message, Toast.LENGTH_SHORT).show()

                            // Remove the observer so there can only be one
                            accountViewModel.register.removeObservers(this)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Passwords must be matching.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }

    // For linking the account with the address
    private fun createCustomAccount() {
        val acc = accountViewModel.getCurrentUser()
        val myIP = getPrivateIpAddress()

        println("my IP: " + myIP)

        customAccountViewModel.createCustomAccount(
            CustomAccount(
                id = "",
                accountId = acc!!.uid,
                email = acc.email,
                address = myIP
            )
        )
    }

    // get the private ip address of the current device
    // sources: https://www.stepstoperform.com/2023/02/retrieving-device-ip-in-kotlin-android.html and
    // CHATGPT for help with distinguishing private IP addresses
    private fun getPrivateIpAddress(): String {
        try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface: NetworkInterface = interfaces.nextElement()
                val addresses: Enumeration<InetAddress> = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address: java.net.InetAddress = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val ipAddress = address.hostAddress
                        // Check if the IP address is in a private range
                        if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith(
                                "172."
                            )
                        ) {
                            return ipAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1" // default address if unable to obtain a private IP
    }


}