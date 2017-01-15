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
		return " ways, relations with (" +
		       " building ~ house|residential|apartments|detached|terrace|farm|hotel|dormitory|houseboat|" +
							"school|civic|college|university|public|hospital|kindergarten|transportation|train_station|"+
							"retail|commercial|warehouse|industrial|manufacture" +
		       " or building:part " +
		       " ) and !building:levels and !height";
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
		changes.addOrModify("roof:levels", ""+answer.getInt(AddBuildingLevelsForm.ROOF_LEVELS));

		return R.string.quest_buildingLevels_commitMessage;
	}

	@Override public String getIconName() {	return "house"; }
}
