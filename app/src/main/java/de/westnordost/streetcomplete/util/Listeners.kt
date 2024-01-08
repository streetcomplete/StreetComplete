package de.westnordost.streetcomplete.util

/** Lightweight wrapper around `HashSet` for storing listeners in a thread-safe way */
class Listeners<T> {
    private val listeners = HashSet<T>()

    fun add(element: T): Boolean {
        synchronized(this) {
            return listeners.add(element)
        }
    }

    fun remove(element: T): Boolean {
        synchronized(this) {
            return listeners.remove(element)
        }
    }

    fun forEach(action: (T) -> Unit) {
        synchronized(this) {
            listeners.forEach(action)
        }
    }
}
