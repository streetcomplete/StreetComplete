package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ActiveDatesDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: ActiveDatesDao

    @BeforeTest fun createDao() {
        dao = ActiveDatesDao(database)
    }

    @Test fun addGetClear() {
        assertEquals(emptyList<LocalDate>(), dao.getAll(100))

        val currentDateUTC = systemTimeNow().toLocalDateTime(TimeZone.UTC).date
        dao.addToday()

        assertEquals(listOf(currentDateUTC), dao.getAll(100))

        dao.addToday()

        assertEquals(listOf(currentDateUTC), dao.getAll(100))

        dao.clear()

        assertEquals(emptyList<LocalDate>(), dao.getAll(100))
    }

    @Test fun replaceAll() {
        dao.addToday()

        val dates = listOf(LocalDate.parse("2011-11-11"), LocalDate.parse("2020-12-12"))
        dao.replaceAll(dates)

        assertEquals(dates, dao.getAll(Int.MAX_VALUE))
    }
}
