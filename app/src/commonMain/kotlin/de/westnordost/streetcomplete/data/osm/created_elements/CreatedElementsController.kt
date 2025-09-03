package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

class CreatedElementsController(
    private val db: CreatedElementsDao
) : CreatedElementsSource {

    // all needs to be synchronized so that the db and cache remain in sync
    private val lock = ReentrantLock()
    private val cache: MutableSet<ElementKey> by lazy {
        lock.withLock { db.getAll().toMutableSet() }
    }

    override fun contains(elementType: ElementType, elementId: Long): Boolean =
        lock.withLock { cache.contains(ElementKey(elementType, elementId)) }

    fun putAll(entries: Collection<ElementKey>) {
        lock.withLock {
            db.putAll(entries)
            cache.addAll(entries)
        }
    }

    fun deleteAll(entries: Collection<ElementKey>) {
        lock.withLock {
            db.deleteAll(entries)
            cache.removeAll(entries.toSet())
        }
    }

    fun clear() {
        lock.withLock {
            db.clear()
            cache.clear()
        }
    }
}
