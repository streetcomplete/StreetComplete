package de.westnordost.streetcomplete.data.osm.download;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;

public interface MapDataWithGeometryHandler
{
	void handle(Element element, ElementGeometry geometry);
}
