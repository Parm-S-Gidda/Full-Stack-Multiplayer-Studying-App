package com.example.shuffle_showdown.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuffle_showdown.history.HistoryModel
import com.example.shuffle_showdown.history.HistoryRepository
import com.example.shuffle_showdown.ui.decks.DecksModel
import com.example.shuffle_showdown.ui.decks.DecksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repository: HistoryRepository) : ViewModel() {

    private val _historySegments = MutableLiveData<List<HistoryModel>>()
    val historySegments: LiveData<List<HistoryModel>>
        get() {
            return _historySegments
        }

    fun getFullHistory(uid: String) {
        viewModelScope.launch {
            try {
                _historySegments.value = repository.getHistory(uid)
            } catch (e: Exception) {
                println("debug: error $e")
            }
        }
    }

    // Referencing https://canvas.sfu.ca/courses/80625/pages/room-database
    // The RoomDataBase demo's use of Coroutines to launch repository functions
    fun addHistorySegment(segment: HistoryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertHistorySegment(segment)
        }
    }

}