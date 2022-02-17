package de.westnordost.streetcomplete.data.meta

import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.streetcomplete.osm.LengthUnit
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays.Companion.getWeekdayIndex
import de.westnordost.streetcomplete.quests.max_speed.SpeedMeasurementUnit
import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CountryInfosTest {
    private val validWeekdays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    private fun checkFirstDayOfWorkweekIsValid(info: CountryInfo) {
        assertNotNull(info.firstDayOfWorkweek)
        assertTrue(getWeekdayIndex(info.firstDayOfWorkweek) > -1)
        assertTrue(getWeekdayIndex(info.firstDayOfWorkweek) < 7)
    }

    private fun checkLengthUnitIsEitherMeterOrFootAndInch(info: CountryInfo) {
        assertNotNull(info.lengthUnits)
        assertTrue(info.lengthUnits.contains(LengthUnit.METER)
            || info.lengthUnits.contains(LengthUnit.FOOT_AND_INCH)
        )
    }

    private fun checkSpeedUnitIsEitherMphOrKmh(info: CountryInfo) {
        assertNotNull(info.speedUnits)
        assertTrue(info.speedUnits.contains(SpeedMeasurementUnit.KILOMETERS_PER_HOUR)
            || info.speedUnits.contains(SpeedMeasurementUnit.MILES_PER_HOUR)
        )
    }

    private fun checkWeightLimitUnitIsEitherTonOrShortTonOrPound(info: CountryInfo) {
        assertNotNull(info.weightLimitUnits)
        assertTrue(info.weightLimitUnits.contains(WeightMeasurementUnit.TON)
            || info.weightLimitUnits.contains(WeightMeasurementUnit.SHORT_TON)
            || info.weightLimitUnits.contains(WeightMeasurementUnit.POUND)
        )
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

    private fun checkStartOfWorkweekValid(info: CountryInfo) {
        assertTrue(validWeekdays.contains(info.firstDayOfWorkweek))
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
