package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.road_name.AddRoadNameForm;

public class AddRoadSurface extends OverpassQuestType
{
	// well, all roads have surfaces, what I mean is that not all ways with highway key are
	// "something with a surface"
	private static final String[] ROADS_WITH_SURFACES = {
			"trunk","trunk_link","motorway","motorway_link",
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "bicycle_road", "living_street", "pedestrian",
			"track", "road",
			/*"service", */ // this is too much, and the information value is very low
	};

	@Override
	protected String getTagFilters()
	{
		return " ways with ( highway ~ " + TextUtils.join("|",ROADS_WITH_SURFACES) + " and" +
			   " !surface)";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MAJOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoadSurfaceForm();
	}

	public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("surface", answer.getString(AddRoadSurfaceForm.SURFACE));
		return R.string.quest_roadSurface_commitMessage;
	}

	@Override public String getIconName() {	return "street_surface"; }
}
