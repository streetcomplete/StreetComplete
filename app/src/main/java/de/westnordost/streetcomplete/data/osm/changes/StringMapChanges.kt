package de.westnordost.streetcomplete.data.osm.changes

import java.util.ArrayList
import java.util.NoSuchElementException

/** A diff that can be applied on a map of strings. Use StringMapChangesBuilder to conveniently build
 * it. A StringMapChanges is immutable.  */
class StringMapChanges(changes: Collection<StringMapEntryChange>) {
    val changes: List<StringMapEntryChange> = ArrayList(changes)

    fun isEmpty() = changes.isEmpty()

    /** @return a StringMapChanges that exactly reverses this StringMapChanges */
    fun reversed() = StringMapChanges(changes.map { it.reversed() })

    /** Return whether the changes have a conflict with the given map  */
    fun hasConflictsTo(map: Map<String, String>) = ConflictIterator(map).hasNext()

    /** Return an iterable to iterate through the changes that have conflicts with the given map  */
    fun getConflictsTo(map: Map<String, String>) = object : Iterable<StringMapEntryChange> {
        override fun iterator() = ConflictIterator(map)
    }

    /** Applies this diff to the given map.  */
    fun applyTo(map: MutableMap<String, String>) {
        check(!hasConflictsTo(map)) { "Could not apply the diff, there is at least one conflict." }

        for (change in changes) {
            change.applyTo(map)
        }
    }

    override fun equals(other: Any?) = changes == (other as? StringMapChanges)?.changes
    override fun hashCode() = changes.hashCode()
    override fun toString() = changes.joinToString()

    private inner class ConflictIterator(private val map: Map<String, String>) : Iterator<StringMapEntryChange> {
        private var next: StringMapEntryChange? = null
        private val it = changes.iterator()

        override fun hasNext(): Boolean {
            findNext()
            return next != null
        }

        override fun next(): StringMapEntryChange {
            findNext()
            val result = next
            next = null
            if (result == null) {
                throw NoSuchElementException()
            }
            return result
        }

        private fun findNext() {
            if (next == null) {
                while (it.hasNext()) {
                    val change = it.next()
                    if (change.conflictsWith(map)) {
                        next = change
                        return
                    }
                }
            }
        }
    }
}
