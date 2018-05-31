package de.westnordost.streetcomplete.quests.bus_stop_shelter;

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

public class AddBusStopShelter extends SimpleOverpassQuestType
{
	@Inject public AddBusStopShelter(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with" +
		       " ((public_transport=platform and (bus=yes or trolleybus=yes or tram=yes))" +
		       " or" +
		       " (highway=bus_stop and public_transport!=stop_position))" +
		       " and !shelter";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("shelter", yesno);
	}

	@Override public String getCommitMessage() { return "Add bus stop shelter"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bus_stop_shelter; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		String tram = tags.get("tram");
		if(tram != null && tram.equals("yes"))
		{
			if (hasName) return R.string.quest_busStopShelter_tram_name_title;
			else         return R.string.quest_busStopShelter_tram_title;
		}
		else
		{
			if (hasName) return R.string.quest_busStopShelter_name_title;
			else         return R.string.quest_busStopShelter_title;
		}
	}
}
