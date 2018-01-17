package de.westnordost.streetcomplete.data.osmnotes;

import android.graphics.Point;

import java.util.ArrayList;

public interface CreateNoteListener
{
	/** Called when the user wants to leave a note which is not related to a quest */
	void onLeaveNote(String note, ArrayList<String> imagePaths, Point screenPosition);
}
