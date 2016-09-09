package de.westnordost.osmagent.quests.osm.persist;

import org.mockito.Mockito;

import java.util.ArrayList;

import de.westnordost.osmagent.quests.OsmagentDbTestCase;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class ElementGeometryDaoTest extends OsmagentDbTestCase
{
	private ElementGeometryDao dao;

	@Override public void setUp()
	{
		super.setUp();
		dao = new ElementGeometryDao(dbHelper, serializer);
	}

	public void testGetNull()
	{
		assertNull(dao.get(Element.Type.NODE, 0));
	}

	public void testGetNullDifferentPKey()
	{
		dao.put(Element.Type.NODE, 0, createSimpleGeometry());
		assertNull(dao.get(Element.Type.WAY, 0));
		assertNull(dao.get(Element.Type.NODE, 1));
	}

	public void testSimplePutGet()
	{
		ElementGeometry geometry = createSimpleGeometry();
		dao.put(Element.Type.NODE, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.NODE, 0);

		assertEquals(geometry, dbGeometry);
	}

	public void testWayGeometryPutGet()
	{
		ElementGeometry geometry = new ElementGeometry(createSomeLatLons(0));
		dao.put(Element.Type.WAY, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.WAY, 0);

		assertEquals(geometry, dbGeometry);
	}

	public void testComplexGeometryPutGet()
	{
		ArrayList<ArrayList<LatLon>> outer = new ArrayList<>();
		outer.add(createSomeLatLons(0));
		outer.add(createSomeLatLons(10));

		ArrayList<ArrayList<LatLon>> inner = new ArrayList<>();
		inner.add(createSomeLatLons(5));

		ElementGeometry geometry = new ElementGeometry(outer, inner);
		dao.put(Element.Type.WAY, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.WAY, 0);

		assertEquals(geometry, dbGeometry);
	}

	public void testDeleteUnreferenced()
	{
		Element.Type type = Element.Type.WAY;
		long id = 0;
		ElementGeometry geometry = createSimpleGeometry();

		dao.put(type, id, geometry);
		assertEquals(1,dao.deleteUnreferenced());

		dao.put(type, id, geometry);
		new OsmQuestDao(dbHelper, serializer, null).add(
				new OsmQuest(Mockito.mock(OsmElementQuestType.class), type, id, geometry));
		assertEquals(0, dao.deleteUnreferenced());
	}

	private ElementGeometry createSimpleGeometry()
	{
		return new ElementGeometry(new OsmLatLon(50,50));
	}

	private ArrayList<LatLon> createSomeLatLons(double start)
	{
		ArrayList<LatLon> result = new ArrayList<>(5);
		for(int i = 0; i < 5; ++i)
		{
			result.add(new OsmLatLon(start+i, start+i));
		}
		return result;
	}
}
