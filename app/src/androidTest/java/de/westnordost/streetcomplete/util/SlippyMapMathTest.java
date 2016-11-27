package de.westnordost.streetcomplete.util;

import android.graphics.Point;
import android.graphics.Rect;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class SlippyMapMathTest extends TestCase
{
	public void testForthAndBack()
	{
		LatLon p = new OsmLatLon(53.0,9.0);
		Point tile = SlippyMapMath.enclosingTile(p, 15);
		BoundingBox bbox = SlippyMapMath.asBoundingBox(tile, 15);

		assertTrue(bbox.getMinLatitude() <= p.getLatitude());
		assertTrue(bbox.getMaxLatitude() >= p.getLatitude());
		assertTrue(bbox.getMinLongitude() <= p.getLongitude());
		assertTrue(bbox.getMaxLongitude() >= p.getLongitude());

		Rect r = SlippyMapMath.enclosingTiles(bbox, 15);
		BoundingBox bbox2 = SlippyMapMath.asBoundingBox(r, 15);

		assertEquals(bbox, bbox2);


	}

	public void testAsTileList()
	{
		List<Point> ps = new ArrayList<>();
		ps.add(new Point(1,1));
		ps.add(new Point(2,1));
		ps.add(new Point(1,2));
		ps.add(new Point(2,2));

		assertEquals(ps, SlippyMapMath.asTileList(new Rect(1,1,2,2)));
	}
}
