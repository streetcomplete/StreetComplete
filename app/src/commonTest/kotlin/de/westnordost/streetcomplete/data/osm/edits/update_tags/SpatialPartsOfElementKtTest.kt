package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.*
import kotlin.test.Test

internal class SpatialPartsOfElementKtTest {

    @Test
    fun `node moved too much`() {
        val n1 = node(pos = p(0.0, 0.0))
        val n2 = n1.copy(position = n1.position.translate(50.0, 0.0))
        assertTrue(isGeometrySubstantiallyDifferent(n1, n2))
    }

    @Test
    fun `node moved only a little`() {
        val n1 = node(pos = p(0.0, 0.0))
        val n2 = n1.copy(position = n1.position.translate(10.0, 0.0))
        assertFalse(isGeometrySubstantiallyDifferent(n1, n2))
    }

    @Test
    fun `way was extended or shortened at start`() {
        assertTrue(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(1, 2, 3)),
            way(nodes = listOf(0, 1, 2, 3))
        ))

        assertTrue(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(0, 1, 2, 3)),
            way(nodes = listOf(1, 2, 3))
        ))
    }

    @Test
    fun `way was extended or shortened at end`() {
        assertTrue(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(1, 2, 3)),
            way(nodes = listOf(1, 2, 3, 0))
        ))

        assertTrue(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(1, 2, 3, 0)),
            way(nodes = listOf(1, 2, 3))
        ))
    }

    @Test
    fun `way was extended or shortened in this app`() {
        assertFalse(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(-1, 1, 2, 3)),
            way(nodes = listOf(1, 2, 3))
        ))
        assertFalse(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(1, 2, 3, -1)),
            way(nodes = listOf(1, 2, 3))
        ))
    }

    @Test
    fun `way geometry was not changed`() {
        assertFalse(isGeometrySubstantiallyDifferent(
            way(nodes = listOf(1, 2, 3)),
            way(nodes = listOf(1, 2, 3))
        ))
    }

    @Test
    fun `relation members were added or removed`() {
        assertTrue(isGeometrySubstantiallyDifferent(
            rel(members = listOf(member(ElementType.NODE, 1))),
            rel(members = listOf(member(ElementType.NODE, 1), member(ElementType.NODE, 2)))
        ))

        assertTrue(isGeometrySubstantiallyDifferent(
            rel(members = listOf(member(ElementType.NODE, 1), member(ElementType.NODE, 2))),
            rel(members = listOf(member(ElementType.NODE, 1)))
        ))
    }

    @Test
    fun `order of relation members changed`() {
        assertTrue(isGeometrySubstantiallyDifferent(
            rel(members = listOf(member(ElementType.NODE, 1), member(ElementType.NODE, 2))),
            rel(members = listOf(member(ElementType.NODE, 2), member(ElementType.NODE, 1)))
        ))
    }

    @Test
    fun `role of any relation member changed`() {
        assertTrue(isGeometrySubstantiallyDifferent(
            rel(members = listOf(member(ElementType.NODE, 1, "a"))),
            rel(members = listOf(member(ElementType.NODE, 1, "b")))
        ))
    }

    @Test
    fun `relation geometry was not changed`() {
        assertFalse(isGeometrySubstantiallyDifferent(
            rel(members = listOf(member(ElementType.NODE, 1, "a"))),
            rel(members = listOf(member(ElementType.NODE, 1, "a")))
        ))
    }
}
