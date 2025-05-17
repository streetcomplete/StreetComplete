package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

class CreatedElementsController(
    private val db: CreatedElementsDao
) : CreatedElementsSource {

    private val cache: MutableSet<ElementKey> by lazy {
        synchronized(this) { db.getAll().toMutableSet() }
    }

    override fun contains(elementType: ElementType, elementId: Long): Boolean =
        synchronized(this) { cache.contains(ElementKey(elementType, elementId)) }

    fun putAll(entries: Collection<ElementKey>) {
        synchronized(this) {
            db.putAll(entries)
            cache.addAll(entries)
        }
    }

    fun deleteAll(entries: Collection<ElementKey>) {
        synchronized(this) {
            db.deleteAll(entries)
            cache.removeAll(entries.toSet())
        }
    }

    fun clear() {
        synchronized(this) {
            db.clear()
            cache.clear()
        }
    }
}
