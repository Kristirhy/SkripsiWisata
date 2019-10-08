package com.example.wisatajogja.Utils

interface Callback<T> {

    abstract fun onSuccess(result: T)

    abstract fun onError(result: String)
}