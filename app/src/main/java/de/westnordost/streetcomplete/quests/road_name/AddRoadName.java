package de.westnordost.streetcomplete.quests.road_name;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;

public class AddRoadName extends SimpleOverpassQuestType
{
	@Inject public AddRoadName(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return " ways with " +
		       " highway ~ living_street|residential|pedestrian|primary|secondary|tertiary|unclassified|road " +
		       " and !name and !ref and noname != yes and !junction and !area";
	}

	@Override
	public int importance()
	{
		return QuestImportance.WARNING;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoadNameForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddRoadNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
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
			return;
		}

		String name = answer.getString(AddRoadNameForm.NAME);
		if(name != null) changes.add("name", name);
	}

	@Override public String getCommitMessage()
	{
		return "Determine road names";
	}

	@Override public String getIconName() {	return "signpost"; }
}
