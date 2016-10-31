package de.westnordost.osmagent.quests;

import android.os.Bundle;

import de.westnordost.osmagent.data.QuestImportance;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;

import de.westnordost.osmagent.R;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.dialogs.road_name.AddRoadNameForm;

public class AddRoadName extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return " ways with (" +
		       " highway ~ living_street|bicycle_road|residential|pedestrian|primary|secondary|tertiary|unclassified|road or " +
		       " highway = service and service = alley) and !name and noname != yes ";
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
