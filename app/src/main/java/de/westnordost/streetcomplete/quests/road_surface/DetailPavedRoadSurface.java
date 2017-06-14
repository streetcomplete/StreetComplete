package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class DetailPavedRoadSurface extends SimpleOverpassQuestType {
	@Inject public DetailPavedRoadSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return " ways with ( highway ~ " + TextUtils.join("|", RoadSurfaceConfig.ROADS_WITH_SURFACES) + " and" +
				" surface=paved)";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new DetailPavedRoadSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.modify("surface", answer.getString(DetailPavedRoadSurfaceForm.SURFACE));
	}

	@Override public String getCommitMessage()
	{
		return "Detail road surfaces";
	}

	@Override public String getIconName()
	{
		return "street_surface";
	}
}
