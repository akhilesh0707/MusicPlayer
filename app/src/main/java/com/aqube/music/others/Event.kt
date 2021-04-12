package com.aqube.music.others

open class Event<out T>(private val data: T) {
    var hadBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hadBeenHandled) {
            null
        } else {
            hadBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}