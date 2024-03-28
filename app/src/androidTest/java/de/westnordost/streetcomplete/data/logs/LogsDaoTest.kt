package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.logs.LogLevel.*
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: LogsDao

    @BeforeTest fun createDao() {
        dao = LogsDao(database)
    }

    @Test fun getAll_sorts_by_timestamp_descending() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 100)
        val m3 = createMessage("3", timestamp = 10)

        listOf(m1, m2, m3).forEach { dao.add(it) }

        // sorted by timestamp ascending
        assertEquals(listOf(m1, m3, m2), dao.getAll())
    }

    @Test fun getAll_filters_by_levels() {
        val m1 = createMessage("1", level = VERBOSE)
        val m2 = createMessage("2", level = WARNING)
        val m3 = createMessage("3", level = ERROR)

        listOf(m1, m2, m3).forEach { dao.add(it) }

        assertTrue(dao.getAll(levels = setOf(WARNING, ERROR)).containsExactlyInAnyOrder(listOf(m2, m3)))
    }

    @Test fun getAll_filters_containing_string() {
        val m1 = createMessage("very foo")
        val m2 = createMessage("bar")
        val m3 = createMessage("foobar")
        val m4 = createMessage("something else", tag = "very foonky")

        listOf(m1, m2, m3, m4).forEach { dao.add(it) }

        assertTrue(dao.getAll(messageContains = "foo").containsExactlyInAnyOrder(listOf(m1, m3, m4)))
    }

    @Test fun getAll_filters_older_than_timestamp() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 10)

        listOf(m1, m2).forEach { dao.add(it) }

        assertEquals(listOf(m1), dao.getAll(olderThan = 10))
    }

    @Test fun getAll_filters_newer_than_timestamp() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 10)

        listOf(m1, m2).forEach { dao.add(it) }

        assertEquals(listOf(m2), dao.getAll(newerThan = 1))
    }

    @Test fun clear() {
        dao.add(createMessage("1", timestamp = 1))
        dao.add(createMessage("2", timestamp = 2))

        assertEquals(2, dao.clear())
        assertEquals(0, dao.getAll().size)
    }
}

private fun createMessage(
    message: String,
    tag: String = "LogsDaoTest",
    level: LogLevel = VERBOSE,
    timestamp: Long = 1
) = LogMessage(
    level,
    tag,
    message,
    null,
    timestamp
)
