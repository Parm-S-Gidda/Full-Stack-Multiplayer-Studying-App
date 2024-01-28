package com.example.shuffle_showdown.customAccount

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
    Created for storing the user's address.
    Links with a Firebase account's uid as well.
 */

@Parcelize
data class CustomAccount(
    var id: String? = null,
    var accountId: String? = null,
    var email: String? = null,
    var address: String? = null
): Parcelable