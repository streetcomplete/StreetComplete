package de.westnordost.streetcomplete.data.meta;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.util.JTSConst;

public class CountryBoundaries
{
	private static String ISO3166_1_ALPHA2 = "ISO3166-1:alpha2";
	private static String ISO3166_2 = "ISO3166-2";
	private static final int WGS84 = 4326;

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);
	private final SpatialIndex index;
	private final Map<String, Geometry> geometriesByIsoCodes;
	private final Map<Geometry, Double> geometrySizeCache;

	public CountryBoundaries(GeometryCollection countriesBoundaries)
	{
		index = new STRtree();
		geometrySizeCache = new HashMap<>(400);
		geometriesByIsoCodes = new HashMap<>(400);

		for(int i = 0; i < countriesBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countriesBoundaries.getGeometryN(i);

			Map<String,String> props = (Map<String,String>) countryBoundary.getUserData();
			if(props == null) continue;
			if(props.containsKey(ISO3166_1_ALPHA2) || props.containsKey(ISO3166_2))
			{
				insertIntoIndex(countryBoundary);
				insertIntoIsoCodes(countryBoundary);

			}
		}
	}

	private void insertIntoIsoCodes(Geometry countryBoundary)
	{
		Map<String,String> props = (Map<String,String>) countryBoundary.getUserData();
		String iso3166_2 = props.get(ISO3166_2);
		String iso3166_1_alpha2 = props.get(ISO3166_1_ALPHA2);

		if(iso3166_1_alpha2 != null)
		{
			geometriesByIsoCodes.put(iso3166_1_alpha2, countryBoundary);
		}
		if(iso3166_2 != null)
		{
			geometriesByIsoCodes.put(iso3166_2, countryBoundary);
		}
	}

	private void insertIntoIndex(Geometry countryBoundary)
	{
		// split multipolygons into its elements and copy the properties to them to make better use
		// of the index data structure. I.e. the United Kingdom would be on the top level of the
		// index since because with all those oversees territories, it spans almost the whole
		// world.
		if(countryBoundary instanceof GeometryCollection)
		{
			GeometryCollection countryBoundaries = (GeometryCollection) countryBoundary;
			for (int j = 0; j < countryBoundaries.getNumGeometries(); j++)
			{
				Geometry countryBoundariesSegment =  countryBoundaries.getGeometryN(j);
				index.insert(countryBoundariesSegment.getEnvelopeInternal(), countryBoundary);
			}
		}
		else
		{
			index.insert(countryBoundary.getEnvelopeInternal(), countryBoundary);
		}
	}

	public boolean intersectsWithAny(String[] isoCodes, BoundingBox bbox)
	{
		for (String isoCode : isoCodes)
		{
			if(intersectsWith(isoCode, bbox)) return true;
		}
		return false;
	}

	public boolean intersectsWith(String isoCode, BoundingBox bbox)
	{
		Geometry geometry = geometriesByIsoCodes.get(isoCode);
		if (geometry == null) return false;
		return geometry.intersects(JTSConst.toLinearRing(bbox));
	}

	public boolean isInAny(String[] isoCodes, BoundingBox bbox)
	{
		for (String isoCode : isoCodes)
		{
			if(isIn(isoCode, bbox)) return true;
		}
		return false;
	}

	public boolean isIn(String isoCode, BoundingBox bbox)
	{
		Geometry geometry = geometriesByIsoCodes.get(isoCode);
		if (geometry == null) return false;
		return geometry.contains(JTSConst.toLinearRing(bbox));
	}

	public boolean isInAny(String[] isoCodes, double longitude, double latitude)
	{
		for (String isoCode : isoCodes)
		{
			if(isIn(isoCode, longitude, latitude)) return true;
		}
		return false;
	}

	public boolean isIn(String isoCode, double longitude, double latitude)
	{
		Geometry geometry = geometriesByIsoCodes.get(isoCode);
		if (geometry == null) return false;
		return geometry.covers(factory.createPoint(new Coordinate(longitude, latitude)));
	}

	public List<String> getIsoCodes(double longitude, double latitude)
	{
		Coordinate coord = new Coordinate(longitude, latitude, 0);
		Point point = factory.createPoint(coord);
		Set possibleMatches = new HashSet(index.query(new Envelope(coord)));
		List<Geometry> matches = new ArrayList<>();
		for (Object possibleMatch : possibleMatches)
		{
			Geometry country = (Geometry) possibleMatch;
			if(country.covers(point)) matches.add(country);
		}

		for (Geometry geometry : matches) {
			if(!geometrySizeCache.containsKey(geometry))
			{
				geometrySizeCache.put(geometry, geometry.getArea());
			}
		}

		Collections.sort(matches, (o1, o2) ->
		{
			double diff = geometrySizeCache.get(o1) - geometrySizeCache.get(o2);
			if(diff > 0) return 1;
			if(diff < 0) return -1;
			return 0;
		});

		List<String> result = new ArrayList<>(matches.size());
		for(Geometry match : matches)
		{
			Map<String,String> props = (Map<String,String>) match.getUserData();
			if(props.containsKey(ISO3166_1_ALPHA2))
				result.add(props.get(ISO3166_1_ALPHA2));
			else if(props.containsKey(ISO3166_2))
				result.add(props.get(ISO3166_2));
		}
		return result;
	}
}
