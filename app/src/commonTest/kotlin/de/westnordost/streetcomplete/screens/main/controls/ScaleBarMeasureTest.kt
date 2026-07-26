package de.westnordost.streetcomplete.screens.main.controls

import de.westnordost.streetcomplete.util.DistanceFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleBarMeasureTest {

    @Test
    fun scale_bar_measure_from_unit_system() {
        assertEquals(
            ScaleBarMeasure.FeetAndMiles,
            ScaleBarMeasure.from(DistanceFormatter.UnitSystem.IMPERIAL_FEET)
        )
        assertEquals(
            ScaleBarMeasure.YardsAndMiles,
            ScaleBarMeasure.from(DistanceFormatter.UnitSystem.IMPERIAL_YARDS)
        )
        assertEquals(
            ScaleBarMeasure.Metric,
            ScaleBarMeasure.from(DistanceFormatter.UnitSystem.METRIC)
        )
    }

    @Test
    fun default_secondary_measure() {
        assertEquals(
            ScaleBarMeasure.Metric,
            defaultSecondaryMeasure(ScaleBarMeasure.FeetAndMiles, "US")
        )
        assertEquals(
            ScaleBarMeasure.FeetAndMiles,
            defaultSecondaryMeasure(ScaleBarMeasure.Metric, "US")
        )
        assertEquals(
            null,
            defaultSecondaryMeasure(ScaleBarMeasure.Metric, "DE")
        )
    }
}
