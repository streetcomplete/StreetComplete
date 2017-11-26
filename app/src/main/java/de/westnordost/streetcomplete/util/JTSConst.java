package de.westnordost.streetcomplete.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;

public class JTSConst
{
	private static final int WGS84 = 4326;
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	public static Geometry toGeometry(ElementGeometry e)
	{
		if(e.polygons != null)
		{
			ElementGeometry.Polygons p = e.getPolygonsOrderedByOrientation();
			return toPolygons(p.outer, p.inner);
		}
		else if (e.polylines != null)
		{
			return toLineStrings(e.polylines);
		}
		else
		{
			return toPoint(e.center);
		}
	}

	public static Geometry toPolygons(List<List<LatLon>> outer, List<List<LatLon>> inner)
	{
		Map<LinearRing, ArrayList<LinearRing>> shellsWithHoles = toShellsWithHoles(outer, inner);

		Polygon[] polys = new Polygon[shellsWithHoles.size()];
		int i = 0;
		for(Map.Entry<LinearRing, ArrayList<LinearRing>> shellWithHoles : shellsWithHoles.entrySet())
		{
			LinearRing shell = shellWithHoles.getKey();
			ArrayList<LinearRing> holesList = shellWithHoles.getValue();
			LinearRing[] holes = null;
			if(holesList != null)
			{
				holes = holesList.toArray(new LinearRing[holesList.size()]);

			}
			polys[i++] = factory.createPolygon(shell, holes);
		}
		if(polys.length == 1) return polys[0];
		else return factory.createMultiPolygon(polys);
	}

	public static Geometry toLineStrings(List<List<LatLon>> polylines)
	{
		LineString[] lineStrings = new LineString[polylines.size()];
		int  i = 0;
		for(List<LatLon> polyline : polylines)
		{
			lineStrings[i++] = factory.createLineString(toCoordinates(polyline));
		}
		if(lineStrings.length == 1) return lineStrings[0];
		else return factory.createMultiLineString(lineStrings);
	}

	private static Map<LinearRing, ArrayList<LinearRing>> toShellsWithHoles(
			List<List<LatLon>> outerPolygons, List<List<LatLon>> innerPolygons)
	{
		// outer -> List of inner
		Map<LinearRing, ArrayList<LinearRing>> shellsWithHoles = new HashMap<>();
		for (List<LatLon> outer : outerPolygons)
		{
			LinearRing shell = factory.createLinearRing(toCoordinates(outer));
			Geometry outerGeom = factory.createPolygon(shell);
			shellsWithHoles.put(shell, null);

			Iterator<List<LatLon>> it = innerPolygons.iterator();
			while(it.hasNext())
			{
				List<LatLon> inner = it.next();
				LinearRing hole = factory.createLinearRing(toCoordinates(inner));
				Geometry holeGeom = factory.createPolygon(hole);
				if(outerGeom.contains(holeGeom))
				{
					if(shellsWithHoles.get(shell) == null)
					{
						shellsWithHoles.put(shell, new ArrayList<>());
					}
					shellsWithHoles.get(shell).add(hole);
					it.remove();
				}
			}
		}
		return shellsWithHoles;
	}

	public static Coordinate[] toCoordinates(List<LatLon> latLons)
	{
		Coordinate[] result = new Coordinate[latLons.size()];
		int i = 0;
		for(LatLon latLon : latLons)
		{
			result[i++] = toCoordinate(latLon);
		}
		return result;
	}

	public static Coordinate toCoordinate(LatLon latLon)
	{
		return new Coordinate(latLon.getLongitude(), latLon.getLatitude());
	}

	public static LinearRing toLinearRing(BoundingBox bbox)
	{
		List<LatLon> corners = new ArrayList<>(5);
		corners.add(bbox.getMin());
		corners.add(new OsmLatLon(bbox.getMinLatitude(), bbox.getMaxLongitude()));
		corners.add(bbox.getMax());
		corners.add(new OsmLatLon(bbox.getMaxLatitude(), bbox.getMinLongitude()));
		corners.add(bbox.getMin());
		return factory.createLinearRing(toCoordinates(corners));
	}

	public static Point toPoint(LatLon latLon)
	{
		return factory.createPoint(toCoordinate(latLon));
	}

	public static LatLon toLatLon(Point p)
	{
		return new OsmLatLon(p.getY(), p.getX());
	}

	public static LatLon toLatLon(Coordinate c)
	{
		return new OsmLatLon(c.y, c.x);
	}
}
