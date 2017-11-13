package de.westnordost.streetcomplete.tangram;

import com.mapzen.tangram.LngLat;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class TangramConst
{
	public static LngLat toLngLat(LatLon pos)
	{
		return new LngLat(pos.getLongitude(), pos.getLatitude());
	}

	public static LatLon toLatLon(LngLat pos)
	{
		return new OsmLatLon(pos.latitude, pos.longitude);
	}

	public static List<List<LngLat>> toLngLat(List<List<LatLon>> positionLists)
	{
		List<List<LngLat>> result = new ArrayList<>(positionLists.size());
		for(List<LatLon> positions : positionLists)
		{
			List<LngLat> resultPositions = new ArrayList<>(positions.size());
			for(LatLon pos : positions)
			{
				resultPositions.add(toLngLat(pos));
			}
			result.add(resultPositions);
		}
		return result;
	}

	public static List<List<LatLon>> toLatLon(List<List<LngLat>> positionLists)
	{
		List<List<LatLon>> result = new ArrayList<>(positionLists.size());
		for(List<LngLat> positions : positionLists)
		{
			List<LatLon> resultPositions = new ArrayList<>(positions.size());
			for(LngLat pos : positions)
			{
				resultPositions.add(toLatLon(pos));
			}
			result.add(resultPositions);
		}
		return result;
	}
}
