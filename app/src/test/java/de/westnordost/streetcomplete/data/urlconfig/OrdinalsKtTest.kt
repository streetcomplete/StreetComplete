package de.westnordost.streetcomplete.data.urlconfig

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

internal class OrdinalsKtTest {

    @Test fun `ordinals to boolean array`() {
        assertArrayEquals(
            booleanArrayOf(),
            Ordinals(setOf()).toBooleanArray()
        )

        assertArrayEquals(
            booleanArrayOf(0, 1),
            Ordinals(setOf(1)).toBooleanArray()
        )

        assertArrayEquals(
            booleanArrayOf(1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1),
            Ordinals(setOf(0, 1, 4, 10)).toBooleanArray()
        )
    }

    @Test fun `boolean array to ordinals`() {
        assertEquals(
            Ordinals(setOf()),
            booleanArrayOf().toOrdinals()
        )

        assertEquals(
            Ordinals(setOf(1)),
            booleanArrayOf(0, 1).toOrdinals()
        )

        assertEquals(
            Ordinals(setOf(0, 1, 4, 10)),
            booleanArrayOf(1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1).toOrdinals()
        )
    }

    @Test fun `boolean array to big integer`() {
        assertEquals(
            0.toBigInteger(),
            booleanArrayOf().toBigInteger()
        )

        assertEquals(
            4.toBigInteger(),
            booleanArrayOf(0, 0, 1).toBigInteger()
        )

        assertEquals(
            4.toBigInteger(),
            booleanArrayOf(0, 0, 1, 0).toBigInteger()
        )
    }

    @Test fun `big integer to boolean array`() {
        assertArrayEquals(
            booleanArrayOf(),
            0.toBigInteger().toBooleanArray()
        )

        assertArrayEquals(
            booleanArrayOf(0, 0, 1),
            4.toBigInteger().toBooleanArray()
        )

        assertArrayEquals(
            booleanArrayOf(0, 0, 1, 0, 1),
            20.toBigInteger().toBooleanArray()
        )
    }
}

private fun booleanArrayOf(vararg ints: Int): BooleanArray =
    ints.map { it != 0 }.toBooleanArray()
