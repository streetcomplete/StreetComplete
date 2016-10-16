package de.westnordost.osmagent.quests.osm.download;

import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;

public interface MapDataWithGeometryHandler
{
	void handle(Element element, ElementGeometry geometry);
}
