package de.westnordost.streetcomplete.data.osm.changes

import org.junit.Test

import org.junit.Assert.*

class StringMapChangesBuilderTest {

    @Test fun delete() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.delete("exists")
        val change = builder.create().changes.single() as StringMapEntryDelete
        assertEquals("exists", change.key)
        assertEquals("like this", change.valueBefore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `delete non-existing fails`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.delete("does not exist")
    }

    @Test fun `deleteIfExists non-existing does not fail`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.deleteIfExists("does not exist")
    }

    @Test fun add() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.add("does not exist", "but now")
        val change = builder.create().changes.single() as StringMapEntryAdd
        assertEquals("does not exist", change.key)
        assertEquals("but now", change.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `add already existing fails`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.add("exists", "like that")
    }

    @Test fun modify() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.modify("exists", "like that")
        val change = builder.create().changes.single() as StringMapEntryModify
        assertEquals("exists", change.key)
        assertEquals("like this", change.valueBefore)
        assertEquals("like that", change.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `modify non-existing does fail`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.modify("does not exist", "bla")
    }

    @Test fun `modifyIfExists non-existing does not fail`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.modifyIfExists("does not exist", "bla")
    }

    @Test(expected = IllegalStateException::class)
    fun `duplicate change on same key fails`() {
        val builder = StringMapChangesBuilder(mapOf("exists" to "like this"))
        builder.modify("exists", "like that")
        builder.delete("exists")
    }
}
