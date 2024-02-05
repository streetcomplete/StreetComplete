package de.westnordost.streetcomplete.util

/** Lightweight wrapper around `HashSet` for storing listeners in a thread-safe way */
class Listeners<T> {
    private var listeners: Set<T> = HashSet()

    fun add(element: T) {
        synchronized(this) { listeners = listeners + element }
    }

    fun remove(element: T) {
        synchronized(this) { listeners = listeners - element }
    }

    fun forEach(action: (T) -> Unit) {
        val listeners = synchronized(this) { listeners }
        // the executing of the action itself is not synchronized, only the access to the set,
        // because it should be possible to call several Listeners::forEach at the same time
        listeners.forEach(action)
    }
}
