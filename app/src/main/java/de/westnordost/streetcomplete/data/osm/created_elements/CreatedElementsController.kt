package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class CreatedElementsController @Inject constructor(
    private val db: CreatedElementsDao
): CreatedElementsSource {

    private val cache: MutableSet<ElementKey> by lazy { db.getAll().toMutableSet() }

    override fun contains(elementType: ElementType, elementId: Long): Boolean =
        cache.contains(ElementKey(elementType, elementId))

    fun putAll(entries: Collection<ElementKey>) {
        synchronized(this) {
            db.putAll(entries)
            cache.addAll(entries)
        }
    }

    fun deleteAll(entries: Collection<ElementKey>) {
        synchronized(this) {
            val result = db.deleteAll(entries)
            cache.removeAll(entries)
            return result
        }
    }

    fun clear() {
        synchronized(this) {
            db.clear()
            cache.clear()
        }
    }
}
