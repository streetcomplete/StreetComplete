package de.westnordost.streetcomplete.data.osm.edits.update_tags

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringMapChangesBuilderTest {

    @Test fun remove() {
        val builder = builder("exists" to "like this")
        builder.remove("exists")

        assertEquals(
            StringMapEntryDelete("exists", "like this"),
            builder.changes.single()
        )
    }

    @Test fun `delete non-existing does nothing`() {
        val builder = builder("exists" to "like this")
        builder.remove("does not exist")

        assertTrue(builder.changes.isEmpty())
    }

    @Test fun add() {
        val builder = builder("exists" to "like this")
        builder["does not exist"] = "but now"

        assertEquals(
            StringMapEntryAdd("does not exist", "but now"),
            builder.changes.single()
        )
    }

    @Test fun `modify same`() {
        val builder = builder("a" to "1")
        builder["a"] = "1"

        assertEquals(
            StringMapEntryModify("a", "1", "1"),
            builder.changes.single()
        )
    }

    @Test fun `add twice`() {
        val builder = builder()
        builder["a"] = "2"
        builder["a"] = "3"

        assertEquals(
            StringMapEntryAdd("a", "3"),
            builder.changes.single()
        )
    }

    @Test fun `add then remove`() {
        val builder = builder()
        builder["a"] = "2"
        builder.remove("a")

        assertTrue(builder.changes.isEmpty())
    }

    @Test fun `remove then add`() {
        val builder = builder("a" to "1")
        builder.remove("a")
        builder["a"] = "2"

        assertEquals(
            StringMapEntryModify("a", "1", "2"),
            builder.changes.single()
        )
    }

    @Test fun modify() {
        val builder = builder("exists" to "like this")
        builder["exists"] = "like that"
        assertEquals(
            StringMapEntryModify("exists", "like this", "like that"),
            builder.changes.single()
        )
    }

    @Test fun isEmpty() {
        // non-empty source map
        assertFalse(builder("a" to "1").isEmpty())
        // empty source map
        assertTrue(builder().isEmpty())

        // empty source map but addition
        assertFalse(builder().also { it["a"] = "1" }.isEmpty())
        // non-empty source map but removal
        assertTrue(builder("a" to "1").also { it.remove("a") }.isEmpty())
    }

    @Test fun size() {
        assertEquals(0, builder().size)
        assertEquals(1, builder("a" to "1").size)
        assertEquals(2, builder("a" to "1", "b" to "2").size)

        assertEquals(1, builder("a" to "1").also { it["a"] = "2" }.size)
        assertEquals(2, builder("a" to "1").also { it["b"] = "2" }.size)
        assertEquals(1, builder("a" to "1", "b" to "2").also { it.remove("a") }.size)
    }

    @Test fun containsKey() {
        assertTrue(builder("a" to "1").containsKey("a"))
        assertFalse(builder("a" to "1").containsKey("b"))

        assertTrue(builder().also { it["a"] = "1" }.containsKey("a"))
        assertFalse(builder("a" to "1").also { it.remove("a") }.containsKey("a"))
        assertTrue(builder("a" to "1").also { it["a"] = "2" }.containsKey("a"))
    }

    @Test fun containsValue() {
        assertTrue(builder("a" to "1").containsValue("1"))
        assertFalse(builder().containsValue("1"))

        assertTrue(builder().also { it["a"] = "1" }.containsValue("1"))
        assertFalse(builder("a" to "1").also { it.remove("a") }.containsValue("1"))
        assertTrue(builder("a" to "1").also { it["a"] = "2" }.containsValue("2"))
        assertFalse(builder("a" to "1").also { it["a"] = "2" }.containsValue("1"))

        assertTrue(builder("a" to "1", "b" to "1").also { it["a"] = "2" }.containsValue("1"))
    }

    @Test fun get() {
        assertEquals("1", builder("a" to "1")["a"])
        assertEquals(null, builder("a" to "1")["b"])

        assertEquals("1", builder().also { it["a"] = "1" }["a"])
        assertEquals(null, builder("a" to "1").also { it.remove("a") }["a"])
        assertEquals("2", builder("a" to "1").also { it["a"] = "2" }["a"])
    }

    @Test fun keys() {
        assertEquals(setOf("a"), builder("a" to "1").keys)
        assertEquals(setOf<String>(), builder().keys)

        assertEquals(setOf("a"), builder().also { it["a"] = "1" }.keys)
        assertEquals(setOf<String>(), builder("a" to "1").also { it.remove("a") }.keys)
        assertEquals(setOf("a"), builder("a" to "1").also { it["a"] = "2" }.keys)
    }

    @Test fun values() {
        assertEquals(listOf("1"), builder("a" to "1").values)
        assertEquals(listOf<String>(), builder().values)

        assertEquals(listOf("1"), builder().also { it["a"] = "1" }.values)
        assertEquals(listOf<String>(), builder("a" to "1").also { it.remove("a") }.values)
        assertEquals(listOf("2"), builder("a" to "1").also { it["a"] = "2" }.values)

        assertEquals(listOf("1", "1"), builder("a" to "1", "b" to "1").values)
    }

    @Test fun entries() {
        assertEquals(mapOf("a" to "1"), builder("a" to "1").map)
        assertEquals(mapOf<String, String>(), builder().map)

        assertEquals(mapOf("a" to "1"), builder().also { it["a"] = "1" }.map)
        assertEquals(mapOf<String, String>(), builder("a" to "1").also { it.remove("a") }.map)
        assertEquals(mapOf("a" to "2"), builder("a" to "1").also { it["a"] = "2" }.map)

        assertEquals(mapOf("a" to "1", "b" to "1"), builder("a" to "1", "b" to "1").map)
    }

    @Test fun hasChanges() {
        assertFalse(builder("a" to "1").hasChanges)
        assertFalse(builder().hasChanges)

        assertTrue(builder().also { it["a"] = "1" }.hasChanges)
        assertTrue(builder("a" to "1").also { it["a"] = "2" }.hasChanges)
        assertFalse(builder("a" to "1").also { it["a"] = "1" }.hasChanges)

        assertFalse(builder().also { it.remove("a") }.hasChanges)
        assertTrue(builder("a" to "1").also { it.remove("a") }.hasChanges)
    }
}

private fun builder(vararg tags: Pair<String, String>) = StringMapChangesBuilder(mapOf(*tags))

private val StringMapChangesBuilder.changes get() = create().changes

private val StringMapChangesBuilder.map get() = entries.map { it.key to it.value }.toMap()
