package de.westnordost.osmagent.data.osm.download;

import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;

public interface MapDataWithGeometryHandler
{
	void handle(Element element, ElementGeometry geometry);
}
