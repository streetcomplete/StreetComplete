package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.Letter.A
import de.westnordost.streetcomplete.util.Letter.B
import de.westnordost.streetcomplete.util.Letter.C
import kotlin.test.Test
import kotlin.test.assertEquals

class FavouritesTest {

    @Test
    fun `takeFavourites most common within functionality`() {
        assertEquals(
            listOf(A, C),
            listOf(A, A, B, C, C).takeFavourites(2),
            "Returns the most common items",
        )
        assertEquals(
            listOf(A, C, B),
            listOf(A, C, B, B, C).takeFavourites(4),
            "Sorted in the original order",
        )
        assertEquals(
            listOf(A),
            listOf(A, null).takeFavourites(2),
            "Doesn't return nulls",
        )
        assertEquals(
            listOf(A, B),
            listOf(A, null, null, B).takeFavourites(2),
            "Doesn't count nulls",
        )
    }

    @Test fun `takeFavourites first item(s) special case`() {
        assertEquals(
            listOf(A, B),
            listOf(A, B, B, C, C).takeFavourites(2, first = 1),
            "Included even if it is not the most common",
        )
        assertEquals(
            listOf(B, C),
            listOf(null, A, B, B, C, C).takeFavourites(2, first = 1),
            "Not included if null",
        )
        assertEquals(
            listOf(A, B),
            listOf(A, B, A, B).takeFavourites(4, first = 1),
            "Not duplicated if it was already among the most common",
        )
        assertEquals(
            listOf(A, B),
            listOf(A, B, C, C).takeFavourites(2, first = 2),
            "Take several",
        )
    }

    @Test fun `takeFavourites counts the right number of items`() {
        assertEquals(
            listOf(A, C),
            listOf(A, B, C, C).takeFavourites(2, 4),
            "Always counts the minimum",
        )
        assertEquals(
            listOf(A, B, C),
            listOf(A, B, C, C).takeFavourites(3, 1),
            "Counts more to find the target",
        )
        assertEquals(
            listOf(A, B),
            listOf(A, null, B, null, C, C).takeFavourites(2, 3),
            "Counts nulls towards the minimum",
        )
        assertEquals(
            listOf(A, B, C),
            listOf(A, null, B, null, C, C).takeFavourites(3, 2, 1),
            "Doesn't count null toward the target",
        )
    }

    @Test fun `takeFavourites pads result`() {
        assertEquals(
            listOf(A, B, C),
            listOf<Letter>().takeFavourites(3, pad = listOf(A, B, C)),
        )
        assertEquals(
            listOf(C, A, B),
            listOf(C).takeFavourites(3, pad = listOf(A, B, C)),
        )
        assertEquals(
            listOf(C),
            listOf(C).takeFavourites(1, pad = listOf(A, B)),
        )
    }
}

private enum class Letter { A, B, C }
