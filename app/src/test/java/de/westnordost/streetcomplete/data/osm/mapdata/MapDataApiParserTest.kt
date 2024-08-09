package de.westnordost.streetcomplete.data.osm.mapdata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapDataApiParserTest {

    @Test fun `parseMapData minimum`() {
        val empty = MapDataApiParser().parseMapData("<osm></osm>", emptySet())
        assertEquals(0, empty.size)
        assertNull(empty.boundingBox)
    }

    @Test fun `parseMapData full`() {
        val osm = """<?xml version="1.0" encoding="UTF-8"?>
            <osm version="0.6" generator="CGImap 0.9.2 (2448320 spike-08.openstreetmap.org)" copyright="OpenStreetMap and contributors" attribution="http://www.openstreetmap.org/copyright" license="http://opendatacommons.org/licenses/odbl/1-0/">
            <bounds minlat="53.0000000" minlon="9.0000000" maxlat="53.0100000" maxlon="9.0100000"/>
            ${nodesOsm(123)}
            ${waysOsm(345)}
            ${relationsOsm(567)}
            </osm>
        """

        val data = MapDataApiParser().parseMapData(osm, emptySet())
        assertEquals(nodesList.toSet(), data.nodes.toSet())
        assertEquals(waysList.toSet(), data.ways.toSet())
        assertEquals(relationsList.toSet(), data.relations.toSet())
        assertEquals(BoundingBox(53.0, 9.0, 53.01, 9.01), data.boundingBox)
    }

    @Test fun `parseMapData with ignored relation types`() {
        val osm = """
            <osm>
            <relation id="1" version="1" timestamp="2023-05-08T14:14:51Z">
              <tag k="type" v="route"/>
            </relation>
            </osm>
        """

        val empty = MapDataApiParser().parseMapData(osm, setOf("route"))
        assertEquals(0, empty.size)
    }

    @Test fun `parseElementUpdates minimum`() {
        assertEquals(
            mapOf(),
            MapDataApiParser().parseElementUpdates("<diffResult></diffResult>")
        )
    }

    @Test fun `parseElementUpdates full`() {
        val diffResult = """
            <diffResult generator="OpenStreetMap Server" version="0.6">
            <node old_id="1"/>
            <way old_id="2"/>
            <relation old_id="3"/>
            <node old_id="-1" new_id="9" new_version="99" />
            <way old_id="-2" new_id="8" new_version="88" />
            <relation old_id="-3" new_id="7" new_version="77" />
            </diffResult>
        """

        val elementUpdates = mapOf(
            ElementKey(ElementType.NODE, 1) to DeleteElement,
            ElementKey(ElementType.WAY, 2) to DeleteElement,
            ElementKey(ElementType.RELATION, 3) to DeleteElement,
            ElementKey(ElementType.NODE, -1) to UpdateElement(9, 99),
            ElementKey(ElementType.WAY, -2) to UpdateElement(8, 88),
            ElementKey(ElementType.RELATION, -3) to UpdateElement(7, 77),
        )

        assertEquals(
            elementUpdates,
            MapDataApiParser().parseElementUpdates(diffResult)
        )
    }
}
