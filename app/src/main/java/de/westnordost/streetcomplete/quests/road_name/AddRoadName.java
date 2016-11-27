package de.westnordost.streetcomplete.quests.road_name;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;

import de.westnordost.streetcomplete.R;

public class AddRoadName extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return " ways with (" +
		       " highway ~ living_street|bicycle_road|residential|pedestrian|primary|secondary|tertiary|unclassified|road or " +
		       " highway = service and service = alley) and !name and noname != yes and !junction";
	}

	@Override
	public int importance()
	{
		return QuestImportance.WARNING;
	}

	public AbstractQuestAnswerFragment getForm()
	{
		return new AddRoadNameForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddRoadNameForm.NO_NAME))
		{
			changes.add("no_name", "yes");
		}
		else
		{
			String name = answer.getString(AddRoadNameForm.NAME);
			if(name != null) changes.add("name", name);
		}
	}

	@Override public int getCommitMessageResourceId()
	{
		return R.string.quest_openingHours_commitMessage;
	}

	@Override public String getIconName() {	return "signpost"; }
}
