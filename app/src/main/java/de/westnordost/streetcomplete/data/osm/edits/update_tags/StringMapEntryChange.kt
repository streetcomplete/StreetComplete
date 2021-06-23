package de.westnordost.streetcomplete.data.osm.edits.update_tags

import kotlinx.serialization.Serializable

@Serializable
sealed class StringMapEntryChange {
    abstract override fun toString(): String
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract fun conflictsWith(map: Map<String, String>): Boolean
    abstract fun applyTo(map: MutableMap<String, String>)
    abstract fun reversed(): StringMapEntryChange
}

@Serializable
data class StringMapEntryAdd(val key: String, val value: String) : StringMapEntryChange() {

    override fun toString() = "ADD \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map.containsKey(key) && map[key] != value
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryDelete(key, value)
}

@Serializable
data class StringMapEntryModify(val key: String, val valueBefore: String, val value: String) : StringMapEntryChange() {

    override fun toString() = "MODIFY \"$key\"=\"$valueBefore\" -> \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map[key] != valueBefore && map[key] != value
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryModify(key, value, valueBefore)
}

@Serializable
data class StringMapEntryDelete(val key: String, val valueBefore: String) : StringMapEntryChange() {

    override fun toString() = "DELETE \"$key\"=\"$valueBefore\""
    override fun conflictsWith(map: Map<String, String>) = map.containsKey(key) && map[key] != valueBefore
    override fun applyTo(map: MutableMap<String, String>) { map.remove(key) }
    override fun reversed() = StringMapEntryAdd(key, valueBefore)
}
