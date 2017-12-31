package de.westnordost.streetcomplete.data.osmnotes;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.LatLon;

public class CreateNoteAnswerComponent
{

	CreateNoteListener callbackListener;

	public void onAttach(CreateNoteListener listener)
	{
		callbackListener = listener;
	}

	public void onLeaveNote(String text, ArrayList<String> imagePaths, LatLon position)
	{
		callbackListener.onLeaveNote(text, imagePaths, position);
	}
}
