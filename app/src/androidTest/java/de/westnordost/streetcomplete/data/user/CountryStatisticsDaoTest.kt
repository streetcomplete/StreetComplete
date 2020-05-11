package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CountryStatisticsDaoTest  : ApplicationDbTestCase() {
    private lateinit var dao: CountryStatisticsDao

    @Before fun createDao() {
        dao = CountryStatisticsDao(dbHelper)
    }

    @Test fun addAndSubtract() {
        dao.addOne("DE")
        dao.addOne("DE")
        dao.addOne("DE")
        dao.subtractOne("DE")
        assertEquals(mapOf("DE" to 2), dao.getAll())
    }

    @Test fun getAllReplaceAll() {
        dao.replaceAll(mapOf(
            "DE" to 4,
            "NL" to 1
        ))
        assertEquals(mapOf(
            "DE" to 4,
            "NL" to 1
        ),dao.getAll())
    }
}
