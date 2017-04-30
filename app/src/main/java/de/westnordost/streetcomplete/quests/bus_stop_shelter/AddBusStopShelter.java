package de.westnordost.streetcomplete.quests.bus_stop_shelter;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBusStopShelter extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return "nodes with (public_transport=platform or (highway=bus_stop and public_transport!=stop_position)) and !shelter";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddBusStopShelterForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("shelter", answer.getString(AddBusStopShelterForm.BUS_STOP_SHELTER));
	}

	@Override public String getCommitMessage()
	{
		return "Add bus stop shelter";
	}

	@Override public String getIconName() {	return "bus_stop"; }
}
