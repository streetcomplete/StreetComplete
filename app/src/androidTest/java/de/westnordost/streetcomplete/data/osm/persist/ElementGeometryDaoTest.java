package de.westnordost.streetcomplete.data.osm.persist;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class ElementGeometryDaoTest extends ApplicationDbTestCase
{
	private ElementGeometryDao dao;

	@Override public void setUp() throws Exception
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

	public void testPutAll()
	{
		ElementGeometry geometry = createSimpleGeometry();
		ArrayList<ElementGeometryDao.Row> rows = new ArrayList<>();
		rows.add(new ElementGeometryDao.Row(Element.Type.NODE, 1, geometry));
		rows.add(new ElementGeometryDao.Row(Element.Type.WAY, 2, geometry));
		dao.putAll(rows);

		assertNotNull(dao.get(Element.Type.WAY, 2));
		assertNotNull(dao.get(Element.Type.NODE, 1));
	}

	public void testSimplePutGet()
	{
		ElementGeometry geometry = createSimpleGeometry();
		dao.put(Element.Type.NODE, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.NODE, 0);

		assertEquals(geometry, dbGeometry);
	}

	public void testPolylineGeometryPutGet()
	{
		List<List<LatLon>> polylines = new ArrayList<>();
		polylines.add(createSomeLatLons(0));

		ElementGeometry geometry = new ElementGeometry(polylines, null);
		dao.put(Element.Type.WAY, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.WAY, 0);

		assertEquals(geometry, dbGeometry);
	}

	public void testPolygonGeometryPutGet()
	{
		List<List<LatLon>> polygons = new ArrayList<>();
		polygons.add(createSomeLatLons(0));
		polygons.add(createSomeLatLons(10));

		ElementGeometry geometry = new ElementGeometry(polygons, null);
		dao.put(Element.Type.RELATION, 0, geometry);
		ElementGeometry dbGeometry = dao.get(Element.Type.RELATION, 0);

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

	private List<LatLon> createSomeLatLons(double start)
	{
		List<LatLon> result = new ArrayList<>(5);
		for(int i = 0; i < 5; ++i)
		{
			result.add(new OsmLatLon(start+i, start+i));
		}
		return result;
	}
}
