package de.westnordost.streetcomplete.quests.building_type;


import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBuildingType extends SimpleOverpassQuestType
{
	@Inject public AddBuildingType(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		// in the case of man_made, historic, military and power, these tags already contain
		// information about the purpose of the building, so no need to force asking it
		return "ways, relations with building=yes and !man_made and !historic and !military and !power and location!=underground";
	}

	public AbstractQuestAnswerFragment createForm() { return new AddBuildingTypeForm(); }

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String building = answer.getString(AddBuildingTypeForm.BUILDING);
		String man_made = answer.getString(AddBuildingTypeForm.MAN_MADE);
		if(man_made != null)
		{
			changes.delete("building");
			changes.add("man_made", man_made);
		}
		else if(building != null)
		{
			changes.modify("building", building);
		}
	}

	@Override public String getCommitMessage() { return "Add building types"; }
	@Override public int getIcon() { return R.drawable.ic_quest_building; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return R.string.quest_buildingType_title2; }
}
