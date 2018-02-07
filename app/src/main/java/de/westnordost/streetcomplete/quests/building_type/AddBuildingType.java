package de.westnordost.streetcomplete.quests.building_type;


import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurfaceForm;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShapeForm;

public class AddBuildingType extends SimpleOverpassQuestType
{
	@Inject public AddBuildingType(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters() { return "ways, relations with building=yes and !man_made"; }

	public AbstractQuestAnswerFragment createForm() { return new AddBuildingTypeForm(); }

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.modify("building", answer.getString(AddBuildingTypeForm.BUILDING));
	}

	@Override public String getCommitMessage() { return "Add building type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_building; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return R.string.quest_buildingType_title; }
}
