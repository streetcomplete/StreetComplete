package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBuildingLevels extends SimpleOverpassQuestType
{
	@Inject public AddBuildingLevels(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

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

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddBuildingLevelsForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("building:levels", ""+answer.getInt(AddBuildingLevelsForm.BUILDING_LEVELS));

		// only set the roof levels if the user supplied that in the form
		int roofLevels = answer.getInt(AddBuildingLevelsForm.ROOF_LEVELS,-1);
		if(roofLevels != -1)
		{
			changes.addOrModify("roof:levels", "" + roofLevels);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add building and roof levels";
	}

	@Override public String getIconName() {	return "building_levels"; }
}
