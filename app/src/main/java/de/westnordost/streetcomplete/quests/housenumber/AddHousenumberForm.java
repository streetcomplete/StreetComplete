package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddHousenumberForm extends AbstractQuestAnswerFragment
{
	private EditText input;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_address_title);

		View contentView = setContentView(R.layout.quest_housenumber);

		input = (EditText) contentView.findViewById(R.id.input);

		return view;
	}

	@Override protected void onClickOk()
	{

	}

	@Override public boolean hasChanges()
	{
		return false;
	}
}
