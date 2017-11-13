package de.westnordost.streetcomplete.data.osmnotes;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

public class CreateNote
{
	public long id;
	public String text;
	public String questTitle;
	public LatLon position;
	public Element.Type elementType;
	public Long elementId;
	public ArrayList<String> imagePaths;

	public boolean hasAssociatedElement()
	{
		return elementType != null && elementId != null;
	}
}
