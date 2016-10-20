package de.westnordost.osmagent.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmagent.R;

public class AddOpeningHoursForm extends AbstractQuestAnswerFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_openingHours_title);
		View contentView = setContentView(R.layout.quest_opening_hours);


		return view;
	}

	@Override protected void onClickOk()
	{

	}
}
