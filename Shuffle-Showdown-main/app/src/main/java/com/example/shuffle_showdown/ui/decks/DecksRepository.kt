package com.example.shuffle_showdown.ui.decks

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DecksRepository(private val database: FirebaseFirestore) {

    /*
    Coroutines Assistance from
    https://stackoverflow.com/questions/67464994/how-to-use-kotlin-coroutines-with-an-onsuccesslistner
    A coroutine is needed here because it is an async function. Simply returning cue cards at
    the end will return an empty list. Thus, we use a coroutine for proper timing of loading
    the data from firebase.
     */
    suspend fun getDecks(uid: String): List<DecksModel> = suspendCoroutine { cont ->
        val decks = arrayListOf<DecksModel>()

        database.collection("decks")
            .whereEqualTo("accountId", uid)
            .get()
            .addOnSuccessListener {
                for (deck in it) {
                    val deck = deck.toObject(DecksModel::class.java)
                    decks.add(deck)
                }
                println("debug: Successfully got cue cards from db")
                cont.resume(decks)
            }
            .addOnFailureListener {
                println("debug: Failed to get cue cards from db")
                cont.resumeWithException(it)
            }
//        return cueCards
    }

    fun insertDeck(decks: DecksModel) {
        // References https://stackoverflow.com/questions/46844907/is-it-possible-to-get-the-id-before-it-was-added
        // Generates unique id for the cue card
        val ref = database.collection("decks").document()
        decks.id = ref.id
        ref
            .set(decks)
            .addOnSuccessListener { documentReference ->
                println("debug: Successfully added Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error adding document $e")
            }
    }

    fun updateDeck(deck: DecksModel) {
        // Pretty similar to insertCueCard
        val ref = database.collection("decks")
            .document(deck.id.toString())
        ref
            .set(deck)
            .addOnSuccessListener {
                println("debug: Successfully updated Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error updating document $e")
            }
    }

    fun deleteDeck(id: String) {
        // Pretty similar to updateCueCard
        val ref = database.collection("decks")
            .document(id)
        ref
            .delete()
            .addOnSuccessListener {
                println("debug: Successfully deleted Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error deleting document $e")
            }
    }

}