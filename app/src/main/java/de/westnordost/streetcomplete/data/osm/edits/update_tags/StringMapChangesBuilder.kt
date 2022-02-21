package de.westnordost.streetcomplete.data.osm.edits.update_tags

class StringMapChangesBuilder(private val source: Map<String, String>) : Map<String, String> {
    private val changes: MutableMap<String, StringMapEntryChange> = mutableMapOf()

    /** Remove the given key from the map */
    fun remove(key: String) {
        changes.remove(key)
        val valueBefore = source[key]
        if (valueBefore != null) {
            addChange(StringMapEntryDelete(key, valueBefore))
        }
    }

    /** put the given value for the given key */
    operator fun set(key: String, value: String) {
        val valueBefore = source[key]
        addChange(if (valueBefore == null) {
            StringMapEntryAdd(key, value)
        } else {
            StringMapEntryModify(key, valueBefore, value)
        })
    }

    /* ----------------------- */

    override val size: Int get() = entries.size
    override fun isEmpty(): Boolean = size == 0
    override fun containsKey(key: String): Boolean = get(key) != null
    override fun containsValue(value: String): Boolean = value in values

    override operator fun get(key: String): String? =
        if (changes.containsKey(key)) {
            when (val change = changes.getValue(key)) {
                is StringMapEntryAdd -> change.value
                is StringMapEntryModify -> change.value
                is StringMapEntryDelete -> null
            }
        } else {
            source[key]
        }

    override val keys: Set<String> get() = entries.map { it.key }.toSet()
    override val values: Collection<String> get() = entries.map { it.value }

    override val entries: Set<Map.Entry<String, String>> get() {
        val result = mutableSetOf<Map.Entry<String, String>>()
        for ((k, v) in source) {
            when (val change = changes[k]) {
                // modified
                is StringMapEntryModify -> {
                    result.add(Entry(k, change.value))
                }
                // deleted
                is StringMapEntryDelete -> {}
                // otherwise use entry from source map
                else -> {
                    result.add(Entry(k, v))
                }
            }
        }
        // add added entries
        for (add in changes.values.filterIsInstance<StringMapEntryAdd>()) {
            result.add(Entry(add.key, add.value))
        }
        return result
    }

    data class Entry(override val key: String, override val value: String) : Map.Entry<String, String>

    val hasChanges: Boolean get() = changes.values.any { change ->
        change !is StringMapEntryModify || change.value != change.valueBefore
    }

    private fun addChange(change: StringMapEntryChange) {
        if (changes[change.key] == change) return
        changes[change.key] = change
    }

    fun create() = StringMapChanges(changes.values)
}
