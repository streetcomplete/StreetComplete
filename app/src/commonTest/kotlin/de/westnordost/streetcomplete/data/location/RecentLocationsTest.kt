package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RecentLocationsTest {
    @Test fun `getAll returns nothing`() {
        val r = RecentLocations(10.seconds, 1.0)
        assertTrue(r.getAll().toList().isEmpty())
    }

    @Test fun `getAll one`() {
        val r = RecentLocations(10.seconds, 1.0)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        r.add(l1)

        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `getAll ordered by elapsedDuration descending`() {
        val r = RecentLocations(10.seconds, 1.0)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 2.seconds)

        r.add(l1)
        r.add(l2)
        for ((first, second) in r.getAll().toList().asSequenceOfPairs()) {
            assertTrue(first.elapsedDuration > second.elapsedDuration)
        }
    }

    @Test fun `does not add older locations`() {
        val r = RecentLocations(10.seconds, 1.0)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 10.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 1.seconds)
        r.add(l1)
        r.add(l2)
        assertEquals(
            l1,
            r.getAll().toList().single()
        )
    }

    @Test fun `removes older locations too close to added location on add`() {
        val r = RecentLocations(10.seconds, 100.0)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(0.0, 0.0).translate(80.0, 0.0), 1f, 2.seconds)
        val l3 = Location(LatLon(0.0, 0.0).translate(160.0, 0.0), 1f, 3.seconds)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())

        r.add(l2)
        assertEquals(listOf(l2, l1), r.getAll().toList())

        r.add(l3)
        assertEquals(listOf(l3, l1), r.getAll().toList())
    }

    @Test fun `removes oldest locations on add`() {
        val r = RecentLocations(10.seconds, 100.0)
        val l1 = Location(LatLon(0.0, 0.0), 1f, 1.seconds)
        val l2 = Location(LatLon(1.0, 0.0), 1f, 5.seconds)
        val l3 = Location(LatLon(2.0, 0.0), 1f, 8.seconds)
        val l4 = Location(LatLon(3.0, 0.0), 1f, 17.seconds)

        r.add(l1)
        assertEquals(listOf(l1), r.getAll().toList())
        r.add(l2)
        assertEquals(listOf(l2, l1), r.getAll().toList())
        r.add(l3)
        assertEquals(listOf(l3, l2, l1), r.getAll().toList())
        r.add(l4)
        assertEquals(listOf(l4, l3), r.getAll().toList())
    }
}
