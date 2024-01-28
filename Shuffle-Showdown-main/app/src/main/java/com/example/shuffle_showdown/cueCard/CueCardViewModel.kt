package com.example.shuffle_showdown.cueCard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.shuffle_showdown.account.Result

@HiltViewModel
class CueCardViewModel @Inject constructor(
    private val repository: CueCardRepository,
) : ViewModel() {

    private val _cueCards = MutableLiveData<List<CueCard>>()
    val cueCards: LiveData<List<CueCard>>
        get() {
            return _cueCards
        }
    /*
    Call getCueCards along with observing _cueCards to trigger the observation immediately!!!
    Example:
        viewModel.getCueCards()
        viewModel.cueCards.observe(viewLifecycleOwner) { it ->
            println("debug: $it")
        }
     */
    /*
    Coroutines Assistance from
    https://stackoverflow.com/questions/67464994/how-to-use-kotlin-coroutines-with-an-onsuccesslistner
    https://canvas.sfu.ca/courses/80625/pages/coroutines
    Launches a coroutine in a ViewModel's scope
     */
    fun getCueCards(deckId: String) {
        viewModelScope.launch {
            try {
                _cueCards.value = repository.getCueCards(deckId)
            } catch (e: Exception) {
                println("debug: error $e")
            }
        }
    }

    // Referencing https://canvas.sfu.ca/courses/80625/pages/room-database
    // The RoomDataBase demo's use of Coroutines to launch repository functions
    fun addCueCard(cueCard: CueCard) {
        CoroutineScope(IO).launch {
            repository.insertCueCard(cueCard)
        }
    }

    fun updateCueCard(cueCard: CueCard) {
        CoroutineScope(IO).launch {
            repository.updateCueCard(cueCard)
        }
    }

    fun deleteCueCard(cueCard: CueCard) {
        CoroutineScope(IO).launch {
            repository.deleteCueCard(cueCard)
        }
    }

    // Alternative way to get Cue Cards
    // Please refer to AccountViewModel
    private val _cueCardsAlt = MutableLiveData<Result<List<CueCard?>>>()
    val cueCardsAlt: LiveData<Result<List<CueCard?>>>
        get() {
            return _cueCardsAlt
        }

    fun getCueCardsAlt(deckId: String) {
        _cueCardsAlt.value = Result.Loading
        viewModelScope.launch {
            try {
                repository.getCueCardsAlt(deckId) { result ->
                    _cueCardsAlt.value = result
                }
            } catch (e: Exception) {
                _cueCardsAlt.value = Result.Error(e)
            }
        }
    }
}