package de.westnordost.streetcomplete.quests.baby_changing_table;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddBabyChangingTableFragment extends YesNoQuestAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		String name = getName(getOsmElement());
		if(name != null && !name.trim().isEmpty())
		{
			setTitle(R.string.quest_baby_changing_table_title, name);
		}
		else
		{
			setTitle(R.string.quest_baby_changing_table_toilets_title);
		}
	}

	private String getName(OsmElement element)
	{
		return element != null && element.getTags() != null ? element.getTags().get("name") : null;
	}
}
