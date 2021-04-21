package de.westnordost.streetcomplete.data.osm.edits.update_tags

// TODO make sealed as soon as Kotlin supports that (1.5)
interface StringMapEntryChange {
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    fun conflictsWith(map: Map<String, String>): Boolean
    fun applyTo(map: MutableMap<String, String>)
    fun reversed(): StringMapEntryChange
}
