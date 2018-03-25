package de.westnordost.streetcomplete.quests.localized_name.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.data.ApplicationDbTestCase;

public class RoadNameSuggestionsDaoTest extends ApplicationDbTestCase
{
	private RoadNameSuggestionsDao dao;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		dao = new RoadNameSuggestionsDao(dbHelper, serializer);
	}

	public void testGetNoNames()
	{
		List<LatLon> positions = new ArrayList<>();
		positions.add(new OsmLatLon(5,5));
		List<Map<String, String>> result = dao.getNames(positions, 0);
		assertEquals(0, result.size());
	}

	public void testGetOneNames()
	{
		HashMap<String,String> names = new HashMap<>();
		names.put("de","Große Straße");
		names.put("en","Big Street");

		dao.putRoad(1, names, createRoadPositions());
		List<Map<String, String>> result = dao.getNames(createPosOnRoad(), 1000);

		assertEquals(1, result.size());
		assertTrue(result.get(0).equals(names));
	}

	public void testGetMultipleNames()
	{
		HashMap<String,String> names1 = new HashMap<>();
		names1.put("en","Big Street");
		dao.putRoad(1, names1, createRoadPositions());

		HashMap<String,String> names2 = new HashMap<>();
		names2.put("es","Calle Pequena");
		dao.putRoad(2, names2, createRoadPositions());

		List<Map<String, String>> result = dao.getNames(createPosOnRoad(), 1000);
		assertEquals(2, result.size());
		assertTrue(result.contains(names1));
		assertTrue(result.contains(names2));
	}

	private static ArrayList<LatLon> createRoadPositions()
	{
		ArrayList<LatLon> roadPositions = new ArrayList<>();
		roadPositions.add(new OsmLatLon(0,0));
		roadPositions.add(new OsmLatLon(0,0.0001));
		return roadPositions;
	}

	private static ArrayList<LatLon> createPosOnRoad()
	{
		ArrayList<LatLon> roadPositions = new ArrayList<>();
		roadPositions.add(new OsmLatLon(0,0.00005));
		return roadPositions;
	}
}
