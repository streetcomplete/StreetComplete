package de.westnordost.streetcomplete.data.osm.download;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.Element;

public interface MapDataWithGeometryHandler
{
	void handle(@NonNull Element element, @Nullable ElementGeometry geometry);
}
