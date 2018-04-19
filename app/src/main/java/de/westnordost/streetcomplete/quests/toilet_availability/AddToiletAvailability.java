package de.westnordost.streetcomplete.quests.toilet_availability;

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

public class AddToiletAvailability extends SimpleOverpassQuestType
{
	@Inject public AddToiletAvailability(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		// only for malls, big stores and rest areas because users should not need to go inside a non-public
		// place to solve the quest. (Considering malls and department stores public enough)
		return "nodes, ways with ( (shop ~ mall|department_store and name) or (highway ~ services|rest_area) ) and !toilets";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("toilets", yesno);
	}

	@Override public String getCommitMessage() { return "Add toilet availability"; }
	@Override public int getIcon() { return R.drawable.ic_quest_toilets; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean isRestArea = "rest_area".equals(tags.get("highway")) || "services".equals(tags.get("highway"));

		if (isRestArea) return R.string.quest_toiletAvailability_rest_area_title;
		else return R.string.quest_toiletAvailability_name_title;
	}
}
