package de.westnordost.streetcomplete.data

import org.junit.Assert.assertEquals
import org.junit.Test

internal class ObjectTypeRegistryTest {

    @Test(expected = IllegalArgumentException::class)
    fun `throws when one class is added twice`() {
        ObjectTypeRegistry<Any>(listOf(1 to A, 2 to A))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when one ordinal is added twice`() {
        ObjectTypeRegistry(listOf(1 to A, 1 to B))
    }

    @Test
    fun `initialize correctly`() {
        val registry = ObjectTypeRegistry(listOf(
            1 to A,
            0 to B,
            2 to C
        ))
        assertEquals(listOf(A, B, C), registry.toList())

        assertEquals(A, registry.getByName("A"))
        assertEquals(B, registry.getByName("B"))
        assertEquals(C, registry.getByName("C"))
        assertEquals(null, registry.getByName("D"))

        assertEquals(A, registry.getByOrdinal(1))
        assertEquals(B, registry.getByOrdinal(0))
        assertEquals(C, registry.getByOrdinal(2))
        assertEquals(null, registry.getByOrdinal(3))

        assertEquals(1, registry.getOrdinalOf(A))
        assertEquals(0, registry.getOrdinalOf(B))
        assertEquals(2, registry.getOrdinalOf(C))
        assertEquals(null, registry.getOrdinalOf(D))
    }

    @Test
    fun `holes in ordinals are okay`() {
        val registry = ObjectTypeRegistry(listOf(
            0 to A,
            10 to B
        ))
        assertEquals(A, registry.getByOrdinal(0))
        assertEquals(B, registry.getByOrdinal(10))

        assertEquals(0, registry.getOrdinalOf(A))
        assertEquals(10, registry.getOrdinalOf(B))
    }
}

private object A
private object B
private object C
private object D
