package de.westnordost.streetcomplete.data.osmnotes;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.LatLon;

public interface CreateNoteListener
{
	/** Called when the user wants to leave a note which is not related to a quest */
	void onLeaveNote(String note, ArrayList<String> imagePaths, LatLon position);
}
