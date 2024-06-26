package org.lovepeaceharmony.androidapp.repo

sealed class DataState<out R> {
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val msg: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}
