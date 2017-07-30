package de.westnordost.streetcomplete.quests.bus_stop_shelter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class BusStopShelterForm extends YesNoQuestAnswerFragment
{

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		String name = getElementName();
		if(name != null)
		{
			setTitle(R.string.quest_busStopShelter_name_title, name);
		}
		else
		{
			setTitle(R.string.quest_busStopShelter_title);
		}
	}
}
