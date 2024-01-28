package com.example.shuffle_showdown.history

import com.example.shuffle_showdown.cueCard.CueCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class HistoryRepository(private val database: FirebaseFirestore) {

    /*
   Coroutines Assistance from
   https://stackoverflow.com/questions/67464994/how-to-use-kotlin-coroutines-with-an-onsuccesslistner
   A coroutine is needed here because it is an async function. Simply returning cue cards at
   the end will return an empty list. Thus, we use a coroutine for proper timing of loading
   the data from firebase.
    */
    suspend fun getHistory(uid: String): List<HistoryModel> = suspendCoroutine { cont ->
        val fullHistory = arrayListOf<HistoryModel>()

        database.collection("history_segments")
            .whereEqualTo("accountId", uid)
            .get()
            .addOnSuccessListener {
                for (segment in it) {
                    val historySegment = segment.toObject(HistoryModel::class.java)
                    fullHistory.add(historySegment)
                }
                println("debug: Successfully got history segments from db")
                cont.resume(fullHistory)
            }
            .addOnFailureListener {
                println("debug: Failed to get history segments from db")
                cont.resumeWithException(it)
            }
    }



    fun insertHistorySegment(historySegment: HistoryModel) {
        // References https://stackoverflow.com/questions/46844907/is-it-possible-to-get-the-id-before-it-was-added
        // Generates unique id for the history segment

        val ref = database.collection("history_segments").document()
        historySegment.id = ref.id
        ref
            .set(historySegment)
            .addOnSuccessListener { documentReference ->
                println("debug: Successfully added history segment")
            }
            .addOnFailureListener { e ->
                println("debug: Error adding document $e")
            }
    }



}