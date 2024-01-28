package com.example.shuffle_showdown.ui.decks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DecksModel(
    var id: String = "",
    var accountId: String? = "",
    val name: String = "",
    val favorite: Boolean = false,
    val cardCount: Int = 0
): Parcelable