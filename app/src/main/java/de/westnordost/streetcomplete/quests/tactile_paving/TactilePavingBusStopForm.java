package de.westnordost.streetcomplete.quests.tactile_paving;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class TactilePavingBusStopForm extends YesNoQuestAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setContentView(R.layout.quest_tactile_paving);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = element != null && element.getTags() != null ? element.getTags().get("name") : null;
		if(name != null && !name.trim().isEmpty())
		{
			setTitle(R.string.quest_tactilePaving_title_name_bus, name);
		}
		else
		{
			setTitle(R.string.quest_tactilePaving_title_bus);
		}
	}
}
