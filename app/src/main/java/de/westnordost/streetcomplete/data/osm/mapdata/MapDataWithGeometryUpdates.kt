package de.westnordost.streetcomplete.data.osm.mapdata

data class MapDataWithGeometryUpdates(
    val updated: MutableMapDataWithGeometry = MutableMapDataWithGeometry(),
    val deleted: MutableList<ElementKey> = ArrayList()
) {
    fun add(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        this.deleted.removeAll(updated.map { it.key })
        this.deleted.addAll(deleted)

        this.updated.removeAll(deleted)
        this.updated.putAll(updated)
    }

    fun clear() {
        updated.clear()
        deleted.clear()
    }
}
