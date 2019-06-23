package de.westnordost.streetcomplete.ktx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
