package de.westnordost.streetcomplete.data.meta;

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
			GEOMETRIES = "geometries";

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	public Geometry read(String string)
	{
		try
		{
			JSONObject json = new JSONObject(string);
			String type = json.getString(TYPE);
			switch(type)
			{
				case "GeometryCollection":
					return createGeometryCollection(json.getJSONArray(GEOMETRIES));
				case "Feature":
					return createFeature(json);
				case "FeatureCollection":
					return factory.createGeometryCollection(createFeatures(json.getJSONArray(FEATURES)));
				default:
					throw new JSONException("Unsupported type '"+type+"'");
			}
		}
		catch (JSONException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> createProperties(JSONObject properties) throws JSONException
	{
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

	private Geometry createFeature(JSONObject feature) throws JSONException
	{
		Map<String, String> propertiesMap = createProperties(feature.getJSONObject("properties"));

		Geometry geometry;
		if(!feature.isNull("geometry"))
		{
			geometry = createGeometry(feature.getJSONObject("geometry"));
		}
		else
		{
			geometry = factory.createGeometryCollection(null);
		}

		geometry.setUserData(propertiesMap);
		return geometry;
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

	private Geometry createGeometry(JSONObject geo) throws JSONException
	{
		String type = geo.getString(TYPE);

		switch (type)
		{
			case "Point":				return createPoint(geo.getJSONArray(COORDINATES));
			case "LineString":			return createLineString(geo.getJSONArray(COORDINATES));
			case "Polygon":				return createPolygon(geo.getJSONArray(COORDINATES));
			case "MultiPoint":			return createMultiPoint(geo.getJSONArray(COORDINATES));
			case "MultiLineString":		return createMultiLineString(geo.getJSONArray(COORDINATES));
			case "MultiPolygon":		return createMultiPolygon(geo.getJSONArray(COORDINATES));
			case "GeometryCollection":	return createGeometryCollection(geo.getJSONArray(GEOMETRIES));
			default:
				throw new JSONException("Unsupported type '"+type+"'");
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
		Polygon[] polygons = new Polygon[coords.length()];
		for (int i = 0; i < coords.length(); i++)
		{
			polygons[i] = createPolygon(coords.getJSONArray(i));
		}
		return factory.createMultiPolygon(polygons);
	}

	private Polygon createPolygon(JSONArray coords) throws JSONException
	{
		LinearRing[] linearRings = new LinearRing[coords.length()];
		for (int i = 0; i < coords.length(); i++)
		{
			linearRings[i] = createLinearRing(coords.getJSONArray(i));
		}

		LinearRing shell = linearRings[0];
		LinearRing[] inner = null;
		if(linearRings.length > 1)
		{
			inner = new LinearRing[linearRings.length-1];
			System.arraycopy(linearRings, 1, inner, 0, linearRings.length - 1);
		}

		return factory.createPolygon(shell, inner);
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

	private LinearRing createLinearRing(JSONArray coords) throws JSONException
	{
		if(coords.length() < 3)
		{
			throw new JSONException("There must be at least three coordinates for a LinearRing");
		}
		Coordinate[] coordinates = createCoordinates(coords);
		if(!coordinates[0].equals(coordinates[coordinates.length-1]))
		{
			throw new JSONException("The first and last coordinate need to be the same in a LinearRing");
		}
		return factory.createLinearRing(coordinates);
	}

	private LineString createLineString(JSONArray coords) throws JSONException
	{
		if(coords.length() < 2)
		{
			throw new JSONException("There must be at least two coordinates for a LineString");
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
