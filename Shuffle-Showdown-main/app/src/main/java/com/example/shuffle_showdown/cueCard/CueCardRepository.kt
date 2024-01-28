package com.example.shuffle_showdown.cueCard

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.example.shuffle_showdown.account.Result

class CueCardRepository(private val database: FirebaseFirestore) {

    /*
    Coroutines Assistance from
    https://stackoverflow.com/questions/67464994/how-to-use-kotlin-coroutines-with-an-onsuccesslistner
    A coroutine is needed here because it is an async function. Simply returning cue cards at
    the end will return an empty list. Thus, we use a coroutine for proper timing of loading
    the data from firebase.
     */
    suspend fun getCueCards(deckId: String): List<CueCard> = suspendCoroutine { cont ->
        val cueCards = arrayListOf<CueCard>()

        database.collection("cue_cards")
            .whereEqualTo("deckId", deckId)
            .get()
            .addOnSuccessListener {
                for (card in it) {
                    val cue = card.toObject(CueCard::class.java)
                    cueCards.add(cue)
                }
                println("debug: Successfully got cue cards from db")
                cont.resume(cueCards)
            }
            .addOnFailureListener {
                println("debug: Failed to get cue cards from db")
                cont.resumeWithException(it)
            }
    }



    fun insertCueCard(cueCard: CueCard) {
        // References https://stackoverflow.com/questions/46844907/is-it-possible-to-get-the-id-before-it-was-added
        // Generates unique id for the cue card
        val ref = database.collection("cue_cards").document()
        cueCard.id = ref.id
        ref
            .set(cueCard)
            .addOnSuccessListener { documentReference ->
                println("debug: Successfully added Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error adding document $e")
            }
    }

    fun updateCueCard(cueCard: CueCard) {
        // Pretty similar to insertCueCard
        val ref = database.collection("cue_cards")
            .document(cueCard.id.toString())
        ref
            .set(cueCard)
            .addOnSuccessListener {
                println("debug: Successfully updated Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error updating document $e")
            }
    }

    fun deleteCueCard(cueCard: CueCard) {
        // Pretty similar to updateCueCard
        val ref = database.collection("cue_cards")
            .document(cueCard.id.toString())
        ref
            .delete()
            .addOnSuccessListener {
                println("debug: Successfully deleted Cue Card")
            }
            .addOnFailureListener { e ->
                println("debug: Error deleting document $e")
            }
    }

    // Alternative way to get Cue Cards
    // Please refer to AccountRepository
    fun getCueCardsAlt(deckId: String, callback: (Result<List<CueCard>>) -> Unit) {
        val cueCards = arrayListOf<CueCard>()

        database.collection("cue_cards")
            .whereEqualTo("deckId", deckId)
            .get()
            .addOnSuccessListener {
                for (card in it) {
                    val cue = card.toObject(CueCard::class.java)
                    cueCards.add(cue)
                }
                callback(Result.Success(cueCards))
            }
            .addOnFailureListener {
                callback(Result.Error(Exception("Login failed")))
            }
    }

}