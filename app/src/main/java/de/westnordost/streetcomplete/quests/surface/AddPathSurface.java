package de.westnordost.streetcomplete.quests.surface;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment;

public class AddPathSurface extends SimpleOverpassQuestType
{
	@Inject public AddPathSurface(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return " ways with highway ~ path|footway|cycleway|bridleway|steps and" +
			   " !surface and access !~ private|no";
	}

	public AbstractQuestAnswerFragment createForm() { return new AddPathSurfaceForm(); }

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("surface", answer.getString(GroupedImageListQuestAnswerFragment.OSM_VALUE));
	}

	@Override public String getCommitMessage() { return "Add path surfaces"; }
	@Override public int getIcon() { return R.drawable.ic_quest_way_surface; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean isSquare = tags.containsKey("area") && tags.get("area").equals("yes");
		String pathType = tags.get("highway");
		if(isSquare) return R.string.quest_streetSurface_square_title;
		if("bridleway".equals(pathType)) return R.string.quest_pathSurface_title_bridleway;
		if("steps".equals(pathType)) return R.string.quest_pathSurface_title_steps;
		// rest is rather similar, can be called simply "path"
		return R.string.quest_pathSurface_title;
	}
}
