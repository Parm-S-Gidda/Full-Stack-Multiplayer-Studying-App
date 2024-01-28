package com.example.shuffle_showdown.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

class AccountRepository(private val auth: FirebaseAuth) {

    /*
    Since auth.signInWithEmailAndPassword is asynchronous and returns a Task object, we use a
    callback function to handle this, making it so we can return the newly logged in user.
    The alternative way is to use a suspend function, which can be seen in CueCardRepository.kt.
    Gets the inputted account from the database, if it exists.
     */
    fun getAccount(email: String, password: String, callback: (Result<FirebaseUser?>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Login success, go into app
                    callback(Result.Success(auth.currentUser))
                } else {
                    // Login failed, usually invalid password
                    callback(Result.Error(Exception("Login failed")))
                }
            }
    }

    fun createAccount(email: String, password: String, callback: (Result<FirebaseUser?>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Register successful
                    callback(Result.Success(auth.currentUser))
                } else {
                    // Register failed
                    when (it.exception) {
                        is FirebaseAuthWeakPasswordException ->
                            callback(Result.Error(Exception("Weak password.")))
                        is FirebaseAuthInvalidCredentialsException ->
                            callback(Result.Error(Exception("Invalid email.")))
                        else ->
                            callback(Result.Error(Exception("Failed to register account. Email may already be in use.")))
                    }
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

//    fun deleteAccount() {}

//    fun updateAccount() {}

}