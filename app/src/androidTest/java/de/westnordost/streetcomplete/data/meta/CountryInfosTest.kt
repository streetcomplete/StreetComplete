package de.westnordost.streetcomplete.data.meta

import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays.Companion.getWeekdayIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CountryInfosTest {
    private val validWeekdays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    private fun checkFirstDayOfWorkweekIsValid(info: CountryInfo) {
        assertNotNull(info.getFirstDayOfWorkweek())
        assertTrue(getWeekdayIndex(info.getFirstDayOfWorkweek()) > -1)
    }

    private fun checkLengthUnitIsEitherMeterOrFootAndInch(info: CountryInfo) {
        assertNotNull(info.getLengthUnits())
        assertTrue(info.getLengthUnits().contains("meter")
            || info.getLengthUnits().contains("foot and inch")
        )
    }

    private fun checkSpeedUnitIsEitherMphOrKmh(info: CountryInfo) {
        assertNotNull(info.getSpeedUnits())
        assertTrue(info.getSpeedUnits().contains("kilometers per hour")
            || info.getSpeedUnits().contains("miles per hour")
        )
    }

    private fun checkWeightLimitUnitIsEitherTonOrShortTonOrPound(info: CountryInfo) {
        assertNotNull(info.getWeightLimitUnits())
        assertTrue(info.getWeightLimitUnits().contains("ton")
            || info.getWeightLimitUnits().contains("short ton")
            || info.getWeightLimitUnits().contains("pound")
        )
    }

    private fun checkAdditionalValidHousenumberRegexes(infos: Map<String, CountryInfo>) {
        assertTrue("99 bis".matches(infos["FR"]!!.getAdditionalValidHousenumberRegex()
            .toRegex()))
        assertTrue("s/n".matches(infos["ES"]!!.getAdditionalValidHousenumberRegex()
            .toRegex()))
    }

    private fun checkRegularShoppingDaysIsBetween0And7(info: CountryInfo) {
        assertNotNull(info.getRegularShoppingDays())
        assertTrue(info.getRegularShoppingDays() <= 7)
        assertTrue(info.getRegularShoppingDays() >= 0)
    }

    private fun checkStartOfWorkweekValid(info: CountryInfo) {
        assertTrue(validWeekdays.contains(info.getFirstDayOfWorkweek()))
    }

    @Test
    @Throws(IOException::class)
    fun all() {
        val infos = getAllCountryInfos()
        for ((key, countryInfo) in infos) {
            try {
                assertEquals(key.split("-".toRegex()).toTypedArray()[0], countryInfo.countryCode)
                checkFirstDayOfWorkweekIsValid(countryInfo)
                checkLengthUnitIsEitherMeterOrFootAndInch(countryInfo)
                checkSpeedUnitIsEitherMphOrKmh(countryInfo)
                checkWeightLimitUnitIsEitherTonOrShortTonOrPound(countryInfo)
                checkRegularShoppingDaysIsBetween0And7(countryInfo)
                checkStartOfWorkweekValid(countryInfo)
            } catch (e: Throwable) {
                throw RuntimeException("Error for $key", e)
            }
        }
        checkAdditionalValidHousenumberRegexes(infos)
    }

    @Throws(IOException::class)
    private fun getAllCountryInfos(): Map<String, CountryInfo> {
        val assetManager = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val countryInfos = CountryInfos(assetManager, null)
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
