package de.westnordost.streetcomplete.tangram;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static org.junit.Assert.*;

public class TangramConstTest
{
	@Test public void convertSingle()
	{
		double lng = 10;
		double lat = 5;

		OsmLatLon pos = new OsmLatLon(lat,lng);
		LatLon pos2 = TangramConst.toLatLon(TangramConst.toLngLat(pos));

		assertEquals(pos, pos2);
	}

	@Test public void convertLists()
	{
		List<List<LatLon>> positionLists = new ArrayList<>();
		List<LatLon> positions1 = new ArrayList<>();
		positionLists.add(positions1);
		List<LatLon> positions2 = new ArrayList<>();
		positions2.add(new OsmLatLon(1,2));
		positions2.add(new OsmLatLon(3,4));
		positionLists.add(positions2);
		List<LatLon> positions3 = new ArrayList<>();
		positions3.add(new OsmLatLon(5,6));
		positionLists.add(positions3);

		List<List<LatLon>> positionLists2 = TangramConst.toLatLon(TangramConst.toLngLat(positionLists));

		assertEquals(positionLists, positionLists2);
	}
}
