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
		       " highway = service and service = alley) and !name and noname != yes and !junction and !area";
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

	public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddRoadNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return R.string.quest_streetName_commitMessage_noname;
		}

		int noProperRoad = answer.getInt(AddRoadNameForm.NO_PROPER_ROAD);
		if(noProperRoad != 0)
		{
			if(noProperRoad == AddRoadNameForm.IS_SERVICE)
				changes.modify("highway", "service");
			else if(noProperRoad == AddRoadNameForm.IS_TRACK)
				changes.modify("highway", "track");
			else if(noProperRoad == AddRoadNameForm.IS_LINK)
			{
				String prevValue = changes.getPreviousValue("highway");
				if(prevValue.matches("primary|secondary|tertiary"))
				{
					changes.modify("highway", prevValue + "_link");
				}
			}
			return R.string.quest_streetName_commitMessage_noproperroad;
		}

		String name = answer.getString(AddRoadNameForm.NAME);
		if(name != null) changes.add("name", name);
		return R.string.quest_streetName_commitMessage;
	}

	@Override public String getIconName() {	return "signpost"; }
}
