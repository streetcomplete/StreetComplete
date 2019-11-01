package de.westnordost.streetcomplete.data.osm.changes

data class StringMapEntryModify(val key: String, val valueBefore: String, val value: String) : StringMapEntryChange {

    override fun toString() = "MODIFY \"$key\"=\"$valueBefore\" -> \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map[key] != valueBefore
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryModify(key, value, valueBefore)
}
