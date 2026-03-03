package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class MapDataApiSerializerTest {

    @Test fun `serialize full`() {
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
            MapDataApiSerializer().serialize(mapDataChanges, 1234L)
        )
    }

    // #6466
    @Test fun `serialize with XML character references`() {
        val mapDataChanges = MapDataChanges(
            modifications = listOf(
                Node(
                    id = 1,
                    position = LatLon(53.0, 9.0),
                    tags = mapOf("inscription" to "BRETT S. HALL\nJuly 4, 1962"),
                    version = 2,
                    timestampEdited = Instant.parse("2019-03-15T01:52:26Z").toEpochMilliseconds()
                )
            )
        )
        assertEquals(
            """
                <osmChange>
                <modify>
                <node id="1" version="2" changeset="1" timestamp="2019-03-15T01:52:26Z" lat="53.0" lon="9.0">
                <tag k="inscription" v="BRETT S. HALL&#xa;July 4, 1962" />
                </node>
                </modify>
                </osmChange>
            """.replace(Regex("[\n\r] *"), ""),
            MapDataApiSerializer().serialize(mapDataChanges, 1L)
        )
    }
}
