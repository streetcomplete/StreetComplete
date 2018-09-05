package de.westnordost.streetcomplete.quests.railway_crossing;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRailwayCrossingBarrier extends SimpleOverpassQuestType
{
	public AddRailwayCrossingBarrier(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with railway=level_crossing and !crossing:barrier";
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddRailwayCrossingBarrierForm.OSM_VALUES);
		changes.add("crossing:barrier", values.get(0));
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddRailwayCrossingBarrierForm(); }
	@Override public String getCommitMessage() { return "Add type of barrier for railway crossing"; }
	@Override public int getIcon() { return R.drawable.ic_quest_railway; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_railway_crossing_barrier_title;
	}
}
