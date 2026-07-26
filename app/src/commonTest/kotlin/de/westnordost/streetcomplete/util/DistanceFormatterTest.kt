package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.DistanceFormatter.DisplayUnit
import de.westnordost.streetcomplete.util.DistanceFormatter.UnitSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceFormatterTest {

    private val testSymbols = mapOf(
        DisplayUnit.METERS to "m",
        DisplayUnit.KILOMETERS to "km",
        DisplayUnit.FEET to "ft",
        DisplayUnit.YARDS to "yd",
        DisplayUnit.MILES to "mi"
    )

    private fun format(distanceInMeters: Double, system: UnitSystem): String =
        DistanceFormatter.format(distanceInMeters, system) { testSymbols.getValue(it) }

    @Test
    fun unit_system_from_region() {
        assertEquals(UnitSystem.IMPERIAL_FEET, UnitSystem.fromRegion("US"))
        assertEquals(UnitSystem.IMPERIAL_YARDS, UnitSystem.fromRegion("GB"))
        assertEquals(UnitSystem.METRIC, UnitSystem.fromRegion("DE"))
        assertEquals(UnitSystem.METRIC, UnitSystem.fromRegion(null))
    }

    @Test
    fun format_metric() {
        assertEquals("0 m", format(0.0, UnitSystem.METRIC))
        assertEquals("12 m", format(12.4, UnitSystem.METRIC))
        assertEquals("120 m", format(120.0, UnitSystem.METRIC))
        assertEquals("999 m", format(999.0, UnitSystem.METRIC))
        assertEquals("1 km", format(1000.0, UnitSystem.METRIC))
        assertEquals("1.3 km", format(1250.0, UnitSystem.METRIC))
        assertEquals("12.5 km", format(12543.0, UnitSystem.METRIC))
    }

    @Test
    fun format_imperial_feet() {
        assertEquals("0 ft", format(0.0, UnitSystem.IMPERIAL_FEET))
        assertEquals("100 ft", format(30.48, UnitSystem.IMPERIAL_FEET))
        assertEquals("5279 ft", format(1609.039, UnitSystem.IMPERIAL_FEET))
        assertEquals("1 mi", format(1609.344, UnitSystem.IMPERIAL_FEET))
        assertEquals("2.5 mi", format(4023.36, UnitSystem.IMPERIAL_FEET))
    }

    @Test
    fun format_imperial_yards() {
        assertEquals("0 yd", format(0.0, UnitSystem.IMPERIAL_YARDS))
        assertEquals("100 yd", format(91.44, UnitSystem.IMPERIAL_YARDS))
        assertEquals("1759 yd", format(1608.43, UnitSystem.IMPERIAL_YARDS))
        assertEquals("1 mi", format(1609.344, UnitSystem.IMPERIAL_YARDS))
        assertEquals("1.5 mi", format(2414.016, UnitSystem.IMPERIAL_YARDS))
    }
}
