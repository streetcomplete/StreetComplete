package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectedPinsLayerTest {

    @Test fun createsOnePointFeaturePerSelectedPosition() {
        val features = selectedPinFeatures(
            listOf(LatLon(1.0, 2.0), LatLon(3.0, 4.0))
        )

        assertEquals(
            listOf(Point(Position(2.0, 1.0)), Point(Position(4.0, 3.0))),
            features.map { it.geometry }
        )
    }

    @Test fun matchesLegacyOvershootInterpolator() {
        assertEquals(0f, overshootInterpolation(0f))
        assertEquals(1f, overshootInterpolation(1f))
        assertEquals(1.078125f, overshootInterpolation(0.75f))
        assertTrue(overshootInterpolation(0.75f) > 1f)
    }
}
