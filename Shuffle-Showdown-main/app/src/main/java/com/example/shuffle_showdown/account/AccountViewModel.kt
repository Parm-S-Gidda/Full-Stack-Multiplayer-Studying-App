package com.example.shuffle_showdown.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
We utilize sealed class Result for easily handling states of 'Loading', 'Success, and 'Failure'.
This allows us to use a progress bar as a visualization for the user.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(private val repository: AccountRepository) : ViewModel() {
    private val _account = MutableLiveData<Result<FirebaseUser?>>()
    val account: LiveData<Result<FirebaseUser?>>
        get() {
            return _account
        }

    private val _register = MutableLiveData<Result<FirebaseUser?>>()
    val register: LiveData<Result<FirebaseUser?>>
        get() {
            return _register
        }

    fun getAccount(email: String, password: String) {
        _account.value = Result.Loading
        viewModelScope.launch {
            try {
                repository.getAccount(email, password) { result ->
                    _account.value = result
                }
            } catch (e: Exception) {
                _account.value = Result.Error(e)
            }
        }
    }

    fun createAccount(email: String, password: String) {
        _register.value = Result.Loading
        viewModelScope.launch {
            try {
                repository.createAccount(email, password) { result ->
                    _register.value = result
                }
            } catch (e: Exception) {
                _register.value = Result.Error(e)
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return repository.getCurrentUser()
    }
}