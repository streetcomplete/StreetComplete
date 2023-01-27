package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class CollectionsTest {

    @Test fun `findNext starts at index inclusive`() {
        assertEquals(2, listOf(1, 2, 3).findNext(1) { true })
    }

    @Test fun `findNext returns null if nothing is found`() {
        assertNull(listOf(1, 2, 3).findNext(1) { it < 2 })
    }

    @Test fun `findNext returns null for empty list`() {
        assertNull(listOf<Int>().findNext(0) { true })
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `findNext throws if out of bounds index`() {
        assertNull(listOf(1, 2, 3).findNext(4) { true })
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `findNext throws if negative index`() {
        assertNull(listOf(1, 2, 3).findNext(-1) { true })
    }

    @Test fun `findPrevious starts at index exclusive`() {
        assertEquals(1, listOf(1, 2, 3).findPrevious(1) { true })
    }

    @Test fun `findPrevious returns null if nothing is found`() {
        assertNull(listOf(1, 2, 3).findPrevious(1) { it > 1 })
    }

    @Test fun `findPrevious returns null for empty list`() {
        assertNull(listOf<Int>().findPrevious(0) { true })
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `findPrevious throws if out of bounds index`() {
        assertNull(listOf(1, 2, 3).findPrevious(4) { true })
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `findPrevious throws if negative index`() {
        assertNull(listOf(1, 2, 3).findPrevious(-1) { true })
    }

    @Test fun `forEachLine with empty list`() {
        listOf<LatLon>().forEachLine { _, _ -> fail() }
    }

    @Test fun `forEachLine with list with only one element`() {
        listOf(p(0.0, 0.0)).forEachLine { _, _ -> fail() }
    }

    @Test fun `forEachLine with several elements`() {
        var counter = 0
        listOf(
            p(0.0, 0.0),
            p(1.0, 0.0),
            p(2.0, 0.0),
            p(3.0, 0.0),
        ).forEachLine { first, second ->
            assertEquals(first.latitude + 1, second.latitude, 0.0)
            counter++
        }
        assertEquals(3, counter)
    }

    @Test fun `indexOfMaxBy with no elements`() {
        assertEquals(-1, listOf<String>().indexOfMaxBy { it.length })
    }

    @Test fun `indexOfMaxBy with some elements`() {
        assertEquals(2, listOf(3, 4, 8).indexOfMaxBy { it })
        assertEquals(0, listOf(4, 0, -1).indexOfMaxBy { it })
    }
}
