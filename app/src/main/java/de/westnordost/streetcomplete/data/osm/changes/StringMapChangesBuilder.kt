package de.westnordost.streetcomplete.data.osm.changes

class StringMapChangesBuilder(private val source: Map<String, String>) {
    private val changes: MutableMap<String, StringMapEntryChange> = mutableMapOf()

    fun delete(key: String) {
        val valueBefore = requireNotNull(source[key]) { "The key '$key' does not exist in the map." }
        checkDuplicate(key)
        changes[key] = StringMapEntryDelete(key, valueBefore)
    }

    fun deleteIfExists(key: String) {
        if (source[key] != null) {
            delete(key)
        }
    }

    fun add(key: String, value: String) {
        require(!source.containsKey(key)) { "The key '$key' already exists in the map." }
        checkDuplicate(key)
        changes[key] = StringMapEntryAdd(key, value)
    }

    fun modify(key: String, value: String) {
        val valueBefore = requireNotNull(source[key]) {"The key '$key' does not exist in the map." }
        checkDuplicate(key)
        changes[key] = StringMapEntryModify(key, valueBefore, value)
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

    private fun checkDuplicate(key: String) {
        check(!changes.containsKey(key)) { "The key '$key' is already being modified." }
    }

    fun create() = StringMapChanges(ArrayList(changes.values))
}
