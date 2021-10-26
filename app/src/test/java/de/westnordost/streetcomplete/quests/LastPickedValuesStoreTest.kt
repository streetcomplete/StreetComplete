package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.quests.Letter.*
import org.junit.Assert.assertEquals
import org.junit.Test


class LastPickedValuesStoreTest {

    @Test fun `mostCommonWithin sorts by frequency first, then recency`() {
        val items = sequenceOf(A, C, B, B, C, D)
        assertEquals(items.mostCommonWithin(4, 99).toList(), listOf(C, B, A, D))
    }

    @Test fun `mostCommonWithin includes the most recent item even if it is not the most common`() {
        val items = sequenceOf(A, B, B, B, C, C)
        assertEquals(items.mostCommonWithin(2, 99).toList(), listOf(B, A))
    }

    @Test fun `mostCommonWithin doesn't return duplicates`() {
        val items = sequenceOf(A, B, A, B)
        assertEquals(items.mostCommonWithin(4, 99).toList(), listOf(A, B))
    }

    @Test fun `mostCommonWithin doesn't include the first item if it's null`() {
        val items = sequenceOf(null, B, null, C)
        assertEquals(items.mostCommonWithin(2, 99).toList(), listOf(B, C))
    }

    @Test fun `mostCommonWithin includes nulls in the number of items to count`() {
        val items = sequenceOf(A, null, null, B, /* stops here */ B, C, D)
        assertEquals(items.mostCommonWithin(2, 4).toList(), listOf(A, B))
    }

    @Test fun `mostCommonWithin keeps counting until enough non-null items have been found`() {
        val items = sequenceOf(A, null, null, B, B, C, /* stops here */ D)
        assertEquals(items.mostCommonWithin(3, 4).toList(), listOf(B, A, C))
    }

    @Test fun `padWith doesn't include duplicates`() {
        val items = sequenceOf(A, B).padWith(listOf(B, C, D, A))
        assertEquals(items.toList(), listOf(A, B, C, D))
    }
}

private enum class Letter { A, B, C, D }
