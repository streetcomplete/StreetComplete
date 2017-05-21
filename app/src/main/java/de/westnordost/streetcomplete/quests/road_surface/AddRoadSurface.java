package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRoadSurface extends SimpleOverpassQuestType
{
	// well, all roads have surfaces, what I mean is that not all ways with highway key are
	// "something with a surface"
	private static final String[] ROADS_WITH_SURFACES = {
			// "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "bicycle_road", "living_street", "pedestrian",
			"track", "road",
			"footway", "cycleway", "path",
			/*"service", */ // this is too much, and the information value is very low
	};

	@Inject public AddRoadSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

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

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("surface", answer.getString(AddRoadSurfaceForm.SURFACE));
	}

	@Override public String getCommitMessage()
	{
		return "Add road surfaces";
	}

	@Override public String getIconName() {	return "street_surface"; }
}
