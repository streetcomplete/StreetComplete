package de.westnordost.streetcomplete.data.osm.mapdata

import kotlin.test.Test
import kotlin.test.assertEquals

class MapDataApiSerializerTest {

    @Test fun `serializeMapDataChanges full`() {
        val osmChange = """
            <osmChange>
            <create>
            ${nodesOsm(1234)}
            ${waysOsm(1234)}
            </create>
            <modify>
            ${waysOsm(1234)}
            ${relationsOsm(1234)}
            </modify>
            <delete>
            ${nodesOsm(1234)}
            ${relationsOsm(1234)}
            </delete>
            </osmChange>
        """

        val mapDataChanges = MapDataChanges(
            creations = nodesList + waysList,
            modifications = waysList + relationsList,
            deletions = nodesList + relationsList,
        )

        assertEquals(
            osmChange.replace(Regex("[\n\r] *"), ""),
            MapDataApiSerializer().serializeMapDataChanges(mapDataChanges, 1234L)
        )
    }

}
