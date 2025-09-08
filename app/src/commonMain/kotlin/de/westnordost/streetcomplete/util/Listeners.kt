package de.westnordost.streetcomplete.util

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/** Lightweight wrapper around a set for storing listeners in a thread-safe way */
class Listeners<T> {
    private val lock = ReentrantLock()
    private val listeners = HashSet<T>()

    fun add(element: T) {
        lock.withLock { listeners += element }
    }

    fun remove(element: T) {
        lock.withLock { listeners -= element }
    }

    fun forEach(action: (T) -> Unit) {
        val listeners = lock.withLock { listeners.toList() }
        // Copy on the executing of the action itself is not synchronized, only the access to the set,
        // because it should be possible to call several Listeners::forEach at the same time
        listeners.forEach(action)
    }
}
