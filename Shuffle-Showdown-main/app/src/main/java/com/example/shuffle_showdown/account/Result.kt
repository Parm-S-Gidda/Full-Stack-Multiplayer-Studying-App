package com.example.shuffle_showdown.account

/*
Referencing https://bignerdranch.com/blog/using-stateflow-over-livedata-for-end-to-end-operations/
Utilized in implementing the visualization of loading data, which is used regarding user account
 */
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
