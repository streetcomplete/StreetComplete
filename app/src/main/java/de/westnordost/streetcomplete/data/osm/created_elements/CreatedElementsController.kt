package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

/** Manages which elements have been created, translating if necessary from the local/temporary
 *  element ids to the proper assigned one from the OSM API (and updating that) */
class CreatedElementsController(
    private val db: CreatedElementsDao
) : CreatedElementsSource {

    private val cache: MutableMap<ElementKey, ElementKey> by lazy {
        synchronized(this) {
            val entries = db.getAll()
            val result = HashMap<ElementKey, ElementKey>(entries.size * 2)
            putAllTo(result, entries)
            result
        }
    }

    override fun contains(elementType: ElementType, elementId: Long): Boolean =
        synchronized(this) { cache.containsKey(ElementKey(elementType, elementId)) }

    override fun getId(elementType: ElementType, elementId: Long): Long? =
        synchronized(this) { cache[ElementKey(elementType, elementId)]?.id }

    fun putAll(entries: Collection<CreatedElementKey>) {
        synchronized(this) {
            db.putAll(entries)
            putAllTo(cache, entries)
        }
    }

    fun deleteAll(entries: Collection<ElementKey>) {
        synchronized(this) {
            val result = db.deleteAll(entries)
            cache.keys.removeAll(entries.toSet())
            return result
        }
    }

    fun clear() {
        synchronized(this) {
            db.clear()
            cache.clear()
        }
    }

    private fun putAllTo(map: MutableMap<ElementKey, ElementKey>, entries: Collection<CreatedElementKey>) {
        for (entry in entries) {
            val key = ElementKey(entry.elementType, entry.newElementId ?: entry.elementId)
            map[key] = key
            if (entry.newElementId != null) {
                val oldKey = ElementKey(entry.elementType, entry.elementId)
                map[oldKey] = key
            }
        }
    }
}
