package de.westnordost.osmagent.quests.osm.download;

import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Way;

public interface WayGeometrySource
{
	List<LatLon> getNodePositions(long wayId);
}
