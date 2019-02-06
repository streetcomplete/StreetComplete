package de.westnordost.streetcomplete.data.osmnotes;

import android.graphics.Point;
import androidx.annotation.Nullable;

import java.util.List;

public interface CreateNoteListener
{
	/** Called when the user wants to leave a note which is not related to a quest */
	void onLeaveNote(String note, @Nullable List<String> imagePaths, Point screenPosition);
}
