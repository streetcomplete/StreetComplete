package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.quests.Letter.*
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.view.image_select.Item
import java.util.LinkedList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class LastPickedValuesStoreTest {

    private lateinit var favs: LastPickedValuesStore<Letter>
    private val key: String = javaClass.simpleName

    private val allItems = Letter.values().toList().toItems()
    private val defaultItems = listOf(X, Y, Z).toItems()

    @Before fun setUp() {
        favs = mock()
    }

    @Test fun `weighted sort returns the default items when there is no history`() {
        on(favs.get(key)).thenReturn(linkedListOf())
        val returnedItems = favs.getWeighted(key, 4, 99, defaultItems, allItems)
        assertEquals(defaultItems, returnedItems)
    }

    @Test fun `weighted sort considers frequency first, then recency, then defaults`() {
        on(favs.get(key)).thenReturn(linkedListOf("A", "C", "B", "B", "C", "D"))
        val returnedItems = favs.getWeighted(key, 6, 99, defaultItems, allItems)
        val expectedItems = listOf(C, B, A, D, X, Y).toItems()
        assertEquals(expectedItems, returnedItems)
    }

    @Test fun `weighted sort returns most recent item even if it is not the most picked`() {
        on(favs.get(key)).thenReturn(linkedListOf("A", "B", "B", "B", "C", "C"))
        val returnedItems = favs.getWeighted(key, 2, 99, defaultItems, allItems)
        val expectedItems = listOf(B, A).toItems()
        assertEquals(expectedItems, returnedItems)
    }

    @Test fun `weighted sort doesn't return duplicates`() {
        on(favs.get(key)).thenReturn(linkedListOf("X", "Y", "X", "A"))
        val returnedItems = favs.getWeighted(key, 4, 99, defaultItems, allItems)
        val expectedItems = listOf(X, Y, A, Z).toItems()
        assertEquals(expectedItems, returnedItems)
    }

    @Test fun `weighted sort only returns items in itemPool (the most recent is not exempt)`() {
        on(favs.get(key)).thenReturn(linkedListOf("p", "B", "q", "C"))
        val returnedItems = favs.getWeighted(key, 2, 99, defaultItems, allItems)
        val expectedItems = listOf(B, C).toItems()
        assertEquals(expectedItems, returnedItems)
    }

    @Test fun `weighted sort still counts non-itemPool values in the history window`() {
        on(favs.get(key)).thenReturn(linkedListOf("A", "p", "q", "B", /**/ "B", "C", "D"))
        val returnedItems = favs.getWeighted(key, 2, 4, defaultItems, allItems)
        val expectedItems = listOf(A, B).toItems()
        assertEquals(expectedItems, returnedItems)
    }

    @Test fun `weighted sort extends the history window (only) as needed to find enough items`() {
        on(favs.get(key)).thenReturn(linkedListOf("A", "p", "q", "B", "B", "C", /**/ "D"))
        val returnedItems = favs.getWeighted(key, 3, 4, defaultItems, allItems)
        val expectedItems = listOf(B, A, C).toItems()
        assertEquals(expectedItems, returnedItems)
    }
}

private enum class Letter { A, B, C, D, X, Y, Z }
private fun List<Letter>.toItems(): List<Item<Letter>> = this.map(::Item)

private fun <T> linkedListOf(vararg items: T): LinkedList<T> = LinkedList(items.toList())

