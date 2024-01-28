package com.example.shuffle_showdown.ui.decks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecksViewModel @Inject constructor(private val repository: DecksRepository) : ViewModel() {

    private val _decks = MutableLiveData<List<DecksModel>>()
    val decks: LiveData<List<DecksModel>>
        get() {
            return _decks
        }

    fun getDecks(uid: String) {
        viewModelScope.launch {
            try {
                _decks.value = repository.getDecks(uid)
            } catch (e: Exception) {
                println("debug: error $e")
            }
        }
    }

    // Referencing https://canvas.sfu.ca/courses/80625/pages/room-database
    // The RoomDataBase demo's use of Coroutines to launch repository functions
    fun addDeck(deck: DecksModel) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertDeck(deck)
        }
    }

    fun updateDeck(deck: DecksModel) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateDeck(deck)
        }
    }

    fun deleteDeck(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteDeck(id)
        }
    }
}