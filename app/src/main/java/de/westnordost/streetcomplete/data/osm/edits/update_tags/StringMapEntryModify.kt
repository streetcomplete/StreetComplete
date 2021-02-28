package de.westnordost.streetcomplete.data.osm.edits.update_tags

data class StringMapEntryModify(val key: String, val valueBefore: String, val value: String) : StringMapEntryChange {

    override fun toString() = "MODIFY \"$key\"=\"$valueBefore\" -> \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map[key] != valueBefore && map[key] != value
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryModify(key, value, valueBefore)
}
