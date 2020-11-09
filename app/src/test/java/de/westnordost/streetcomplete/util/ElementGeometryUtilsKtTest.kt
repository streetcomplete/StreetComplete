package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import org.junit.Assert.assertTrue

import org.junit.Test

class ElementGeometryUtilsKtTest {

    @Test fun `issue2248 simple`() {
        // https://github.com/westnordost/StreetComplete/issues/2248
        // this is the geometry of the street and sidewalk boiled down to the necessary elements
        // to reproduce this bug
        val street19801348 = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(50.0751820, 19.8861837),
            OsmLatLon(50.0751033, 19.8865969)
        )), OsmLatLon(50.0751820, 19.8861837))

        val sidewalk406543797 = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(50.0750588, 19.8866672),
            OsmLatLon(50.0751290, 19.8862832)
        )), OsmLatLon(50.0751290, 19.8862832))

        assertTrue(street19801348.isNearAndAligned(10.0, 25.0, listOf(sidewalk406543797)))
    }
}
