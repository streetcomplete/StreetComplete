package de.westnordost.streetcomplete.data.osm.tql

/** Enum that specifies which type(s) of elements to retrieve  */
enum class ElementsTypeFilter(val tqlName: String, val oqlName: String) {
    NODES("nodes", "node"),
    WAYS("ways", "way"),
    RELATIONS("relations", "rel")
}
