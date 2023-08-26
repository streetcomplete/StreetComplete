package de.westnordost.streetcomplete.data.urlconfig

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class OrdinalsKtTest {

    @Test fun `ordinals to boolean array`() {
        assertContentEquals(
            booleanArrayOf(),
            Ordinals(setOf()).toBooleanArray()
        )

        assertContentEquals(
            booleanArrayOf(0, 1),
            Ordinals(setOf(1)).toBooleanArray()
        )

        assertContentEquals(
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
        assertContentEquals(
            booleanArrayOf(),
            0.toBigInteger().toBooleanArray()
        )

        assertContentEquals(
            booleanArrayOf(0, 0, 1),
            4.toBigInteger().toBooleanArray()
        )

        assertContentEquals(
            booleanArrayOf(0, 0, 1, 0, 1),
            20.toBigInteger().toBooleanArray()
        )
    }
}

private fun booleanArrayOf(vararg ints: Int): BooleanArray =
    ints.map { it != 0 }.toBooleanArray()
