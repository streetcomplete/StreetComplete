package de.westnordost.streetcomplete.data.osmnotes;

import java.util.List;

import androidx.annotation.Nullable;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

public class CreateNote
{
	public long id;
	public String text;
	@Nullable public String questTitle;
	public LatLon position;
	@Nullable public Element.Type elementType;
	@Nullable public Long elementId;
	@Nullable public List<String> imagePaths;

	public boolean hasAssociatedElement()
	{
		return elementType != null && elementId != null;
	}
}
