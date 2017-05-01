package de.westnordost.streetcomplete.quests.bike_parking_capacity;

import android.os.Bundle;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBikeParkingCapacity extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with amenity=bicycle_parking and !capacity";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddBikeParkingCapacityForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("capacity", ""+answer.getInt(AddBikeParkingCapacityForm.BIKE_PARKING_CAPACITY));
	}

	@Override public String getCommitMessage()
	{
		return "Add bicycle parking capacity";
	}

	@Override public String getIconName() {	return "bicycle_parking"; }
}
