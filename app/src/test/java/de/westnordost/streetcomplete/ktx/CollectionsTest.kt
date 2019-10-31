package de.westnordost.streetcomplete.ktx

import org.junit.Assert.*
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

    @Test fun `forEachPair with empty list`() {
        listOf<String>().forEachPair { _, _ -> fail() }
    }

    @Test fun `forEachPair with list with only one element`() {
        listOf(1).forEachPair { _, _ -> fail() }
    }

    @Test fun `forEachPair with several elements`() {
        var counter = 0
        listOf(1,2,3,4).forEachPair { first, second ->
            assertEquals(first+1, second)
            counter++
        }
        assertEquals(3, counter)
    }

    @Test fun `indexOfMaxBy with no elements`() {
        assertEquals(-1, listOf<String>().indexOfMaxBy { it.length })
    }

    @Test fun `indexOfMaxBy with some elements`() {
        assertEquals(2, listOf(3,4,8).indexOfMaxBy { it })
        assertEquals(0, listOf(4,0,-1).indexOfMaxBy { it })
    }
}
