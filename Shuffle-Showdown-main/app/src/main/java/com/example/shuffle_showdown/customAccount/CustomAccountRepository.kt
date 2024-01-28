package com.example.shuffle_showdown.customAccount

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CustomAccountRepository(private val database: FirebaseFirestore) {

    suspend fun getAccount(accountId: String): CustomAccount = suspendCoroutine { cont ->
        var customAccount: CustomAccount

        database.collection("accounts")
            .whereEqualTo("accountId", accountId)
            .get()
            .addOnSuccessListener {
                for (acc in it) {
                    customAccount = acc.toObject(CustomAccount::class.java)
                    println("debug: Successfully got account from db")
                    cont.resume(customAccount)
                    break
                }
            }
            .addOnFailureListener {
                println("debug: Failed to get account from db")
                cont.resumeWithException(it)
            }
    }

    // Same as getAccount, by using email to find the account
    suspend fun getAccountByEmail(email: String): CustomAccount = suspendCoroutine { cont ->
        var customAccount: CustomAccount
        var index: Int = 0


        database.collection("accounts")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                for (acc in it) {
                    index++
                    customAccount = acc.toObject(CustomAccount::class.java)
                    println("debug: Successfully got account from db")
                    cont.resume(customAccount)
                    break
                }

                //if the user was not found set the id to N/A so the activty can inform the user
                if(index == 0){
                    customAccount = CustomAccount(id = "N/A")
                    cont.resume(customAccount)
                }
            }
            .addOnFailureListener {
                println("debug: Failed to get account from db")
                cont.resumeWithException(it)
            }
    }

    fun createAccount(account: CustomAccount) {
        // Generates unique id for the cue card
        val ref = database.collection("accounts").document()
        account.id = ref.id
        ref
            .set(account)
            .addOnSuccessListener { documentReference ->
                println("debug: Successfully added Account")
            }
            .addOnFailureListener { e ->
                println("debug: Error adding account $e")
            }
    }

//    fun deleteAccount(account: CustomAccount) {}

}