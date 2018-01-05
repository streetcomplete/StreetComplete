package de.westnordost.streetcomplete.data.osmnotes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class CreateNoteFragment extends Fragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_create_note, container, false);
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new CreateNoteDialog();
	}
}
