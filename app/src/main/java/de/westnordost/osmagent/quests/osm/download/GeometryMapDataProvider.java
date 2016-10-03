package de.westnordost.osmagent.quests.osm.download;

import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Way;

public interface GeometryMapDataProvider
{
	Node getNode(long id);
	Way getWay(long id);
}
