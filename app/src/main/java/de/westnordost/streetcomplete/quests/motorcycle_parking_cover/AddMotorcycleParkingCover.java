package de.westnordost.streetcomplete.quests.motorcycle_parking_cover;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddMotorcycleParkingCover extends SimpleOverpassQuestType
{
	@Inject public AddMotorcycleParkingCover(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with amenity=motorcycle_parking and access !~ private|no and !covered and motorcycle_parking !~ shed|garage_boxes|building";
	}

	public AbstractQuestAnswerFragment createForm() { return new YesNoQuestAnswerFragment(); }

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("covered", yesno);
	}

	@Override public String getCommitMessage() { return "Add motorcycle parkings cover"; }
	@Override public int getIcon() { return R.drawable.ic_quest_motorcycle_parking_cover; }
	@Override public int getTitle(@NonNull Map<String,String> tags)
	{
		return R.string.quest_motorcycleParkingCoveredStatus_title;
	}
}
