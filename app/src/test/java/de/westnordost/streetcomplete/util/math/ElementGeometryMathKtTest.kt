package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import org.junit.Assert
import org.junit.Test

class ElementGeometryMathKtTest {

    @Test
    fun `issue2248 simple`() {
        // https://github.com/streetcomplete/StreetComplete/issues/2248
        // this is the geometry of the street and sidewalk boiled down to the necessary elements
        // to reproduce this bug
        val street19801348 = ElementPolylinesGeometry(listOf(listOf(
            de.westnordost.streetcomplete.testutils.p(50.0751820, 19.8861837),
            de.westnordost.streetcomplete.testutils.p(50.0751033, 19.8865969)
        )), de.westnordost.streetcomplete.testutils.p(50.0751820, 19.8861837))

        val sidewalk406543797 = ElementPolylinesGeometry(listOf(listOf(
            de.westnordost.streetcomplete.testutils.p(50.0750588, 19.8866672),
            de.westnordost.streetcomplete.testutils.p(50.0751290, 19.8862832)
        )), de.westnordost.streetcomplete.testutils.p(50.0751290, 19.8862832))

        Assert.assertTrue(street19801348.isNearAndAligned(10.0, 25.0, listOf(sidewalk406543797)))
    }
}
