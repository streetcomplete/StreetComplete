package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;


public class AddWheelchairAccessBusinessForm extends WheelchairAccessAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setContentView(R.layout.quest_wheelchair_business_explanation);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = element != null && element.getTags() != null ? element.getTags().get("name") : null;
		setTitle(R.string.quest_wheelchairAccess_name_title, name);

	}
}
