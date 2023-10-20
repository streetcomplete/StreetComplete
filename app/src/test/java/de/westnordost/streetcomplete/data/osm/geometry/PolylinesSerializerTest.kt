package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.testutils.p
import kotlin.test.Test
import kotlin.test.assertEquals

class PolylinesSerializerTest {

    @Test fun `serialize and deserialize empty`() {
        check(listOf())
    }

    @Test fun `serialize and deserialize empty polylines`() {
        check(listOf(listOf(), listOf(), listOf()))
    }

    @Test fun `serialize and deserialize one polyline`() {
        check(listOf(listOf(
            p(0.0, 10.0),
            p(3.0, 4.0),
            p(-10.1111, 12.333),
        )))
    }

    @Test fun `serialize and deserialize several polylines`() {
        check(listOf(
            listOf(
                p(0.0, 10.0),
                p(3.0, 4.0)
            ),
            listOf(
                p(8.1, 5.6),
                p(3.4, 8.3)
            )
        ))
    }

    private fun check(polylines: List<List<LatLon>>) {
        val s = PolylinesSerializer()
        assertEquals(
            polylines,
            s.deserialize(s.serialize(polylines))
        )
    }
}
