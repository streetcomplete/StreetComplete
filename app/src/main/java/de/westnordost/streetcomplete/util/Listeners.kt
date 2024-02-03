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
        val listenersCopy = synchronized(this) { ArrayList(listeners) }
        // the executing of the action itself is not synchronized, only the access to the set,
        // because it should be possible to call several Listeners::forEach at the same time
        listenersCopy.forEach(action)
    }
}
