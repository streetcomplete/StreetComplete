package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.Letter.A
import de.westnordost.streetcomplete.util.Letter.B
import de.westnordost.streetcomplete.util.Letter.C
import org.junit.Assert.assertEquals
import org.junit.Test

class LastPickedValuesStoreTest {

    @Test fun `mostCommonWithin basic functionality`() {
        assertEquals(
            "Returns the most common items",
            listOf(A, C),
            sequenceOf(A, A, B, C, C).mostCommonWithin(2, 99, 1).toList(),
        )
        assertEquals(
            "Sorted in the original order",
            listOf(A, C, B),
            sequenceOf(A, C, B, B, C).mostCommonWithin(4, 99, 1).toList(),
        )
        assertEquals(
            "Doesn't return nulls",
            listOf(A),
            sequenceOf(A, null).mostCommonWithin(2, 99, 1).toList()
        )
    }

    @Test fun `mostCommonWithin first item(s) special case`() {
        assertEquals(
            "Included even if it is not the most common",
            listOf(A, B),
            sequenceOf(A, B, B, C, C).mostCommonWithin(2, 99, 1).toList(),
        )
        assertEquals(
            "Not included if null",
            listOf(B, C),
            sequenceOf(null, B, C).mostCommonWithin(2, 99, 1).toList(),
        )
        assertEquals(
            "Not duplicated if it was already among the most common",
            listOf(A, B),
            sequenceOf(A, B, A, B).mostCommonWithin(4, 99, 1).toList(),
        )
    }

    @Test fun `mostCommonWithin counts the right number of items`() {
        assertEquals(
            "Always counts the minimum",
            listOf(A, C),
            sequenceOf(A, B, C, C).mostCommonWithin(2, 4, 1).toList(),
        )
        assertEquals(
            "Counts more to find the target",
            listOf(A, B, C),
            sequenceOf(A, B, C, C).mostCommonWithin(3, 1, 1).toList(),
        )
        assertEquals(
            "Counts nulls towards the minimum",
            listOf(A, B),
            sequenceOf(A, null, B, null, C, C).mostCommonWithin(2, 3, 1).toList(),
        )
        assertEquals(
            "Doesn't count null toward the target",
            listOf(A, B, C),
            sequenceOf(A, null, B, null, C, C).mostCommonWithin(3, 2, 1).toList(),
        )
    }

    @Test fun `padWith doesn't include duplicates`() {
        assertEquals(
            listOf(A, B, C),
            sequenceOf(A).padWith(listOf(B, A, C)).toList(),
        )
    }
}

private enum class Letter { A, B, C }
