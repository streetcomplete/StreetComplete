package de.westnordost.streetcomplete.quests.bus_stop_shelter;

import android.os.Bundle;

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
		return "nodes with (public_transport=platform or (highway=bus_stop and public_transport!=stop_position)) and !shelter";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new BusStopShelterForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("shelter", yesno);
	}

	@Override public String getCommitMessage() { return "Add bus stop shelter"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bus; }
}
