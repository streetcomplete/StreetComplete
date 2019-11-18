package de.westnordost.osmapi.overpass;

import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

public interface WayGeometrySource
{
	List<LatLon> getNodePositions(long wayId);
}
