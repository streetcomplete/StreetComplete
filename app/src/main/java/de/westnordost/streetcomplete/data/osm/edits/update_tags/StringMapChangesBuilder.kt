package de.westnordost.streetcomplete.data.osm.edits.update_tags

class StringMapChangesBuilder(private val source: Map<String, String>) : Map<String, String> {
    private val changes: MutableMap<String, StringMapEntryChange> = mutableMapOf()

    /** Remove only the given key from the map */
    fun removeOne(key: String) {
        changes.remove(key)
        val valueBefore = source[key]
        if (valueBefore != null) {
            addChange(StringMapEntryDelete(key, valueBefore))
        }
    }

    /** Remove the given key (and related keys with metadata) from the map */
    fun remove(key: String) {
        removeOne(key)
        // removeCheckDatesForKey(key)     // FIXME: this one would be better as it remove other check_date keys too, but triggers infinite recursion, so should be done in some other way
        // removeOne("check_date:" + key)  // FIXME: this seems to break test that modify existing tag xxx and thus create check_date:xxx (like `updates check_date` in SidewalkSurfaceCreatorKtTest.kt)
        removeOne("source:" + key)
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
