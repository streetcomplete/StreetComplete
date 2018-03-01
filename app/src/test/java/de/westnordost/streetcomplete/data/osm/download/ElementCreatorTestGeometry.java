package de.westnordost.streetcomplete.data.osm.download;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

public class ElementCreatorTestGeometry extends TestCase implements WayGeometrySource
{
	private static final LatLon
			P0 = new OsmLatLon(0d,2d),
			P1 = new OsmLatLon(0d,0d),
			P2 = new OsmLatLon(2d,0d),
			P3 = new OsmLatLon(2d,2d);

	private static final Node
			N0 =  new OsmNode(0L,0,P0,null),
			N1 =  new OsmNode(1L,0,P1,null),
			N2 =  new OsmNode(2L,0,P2,null),
			N3 =  new OsmNode(3L,0,P3,null);

	private static final Map<String,String>
			wayArea = new HashMap<>(),
			relationArea = new HashMap<>();
	static {
		wayArea.put("area", "yes");
		relationArea.put("type","multipolygon");
	}

	private static final Way
			W0 = new OsmWay(0L,0, Arrays.asList(0L,1L),null),
			W1 = new OsmWay(1L,0,Arrays.asList(1L,2L,0L),null),
			W2 = new OsmWay(2L,0,Arrays.asList(0L,1L,2L,0L),wayArea),
			W3 = new OsmWay(3L,0,Arrays.asList(3L,2L), null),
			W4 = new OsmWay(4L,0,Arrays.asList(0L,1L,1L,2L), null),
			W5 = new OsmWay(5L,0, Collections.emptyList(),null);

	private static final RelationMember
			RM0 = new OsmRelationMember(0L, "outer", Element.Type.WAY),
			RM1 = new OsmRelationMember(1L, "outer", Element.Type.WAY),
			RM2 = new OsmRelationMember(2L, "inner", Element.Type.WAY),
			RM3 = new OsmRelationMember(3L, "", Element.Type.WAY);

	private static final Relation
			R0 = new OsmRelation(0L, 0, Arrays.asList(RM0, RM1, RM3), relationArea),
			R1 = new OsmRelation(1L, 0, Arrays.asList(RM0, RM1, RM2, RM3), null);

	private static final List<Node> nodes = Arrays.asList(N0, N1, N2, N3);
	private static final List<Way> ways = Arrays.asList(W0, W1, W2, W3, W4, W5);

	@Override public List<LatLon> getNodePositions(long wayId)
	{
		List<Long> nodeIds = ways.get((int) wayId).getNodeIds();
		List<LatLon> result = new ArrayList<>();
		for(Long nodeId : nodeIds)
		{
			result.add(nodes.get(nodeId.intValue()).getPosition());
		}
		return result;
	}

	private ElementGeometryCreator createCreator()
	{
		ElementGeometryCreator creator = new ElementGeometryCreator();
		creator.setWayGeometryProvider(this);
		return creator;
	}

	public void testCreateForNode()
	{
		ElementGeometry geom = createCreator().create(N0);
		assertEquals(P0, geom.center);
	}

	public void testCreateForEmptyWay()
	{
		ElementGeometry geom = createCreator().create(W5);
		assertNull(geom);
	}

	public void testCreateForWayWithDuplicateNodes()
	{
		ElementGeometry geom = createCreator().create(W4);
		assertNotNull(geom.polylines);
		assertNotNull(geom.center);
	}

	public void testCreateForSimpleAreaWay()
	{
		ElementGeometry geom = createCreator().create(W2);
		assertNotNull(geom.polygons);
		assertEquals(1,geom.polygons.size());
		List<LatLon> polygon = geom.polygons.get(0);

		for(int i=0; i<W2.getNodeIds().size(); ++i)
		{
			LatLon shouldBe = nodes.get(W2.getNodeIds().get(i).intValue()).getPosition();
			assertEquals(shouldBe, polygon.get(i));
		}
	}

	public void testCreateForSimpleNonAreaWay()
	{
		ElementGeometry geom = createCreator().create(W0);
		assertNotNull(geom.polylines);
		assertEquals(1,geom.polylines.size());
		assertEquals(W0.getNodeIds().size(), geom.polylines.get(0).size());

		List<LatLon> polyline = geom.polylines.get(0);

		for(int i=0; i<W0.getNodeIds().size(); ++i)
		{
			LatLon shouldBe = nodes.get(W0.getNodeIds().get(i).intValue()).getPosition();
			assertEquals(shouldBe, polyline.get(i));
		}
	}

	public void testCreateForMultipolygonRelation()
	{
		ElementGeometry geom = createCreator().create(R0);

		assertNotNull(geom.polygons);
		assertEquals(1, geom.polygons.size());
	}

	public void testCreateForPolylineRelation()
	{
		ElementGeometry geom = createCreator().create(R1);

		assertNotNull(geom.polylines);
		assertEquals(3, geom.polylines.size());
	}
}
