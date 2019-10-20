package de.westnordost.streetcomplete.data.osm.download

import org.junit.Test

import java.io.ByteArrayInputStream
import java.io.InputStream

import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.osmapi.map.OsmMapDataFactory
import de.westnordost.osmapi.map.data.*

import org.junit.Assert.*

class OverpassMapDataParserTest {

    @Test fun node() {
        val pos = OsmLatLon(51.7463194, 0.2428181)
        val eg = parseOne("<node id='5' version='1' lat='${pos.latitude}' lon='${pos.longitude}'/>")

        val node = eg.element as Node
        assertEquals(pos, node.position)
        assertEquals(5, node.id)
        assertEquals(1, node.version)
        assertNull(node.tags)
        assertEquals(ElementGeometry(pos), eg.geometry)
    }

    @Test fun way() {
        val ps = listOf(OsmLatLon(1.0, 2.0), OsmLatLon(3.0, 4.0))
        val eg = parseOne("""
            <way id='8' version='1' >
              <nd ref='2' lat='${ps[0].latitude}' lon='${ps[0].longitude}' />
              <nd ref='3' lat='${ps[1].latitude}' lon='${ps[1].longitude}' />
            </way>
        """)

        val way = eg.element as Way
        assertEquals(8, way.id)
        assertEquals(1, way.version)
        assertEquals(listOf(2L,3L), way.nodeIds)
        assertEquals(
	        ElementGeometry(listOf(ps), null),
	        eg.geometry
        )
    }

    @Test fun relation() {
        val p = listOf(
            OsmLatLon(1.0, 2.0),
            OsmLatLon(3.0, 4.0),
            OsmLatLon(5.0, 6.0),
            OsmLatLon(7.0, 8.0), 
            OsmLatLon(9.0, 10.0))
        val eg = parseOne("""
            <relation id='10' version='1'>
             <member type='relation' ref='4' role=''/>
             <member type='way' ref='1' role='outer'>
              <nd lat='${p[0].latitude}' lon='${p[0].longitude}'/>
              <nd lat='${p[1].latitude}' lon='${p[1].longitude}'/>
             </member>
             <member type='way' ref='2' role='inner'>
              <nd lat='${p[2].latitude}' lon='${p[2].longitude}'/>
              <nd lat='${p[3].latitude}' lon='${p[3].longitude}'/>
             </member>
             <member type='node' ref='3' role='point'>
              <nd lat='${p[4].latitude}' lon='${p[4].longitude}'/>
             </member>
            </relation>
        """)

        val relation = eg.element as Relation
        assertEquals(10, relation.id)
        assertEquals(1, relation.version)
	    assertEquals(listOf(
		    OsmRelationMember(4, "", Element.Type.RELATION),
		    OsmRelationMember(1, "outer", Element.Type.WAY),
		    OsmRelationMember(2, "inner", Element.Type.WAY),
		    OsmRelationMember(3, "point", Element.Type.NODE)
	    ), relation.members)
        assertNull(relation.tags)
	    assertEquals(
		    ElementGeometry(listOf(p.subList(0,2), p.subList(2,4)), null),
		    eg.geometry
	    )
    }

    @Test fun tags() {
        val eg = parseOne("""
            <relation id='1' version='1' >
              <tag k='a' v='b'/>
              <tag k='c' v='d'/>
            </relation>
        """)

	    assertEquals(mapOf("a" to "b", "c" to "d"), eg.element.tags)
    }

    @Test fun skelInput() {
        val eg = parseOne("<node id='123' lat='12.345' lon='14.467'/>")

        assertEquals(-1, eg.element.version)
    }

    @Test fun parseSeveral() {
        val egs = parse("""
            <node id='1' version='1' lat='1' lon='4'/>
			<way id='1' version='1'>
			 <nd ref='2' lat='1' lon='3'/>
			 <nd ref='3' lat='2' lon='4'/>
			</way>
			<relation id='1' version='1'>
			 <member type='way' ref='2' role='inner'>
			  <nd lat='1' lon='3'/>
			  <nd lat='2' lon='4'/>
			 </member>
			</relation>
		""")

        assertEquals(3, egs.size)
    }

	private fun parse(xml: String): List<ElementWithGeometry> {
		val parser = OverpassMapDataParser(OsmMapDataFactory())
		val result = mutableListOf<ElementWithGeometry>()
		parser.setHandler { element, geometry ->
			result.add(ElementWithGeometry(element, geometry))
		}
		parser.parse(xml.toInputStream())
		return result
	}

    private fun parseOne(xml: String) = parse(xml).first()

    private data class ElementWithGeometry(val element:Element, val geometry: ElementGeometry?)

    private fun String.toInputStream(): InputStream =
	    ByteArrayInputStream(this.toByteArray(charset("UTF-8")))
}
