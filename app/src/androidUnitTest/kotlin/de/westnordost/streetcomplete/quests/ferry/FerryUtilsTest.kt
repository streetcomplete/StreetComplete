package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.rel
import kotlin.test.Test
import kotlin.test.assertEquals

class FerryUtilsTest {

    @Test
    fun wayIdsInFerryRoutes() {
        assertEquals(
            emptySet(),
            wayIdsInFerryRoutes(emptyList())
        )

        assertEquals(
            emptySet(),
            wayIdsInFerryRoutes(listOf(
                rel(
                    id = 1L,
                    members = listOf(member(ElementType.WAY, 5L)),
                    tags = mapOf("route" to "bus")
                )
            ))
        )

        assertEquals(
            setOf(5L, 6L),
            wayIdsInFerryRoutes(
                listOf(
                    rel(
                        id = 1L,
                        members = listOf(member(ElementType.WAY, 5L)),
                        tags = mapOf("route" to "ferry")
                    ),
                    rel(
                        id = 2L,
                        members = listOf(
                            member(ElementType.WAY, 6L),
                            member(ElementType.NODE, 7L),
                            member(ElementType.RELATION, 8L),
                        ),
                        tags = mapOf("route" to "ferry")
                    )
                )
            )
        )
    }
}
