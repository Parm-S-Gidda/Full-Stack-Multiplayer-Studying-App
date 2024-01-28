package com.example.shuffle_showdown.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InviteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is invites Fragment"
    }
    val text: LiveData<String> = _text
}