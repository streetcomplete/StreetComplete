package de.westnordost.streetcomplete.data.osm.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;

public interface MapDataWithGeometryHandler
{
	void handle(@NonNull Element element, @Nullable ElementGeometry geometry);
}
