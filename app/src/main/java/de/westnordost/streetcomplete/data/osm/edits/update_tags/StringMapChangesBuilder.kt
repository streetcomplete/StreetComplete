package de.westnordost.streetcomplete.data.osm.edits.update_tags

class StringMapChangesBuilder(private val source: Map<String, String>) {
    private val changes: MutableMap<String, StringMapEntryChange> = mutableMapOf()

    fun delete(key: String) {
        val valueBefore = requireNotNull(source[key]) { "The key '$key' does not exist in the map." }
        val change = StringMapEntryDelete(key, valueBefore)
        if (changes[key] == change) return
        checkDuplicate(key)
        changes[key] = change
    }

    fun deleteIfExists(key: String) {
        if (source[key] != null) {
            delete(key)
        }
    }

    fun deleteIfPreviously(key: String, valueBefore: String) {
        if (source[key] == valueBefore) {
            delete(key)
        }
    }

    fun add(key: String, value: String) {
        require(!source.containsKey(key)) { "The key '$key' already exists in the map." }
        val change = StringMapEntryAdd(key, value)
        if (changes[key] == change) return
        checkDuplicate(key)
        changes[key] = change
    }

    fun modify(key: String, value: String) {
        val valueBefore = requireNotNull(source[key]) {"The key '$key' does not exist in the map." }
        val change = StringMapEntryModify(key, valueBefore, value)
        if (changes[key] == change) return
        checkDuplicate(key)
        changes[key] = change
    }

    fun addOrModify(key: String, value: String) {
        val valueBefore = source[key]
        if (valueBefore == null) {
            add(key, value)
        } else {
            modify(key, value)
        }
    }

    fun modifyIfExists(key: String, value: String) {
        if (source[key] != null) {
            modify(key, value)
        }
    }

    fun getPreviousValue(key: String): String? {
        return source[key]
    }

    fun getPreviousEntries(): Map<String, String> {
        return source.toMap()
    }

    fun getChanges(): List<StringMapEntryChange> = changes.values.toList()

    private fun checkDuplicate(key: String) {
        check(!changes.containsKey(key)) { "The key '$key' is already being modified." }
    }

    fun create() = StringMapChanges(ArrayList(changes.values))
}
