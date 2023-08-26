package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.Letter.A
import de.westnordost.streetcomplete.util.Letter.B
import de.westnordost.streetcomplete.util.Letter.C
import kotlin.test.Test
import kotlin.test.assertEquals

class LastPickedValuesStoreTest {

    @Test
    fun `mostCommonWithin basic functionality`() {
        assertEquals(
            listOf(A, C),
            sequenceOf(A, A, B, C, C).mostCommonWithin(2, 99, 1).toList(),
            "Returns the most common items",
        )
        assertEquals(
            listOf(A, C, B),
            sequenceOf(A, C, B, B, C).mostCommonWithin(4, 99, 1).toList(),
            "Sorted in the original order",
        )
        assertEquals(
            listOf(A),
            sequenceOf(A, null).mostCommonWithin(2, 99, 1).toList(),
            "Doesn't return nulls",
        )
    }

    @Test fun `mostCommonWithin first item(s) special case`() {
        assertEquals(
            listOf(A, B),
            sequenceOf(A, B, B, C, C).mostCommonWithin(2, 99, 1).toList(),
            "Included even if it is not the most common",
        )
        assertEquals(
            listOf(B, C),
            sequenceOf(null, B, C).mostCommonWithin(2, 99, 1).toList(),
            "Not included if null",
        )
        assertEquals(
            listOf(A, B),
            sequenceOf(A, B, A, B).mostCommonWithin(4, 99, 1).toList(),
            "Not duplicated if it was already among the most common",
        )
    }

    @Test fun `mostCommonWithin counts the right number of items`() {
        assertEquals(
            listOf(A, C),
            sequenceOf(A, B, C, C).mostCommonWithin(2, 4, 1).toList(),
            "Always counts the minimum",
        )
        assertEquals(
            listOf(A, B, C),
            sequenceOf(A, B, C, C).mostCommonWithin(3, 1, 1).toList(),
            "Counts more to find the target",
        )
        assertEquals(
            listOf(A, B),
            sequenceOf(A, null, B, null, C, C).mostCommonWithin(2, 3, 1).toList(),
            "Counts nulls towards the minimum",
        )
        assertEquals(
            listOf(A, B, C),
            sequenceOf(A, null, B, null, C, C).mostCommonWithin(3, 2, 1).toList(),
            "Doesn't count null toward the target",
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
