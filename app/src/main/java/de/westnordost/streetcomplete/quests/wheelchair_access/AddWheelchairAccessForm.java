package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;


public class AddWheelchairAccessForm extends WheelchairAccessAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = element != null && element.getTags() != null ? element.getTags().get("name") : null;
		setTitle(getResources().getString(R.string.quest_wheelchairAccess_title, name));

	}
}
