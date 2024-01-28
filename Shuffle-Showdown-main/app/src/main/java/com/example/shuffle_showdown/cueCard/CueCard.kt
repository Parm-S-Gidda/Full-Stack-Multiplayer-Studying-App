package com.example.shuffle_showdown.cueCard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// Represents individual Cue Cards to be stored in Firebase
// Assistance from https://developer.android.com/kotlin/parcelize
// Parcelize allows for CueCard to put into a Bundle
@Parcelize
data class CueCard(
    var id: String? = null,
    var deckId: String? = null,
    var term: String? = null,
    var answer: String? = null,
    var isMultipleChoice: Boolean? = null,
    var fakeAnswer1: String? = null,
    var fakeAnswer2: String? = null,
    var fakeAnswer3: String? = null,
): Parcelable