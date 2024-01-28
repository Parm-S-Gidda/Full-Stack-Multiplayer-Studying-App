package com.example.shuffle_showdown.customAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomAccountViewModel @Inject constructor(
    private val repository: CustomAccountRepository
) : ViewModel() {

    private val _customAccount = MutableLiveData<CustomAccount>()
    val customAccount: LiveData<CustomAccount>
        get() {
            return _customAccount
        }

    private val _accountByEmail = MutableLiveData<CustomAccount>()
    val accountByEmail: LiveData<CustomAccount>
        get() {
            return _accountByEmail
        }

    fun getCustomAccount(accountId: String) {
        viewModelScope.launch {
            try {
                _customAccount.value = repository.getAccount(accountId)
            } catch (e: Exception) {
                println("debug: error $e")
            }
        }
    }

    fun getAccountByEmail(email: String) {
        viewModelScope.launch {
            try {
                _accountByEmail.value = repository.getAccountByEmail(email)
            } catch (e: Exception) {
                println("debug: error $e")
            }
        }
    }

    fun createCustomAccount(account: CustomAccount) {
        viewModelScope.launch {
            repository.createAccount(account)
        }
    }

}