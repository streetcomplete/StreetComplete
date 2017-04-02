package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBuildingLevels extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return " ways, relations with " +
		       " building ~ house|residential|apartments|detached|terrace|farm|hotel|dormitory|houseboat|" +
							"school|civic|college|university|public|hospital|kindergarten|transportation|train_station|"+
							"retail|commercial|warehouse|industrial|manufacture" +
		       " and !building:levels and !height and !building:height";
		// building:height is undocumented, but used the same way as height and currently over 50k times
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddBuildingLevelsForm();
	}

	public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("building:levels", ""+answer.getInt(AddBuildingLevelsForm.BUILDING_LEVELS));

		// only set the roof levels if the user supplied that in the form
		int roofLevels = answer.getInt(AddBuildingLevelsForm.ROOF_LEVELS,-1);
		if(roofLevels != -1)
		{
			changes.addOrModify("roof:levels", "" + roofLevels);
		}

		return R.string.quest_buildingLevels_commitMessage;
	}

	@Override public String getIconName() {	return "building_levels"; }
}
