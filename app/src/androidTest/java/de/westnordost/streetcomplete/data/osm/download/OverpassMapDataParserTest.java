package de.westnordost.streetcomplete.data.osm.download;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OverpassMapDataParserTest extends TestCase
{
	public void testNode()
	{
		LatLon pos = new OsmLatLon(51.7463194, 0.2428181);

		String xml = " <node id='5' version='1' lat='" + pos.getLatitude() +
				"' lon='" + pos.getLongitude() + "'/>";

		Element e = parseOne(xml, null);

		assertTrue(e instanceof Node);
		Node node = (Node) e;
		assertEquals(pos, node.getPosition());
		assertEquals(5, node.getId());
		assertEquals(1, node.getVersion());

		assertNull(node.getTags());
	}

	public void testWay()
	{
		OsmLatLon[] p = new OsmLatLon[2];
		p[0] = new OsmLatLon(1,2);
		p[1] = new OsmLatLon(3,4);

		String xml =
				"<way id='8' version='1' >\n" +
				" <nd ref='2' lat='" + p[0].getLatitude() + "' lon='"+p[0].getLongitude()+"' />\n" +
				" <nd ref='3' lat='" + p[1].getLatitude() + "' lon='"+p[1].getLongitude()+"' />\n" +
				"</way>";

		LongSparseArray<List<LatLon>> expectedGeometry = new LongSparseArray<>();
		expectedGeometry.put(8, new ArrayList<>(Arrays.asList(p)));

		Element e = parseOne(xml, expectedGeometry);

		assertTrue(e instanceof Way);
		Way way = (Way) e;

		assertEquals(8, way.getId());
		assertEquals(1, way.getVersion());

		assertEquals(2, way.getNodeIds().size());
		assertEquals(2, (long) way.getNodeIds().get(0));
		assertEquals(3, (long) way.getNodeIds().get(1));
	}

	public void testRelation()
	{
		OsmLatLon[] p = new OsmLatLon[5];
		p[0] = new OsmLatLon(1,2);
		p[1] = new OsmLatLon(3,4);
		p[2] = new OsmLatLon(5,6);
		p[3] = new OsmLatLon(7,8);
		p[4] = new OsmLatLon(9,10);

		String xml =
				"<relation id='10' version='1'>\n" +
				" <member type='relation' ref='4' role=''/>\n" +
				" <member type='way' ref='1' role='outer'>\n" +
				"  <nd lat='" + p[0].getLatitude() + "' lon='"+p[0].getLongitude()+"'/>\n" +
				"  <nd lat='" + p[1].getLatitude() + "' lon='"+p[1].getLongitude()+"'/>\n" +
				" </member>\n" +
				" <member type='way' ref='2' role='inner'>\n" +
				"  <nd lat='" + p[2].getLatitude() + "' lon='"+p[2].getLongitude()+"'/>\n" +
				"  <nd lat='" + p[3].getLatitude() + "' lon='"+p[3].getLongitude()+"'/>\n" +
				" </member>\n" +
				" <member type='node' ref='3' role='point'>\n" +
				"  <nd lat='" + p[4].getLatitude() + "' lon='"+p[4].getLongitude()+"'/>\n" +
				" </member>\n" +
				"</relation>";

		LongSparseArray<List<LatLon>> expectedGeometry = new LongSparseArray<>();
		expectedGeometry.put(1, new ArrayList<>(Arrays.asList(p[0], p[1])));
		expectedGeometry.put(2, new ArrayList<>(Arrays.asList(p[2], p[3])));

		Element e = parseOne(xml, expectedGeometry);

		assertTrue(e instanceof Relation);
		Relation relation = (Relation) e;

		assertEquals(10, relation.getId());
		assertEquals(1, relation.getVersion());

		assertEquals(4, relation.getMembers().size());
		RelationMember rm[] = new RelationMember[relation.getMembers().size()];
		relation.getMembers().toArray(rm);

		assertEquals(4, rm[0].getRef());
		assertEquals(Element.Type.RELATION, rm[0].getType());
		assertEquals("", rm[0].getRole());

		assertEquals(1, rm[1].getRef());
		assertEquals(Element.Type.WAY, rm[1].getType());
		assertEquals("outer", rm[1].getRole());

		assertEquals(2, rm[2].getRef());
		assertEquals(Element.Type.WAY, rm[2].getType());
		assertEquals("inner", rm[2].getRole());

		assertEquals(3, rm[3].getRef());
		assertEquals(Element.Type.NODE, rm[3].getType());
		assertEquals("point", rm[3].getRole());

		assertNull(relation.getTags());
	}

	public void testTags()
	{
		String xml =
				"<relation id='1' version='1' >\n" +
				" <tag k='a' v='b'/>" +
				" <tag k='c' v='d'/>" +
				"</relation>";

		Element element = parseOne(xml, null);

		assertNotNull(element.getTags());
		assertEquals(2, element.getTags().size());

		assertEquals("b", element.getTags().get("a"));
		assertEquals("d", element.getTags().get("c"));
	}

	public void testSkelInput()
	{
		String xml =
			"<node id='123' lat='12.345' lon='14.467'/>\n";

		Element element = parseOne(xml, null);
		assertEquals(-1, element.getVersion());
	}

	public void testParseSeveral() throws IOException
	{
		String xml =
				"<node id='1' version='1' lat='1' lon='4'/>\n" +
				"<way id='1' version='1'>\n" +
				" <nd ref='2' lat='1' lon='3'/>\n" +
				" <nd ref='3' lat='2' lon='4'/>\n" +
				"</way>\n" +
				"<relation id='1' version='1'>\n" +
				" <member type='way' ref='2' role='inner'>\n" +
				"  <nd lat='1' lon='3'/>\n" +
				"  <nd lat='2' lon='4'/>\n" +
				" </member>\n" +
				"</relation>";

		OverpassMapDataParser parser = new OverpassMapDataParser(
				new TestElementGeometryCreator(null), new OsmMapDataFactory());

		MapDataWithGeometryHandler mockHandler = mock(MapDataWithGeometryHandler.class);

		parser.setHandler(mockHandler);
		parser.parse(asInputStream(xml));

		verify(mockHandler, times(3)).handle(any(Element.class), isNull(ElementGeometry.class));
	}

	private Element parseOne(String xml, LongSparseArray<List<LatLon>> expectedGeometry)
	{
		SingleElementHandler handler = new SingleElementHandler();
		OverpassMapDataParser parser = new OverpassMapDataParser(
				new TestElementGeometryCreator(expectedGeometry), new OsmMapDataFactory());
		parser.setHandler(handler);
		try
		{
			parser.parse(asInputStream(xml));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return handler.element;
	}

	private class SingleElementHandler implements MapDataWithGeometryHandler
	{
		Element element;
		@Override public void handle(@NonNull Element element, @Nullable ElementGeometry geometry)
		{
			this.element = element;
		}
	}

	/** Does not actually create the geometry but only tests if the necessary data is available when
	 *  it's methods are called */
	private class TestElementGeometryCreator extends ElementGeometryCreator
	{
		private LongSparseArray<List<LatLon>> expectedGeometry;

		public TestElementGeometryCreator(LongSparseArray<List<LatLon>> expectedGeometry)
		{
			this.expectedGeometry = expectedGeometry;
		}

		@Override public ElementGeometry create(Node node)
		{
			return null;
		}

		@Override public ElementGeometry create(Way way)
		{
			if(expectedGeometry == null) return null;

			assertEquals(expectedGeometry.get(way.getId()), data.getNodePositions(way.getId()));

			return null;
		}

		@Override public ElementGeometry create(Relation relation)
		{
			if(expectedGeometry == null) return null;

			for(RelationMember rm : relation.getMembers())
			{
				if(rm.getType() == Element.Type.WAY)
				{
					assertEquals(expectedGeometry.get(rm.getRef()), data.getNodePositions(rm.getRef()));
				}
			}
			return null;
		}
	}

	private static InputStream asInputStream(String str)
	{
		try
		{
			return new ByteArrayInputStream(str.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException ignore) {}
		return null;
	}
}
