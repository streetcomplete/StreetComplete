package de.westnordost.streetcomplete.data.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class GeoJsonReader
{
	private static final int WGS84 = 4326;

	private static final String
			TYPE = "type",
			FEATURES = "features",
			COORDINATES = "coordinates",
			GEOMETRIES = "geometries",
			GEOMETRY = "geometry",
			PROPERTIES = "properties";

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	public Geometry read(String string) throws GeoJsonException
	{
		try
		{
			JSONObject json = new JSONObject(string);
			String type = json.getString(TYPE);
			switch(type)
			{
				case "Feature":
					return createFeature(json);
				case "FeatureCollection":
					Geometry[] features = createFeatures(json.getJSONArray(FEATURES));
					return factory.createGeometryCollection(features);
				default:
					return createGeometry(json);
			}
		}
		catch(JSONException e)
		{
			throw new GeoJsonException(e);
		}
	}

	private Map<String, String> createProperties(JSONObject properties) throws JSONException
	{
		if(properties.length() == 0) return null;

		Map<String,String> result = new HashMap<>(properties.length());
		Iterator<String> keys = properties.keys();
		while(keys.hasNext())
		{
			String key = keys.next();
			String value = properties.getString(key);
			result.put(key, value);
		}
		return result;
	}

	private Geometry[] createFeatures(JSONArray features) throws JSONException
	{
		Geometry[] geometries = new Geometry[features.length()];
		for (int i = 0; i < features.length(); ++i)
		{
			JSONObject feature = features.getJSONObject(i);
			geometries[i] = createFeature(feature);
		}
		return geometries;
	}

	private Geometry createFeature(JSONObject feature) throws JSONException
	{
		Map<String, String> propertiesMap = createProperties(feature.getJSONObject(PROPERTIES));

		Geometry geometry;
		if(!feature.isNull(GEOMETRY))
		{
			geometry = createGeometry(feature.getJSONObject(GEOMETRY));
		}
		else
		{
			geometry = factory.createGeometryCollection(null);
		}

		geometry.setUserData(propertiesMap);
		return geometry;
	}

	private Geometry createGeometry(JSONObject geo) throws JSONException
	{
		String type = geo.getString(TYPE);

		switch (type)
		{
			case "Point":				return createPoint(geo.getJSONArray(COORDINATES));
			case "LineString":			return createLineString(geo.getJSONArray(COORDINATES));
			case "MultiPoint":			return createMultiPoint(geo.getJSONArray(COORDINATES));
			case "MultiLineString":		return createMultiLineString(geo.getJSONArray(COORDINATES));
			case "Polygon":				return createPolygon(geo.getJSONArray(COORDINATES));
			case "MultiPolygon":		return createMultiPolygon(geo.getJSONArray(COORDINATES));
			case "GeometryCollection":	return createGeometryCollection(geo.getJSONArray(GEOMETRIES));
			default:
				throw new GeoJsonException("Unsupported type '"+type+"'");
		}
	}

	private GeometryCollection createGeometryCollection(JSONArray geometries) throws JSONException
	{
		if(geometries.length() == 0) return factory.createGeometryCollection(null);

		Geometry[] result = new Geometry[geometries.length()];
		for (int i = 0; i < geometries.length(); i++)
		{
			JSONObject geometry = geometries.getJSONObject(i);
			result[i] = createGeometry(geometry);
		}
		return factory.createGeometryCollection(result);
	}

	private MultiPolygon createMultiPolygon(JSONArray coords) throws JSONException
	{
		ArrayList<Polygon> polygons = new ArrayList<>(coords.length());
		for (int i = 0; i < coords.length(); i++)
		{
			polygons.add(createPolygon(coords.getJSONArray(i)));
		}

		/* JTS MultiPolygon imposes a restriction on the contained Polygons that a GeoJson
		 * MultiPolygon does not impose. From the JTS documentation:
		 * "As per the OGC SFS specification, the Polygons in a MultiPolygon may not overlap, and
		 *  may only touch at single points. This allows the topological point-set semantics to be
		 *  well-defined."*/
		mergePolygons(polygons);

		return factory.createMultiPolygon(polygons.toArray(new Polygon[]{}));
	}

	private void mergePolygons(ArrayList<Polygon> polygons)
	{
		if(polygons.size() == 1) return;

		long time = System.currentTimeMillis();

		for(int i1 = 0; i1 < polygons.size() - 1; ++i1)
		{
			Polygon p1 = polygons.get(i1);
			for(int i2 = i1+1; i2 < polygons.size(); ++i2)
			{
				Polygon p2 = polygons.get(i2);
				// Geometry.union() seems to not have this optimization (bbox check)
				if (!p1.getEnvelopeInternal().intersects(p2.getEnvelopeInternal())) continue;

				Geometry p1p2Union = p1.union(p2);
				// if p1 and p2 wouldn't intersect, p1p2Union would be a GeometryCollection or MultiPolygon
				if(p1p2Union instanceof Polygon)
				{
					polygons.remove(i2);
					polygons.set(i1, (Polygon) p1p2Union);
					--i1; // start again at i1
					break;
				}
			}
		}
	}

	private Polygon createPolygon(JSONArray coords) throws JSONException
	{
		LinearRing[] linearRings = new LinearRing[coords.length()];
		for (int i = 0; i < coords.length(); i++)
		{
			linearRings[i] = createLinearRing(coords.getJSONArray(i));
		}

		LinearRing shell = linearRings[0];
		ArrayList<LinearRing> inner = new ArrayList<>();
		if(linearRings.length > 1)
		{
			LinearRing[] innerArray = new LinearRing[linearRings.length-1];
			System.arraycopy(linearRings, 1, innerArray, 0, linearRings.length - 1);
			inner.addAll(Arrays.asList(innerArray));

			/* JTS imposes a restriction on a polygon that GeoJSON does not: that the linear rings
			   that define the holes may not touch each other in more than one point. So, we need to
			   merge inner linear rings that touch each other in a line together */
			mergeHoles(inner);
		}

		Polygon polygon = factory.createPolygon(shell, inner.toArray(new LinearRing[]{}));
		/* in JTS, outer shells are clockwise but in GeoJSON it is specified to be the other way
		   round. This reader is forgiving: It does not care about the direction, it will just
		   reorder if necessary (part of normalize) */
		polygon.normalize();
		return polygon;
	}

	private void mergeHoles(ArrayList<LinearRing> rings)
	{
		if(rings.size() == 1) return;

		// need to be converted to polygons and back because linearring is a lineal data structure,
		// we want to merge by area
		ArrayList<Polygon> polygons = new ArrayList<>(rings.size());
		for (LinearRing ring : rings)
		{
			polygons.add(factory.createPolygon(ring));
		}
		mergePolygons(polygons);

		// something was merged. Convert polygons back to rings
		if(polygons.size() != rings.size())
		{
			rings.clear();
			for (Polygon polygon : polygons)
			{
				rings.add((LinearRing) polygon.getExteriorRing());
			}
		}
	}

	private MultiLineString createMultiLineString(JSONArray coords) throws JSONException
	{
		LineString[] lineStrings = new LineString[coords.length()];
		for (int i = 0; i < coords.length(); i++)
		{
			lineStrings[i] = createLineString(coords.getJSONArray(i));
		}
		return factory.createMultiLineString(lineStrings);
	}

	private LinearRing createLinearRing(JSONArray coords) throws JSONException, GeoJsonException
	{
		if(coords.length() < 4)
		{
			throw new GeoJsonException("There must be at least four coordinates for a LinearRing");
		}
		Coordinate[] coordinates = createCoordinates(coords);
		if(!coordinates[0].equals(coordinates[coordinates.length-1]))
		{
			throw new GeoJsonException("The first and last coordinate need to be the same in a LinearRing");
		}
		return factory.createLinearRing(coordinates);
	}

	private LineString createLineString(JSONArray coords) throws JSONException, GeoJsonException
	{
		if(coords.length() < 2)
		{
			throw new GeoJsonException("There must be at least two coordinates for a LineString");
		}
		return factory.createLineString(createCoordinates(coords));
	}

	private MultiPoint createMultiPoint(JSONArray coords) throws JSONException
	{
		return factory.createMultiPoint(createCoordinates(coords));
	}

	private Point createPoint(JSONArray coords) throws JSONException
	{
		return factory.createPoint(createCoordinate(coords));
	}

	private Coordinate[] createCoordinates(JSONArray coords) throws JSONException
	{
		Coordinate[] result = new Coordinate[coords.length()];
		for (int i = 0; i < coords.length(); i++)
		{
			result[i] = createCoordinate(coords.getJSONArray(i));
		}
		return result;
	}

	private Coordinate createCoordinate(JSONArray coord) throws JSONException
	{
		double x = coord.getDouble(0);
		double y = coord.getDouble(1);
		double z = coord.length() > 2 ? coord.getDouble(2) : Double.NaN;
		return new Coordinate(x,y,z);
	}
}
