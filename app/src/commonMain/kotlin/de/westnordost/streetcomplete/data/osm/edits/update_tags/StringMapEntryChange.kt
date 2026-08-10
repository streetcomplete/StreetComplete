package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import kotlinx.serialization.Serializable

@Serializable
sealed class StringMapEntryChange {
    abstract val key: String
    abstract override fun toString(): String
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract fun conflictsWith(map: Map<String, String>): Boolean
    abstract fun applyTo(map: MutableMap<String, String>)
    abstract fun reversed(): StringMapEntryChange
    abstract fun isValid(): Boolean
}

@Serializable
data class StringMapEntryAdd(override val key: String, val value: String) : StringMapEntryChange() {

    override fun toString() = "ADD \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map.containsKey(key) && map[key] != value
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryDelete(key, value)
    override fun isValid() =
        key.length <= MAX_OSM_TAG_VALUE_LENGTH && value.length <= MAX_OSM_TAG_VALUE_LENGTH
}

@Serializable
data class StringMapEntryModify(override val key: String, val valueBefore: String, val value: String) : StringMapEntryChange() {

    override fun toString() = "MODIFY \"$key\"=\"$valueBefore\" -> \"$key\"=\"$value\""
    override fun conflictsWith(map: Map<String, String>) = map[key] != valueBefore && map[key] != value
    override fun applyTo(map: MutableMap<String, String>) { map[key] = value }
    override fun reversed() = StringMapEntryModify(key, value, valueBefore)
    override fun isValid() =
        key.length <= MAX_OSM_TAG_VALUE_LENGTH && value.length <= MAX_OSM_TAG_VALUE_LENGTH
}

@Serializable
data class StringMapEntryDelete(override val key: String, val valueBefore: String) : StringMapEntryChange() {

    override fun toString() = "DELETE \"$key\"=\"$valueBefore\""
    override fun conflictsWith(map: Map<String, String>) = map.containsKey(key) && map[key] != valueBefore
    override fun applyTo(map: MutableMap<String, String>) { map.remove(key) }
    override fun reversed() = StringMapEntryAdd(key, valueBefore)
    override fun isValid() = key.length <= MAX_OSM_TAG_VALUE_LENGTH
}
