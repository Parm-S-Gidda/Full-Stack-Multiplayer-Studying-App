package com.example.shuffle_showdown.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class HistoryModel(
    var id: String = "",
    var accountId: String? = "",
    val deckName: String = "",
    val otherPlayerUserName:String = "",
    val iwon: Int = 0,
    val score:String = ""
): Parcelable