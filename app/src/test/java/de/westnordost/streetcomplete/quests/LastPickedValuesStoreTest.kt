package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.quests.Letter.*
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.view.image_select.Item
import org.junit.Assert.assertEquals
import org.junit.Test


class LastPickedValuesStoreTest {

    @Test fun `weighted sort returns the default items when there is no history`() {
        val allItems = Letter.values().toList().map { Item(it) }
        val defaultItems = listOf(A, B, C).map { Item(it) }

        val favs = mock<LastPickedValuesStore<Letter>>()
        on(favs.get(javaClass.simpleName)).thenReturn(sequenceOf())

        val returnedItems = favs.getWeighted(javaClass.simpleName, 4, 99, defaultItems, allItems)
        assertEquals(defaultItems, returnedItems)
    }

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
}

private enum class Letter { A, B, C, D }
