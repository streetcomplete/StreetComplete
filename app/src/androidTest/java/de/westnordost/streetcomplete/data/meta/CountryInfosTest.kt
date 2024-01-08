package de.westnordost.streetcomplete.data.meta

import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays.Companion.getWeekdayIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CountryInfosTest {
    private val validWeekdays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    private fun checkFirstDayOfWorkweekIsValid(info: CountryInfo) {
        assertNotNull(info.firstDayOfWorkweek)
        assertTrue(validWeekdays.contains(info.firstDayOfWorkweek))
        assertTrue(getWeekdayIndex(info.firstDayOfWorkweek) > -1)
        assertTrue(getWeekdayIndex(info.firstDayOfWorkweek) < 7)
    }

    private fun checkAdditionalValidHousenumberRegexes(infos: Map<String, CountryInfo>) {
        assertTrue("99 bis".matches(infos["FR"]!!.additionalValidHousenumberRegex!!.toRegex()))
        assertTrue("s/n".matches(infos["ES"]!!.additionalValidHousenumberRegex!!.toRegex()))
    }

    private fun checkRegularShoppingDaysIsBetween0And7(info: CountryInfo) {
        assertNotNull(info.regularShoppingDays)
        assertTrue(info.regularShoppingDays <= 7)
        assertTrue(info.regularShoppingDays >= 0)
    }

    @Test fun all() {
        val infos = getAllCountryInfos()
        for ((key, countryInfo) in infos) {
            try {
                assertEquals(key.split("-").first(), countryInfo.countryCode)
                checkFirstDayOfWorkweekIsValid(countryInfo)
                checkRegularShoppingDaysIsBetween0And7(countryInfo)
            } catch (e: Throwable) {
                throw RuntimeException("Error for $key", e)
            }
        }
        checkAdditionalValidHousenumberRegexes(infos)
    }

    private fun getAllCountryInfos(): Map<String, CountryInfo> {
        val assetManager = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val countryInfos = CountryInfos(assetManager)
        val fileList = assetManager.list("country_metadata")!!
        assertNotNull(fileList)

        return fileList.associate { filename ->
            val country = filename.substring(0, filename.lastIndexOf("."))
            try {
                (country to countryInfos.get(listOf(country)))
            } catch (e: Throwable) {
                throw RuntimeException("Error for $filename", e)
            }
        }
    }
}
