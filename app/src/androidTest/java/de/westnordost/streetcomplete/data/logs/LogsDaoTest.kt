package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.logs.LogLevel.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LogsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: LogsDao

    @BeforeTest fun createDao() {
        dao = LogsDao(database)
    }

    @Test fun getAll() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 100)
        val m3 = createMessage("3", timestamp = 10)

        listOf(m1, m2, m3).forEach { dao.add(it) }

        // sorted by timestamp descending
        assertEquals(listOf(m2, m3, m1), dao.getAll())
    }

    @Test fun getAllByLevels() {
        val m1 = createMessage("1", level = VERBOSE)
        val m2 = createMessage("2", level = WARNING)
        val m3 = createMessage("3", level = ERROR)

        listOf(m1, m2, m3).forEach { dao.add(it) }

        assertEquals(listOf(m2, m3), dao.getAll(levels = setOf(WARNING, ERROR)))
    }

    @Test fun getAllContainingMessage() {
        val m1 = createMessage("foo")
        val m2 = createMessage("bar")
        val m3 = createMessage("foobar")

        listOf(m1, m2, m3).forEach { dao.add(it) }

        assertEquals(listOf(m1, m3), dao.getAll(messageContains = "foo"))
    }

    @Test fun getAllOlderThan() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 10)

        listOf(m1, m2).forEach { dao.add(it) }

        assertEquals(listOf(m1), dao.getAll(olderThan = 10))
    }

    @Test fun getAllNewerThan() {
        val m1 = createMessage("1", timestamp = 1)
        val m2 = createMessage("2", timestamp = 10)

        listOf(m1, m2).forEach { dao.add(it) }

        assertEquals(listOf(m2), dao.getAll(newerThan = 1))
    }
}

private fun createMessage(
    message: String,
    level: LogLevel = VERBOSE,
    timestamp: Long = 1
) = LogMessage(
    level,
    TAG,
    message,
    null,
    timestamp
)

private const val TAG = "LogsDaoTest"
