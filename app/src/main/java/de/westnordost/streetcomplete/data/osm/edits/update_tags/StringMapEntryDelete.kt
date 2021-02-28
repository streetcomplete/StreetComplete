package de.westnordost.streetcomplete.data.osm.edits.update_tags

data class StringMapEntryDelete(val key: String, val valueBefore: String) : StringMapEntryChange {

    override fun toString() = "DELETE \"$key\"=\"$valueBefore\""
    override fun conflictsWith(map: Map<String, String>) = map.containsKey(key) && map[key] != valueBefore
    override fun applyTo(map: MutableMap<String, String>) { map.remove(key) }
    override fun reversed() = StringMapEntryAdd(key, valueBefore)
}
