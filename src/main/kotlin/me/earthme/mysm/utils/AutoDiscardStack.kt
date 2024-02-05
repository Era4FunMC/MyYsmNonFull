package me.earthme.mysm.utils

import java.util.LinkedList

class AutoDiscardStack<T>(private val maxSize: Int) : LinkedList<T>() {
    init {
        if (maxSize <= 0)
            throw IllegalArgumentException("Max size must be greater than 0")
    }

    override fun push(e: T) {
        // Remove the first added element if the elements is over limit
        if (this.size >= maxSize)
            this.removeFirst()
        super.push(e)
    }
}