package de.westnordost.streetcomplete.data.osm.changes

interface StringMapEntryChange {
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    fun conflictsWith(map: Map<String, String>): Boolean
    fun applyTo(map: MutableMap<String, String>)
    fun reversed(): StringMapEntryChange
}
