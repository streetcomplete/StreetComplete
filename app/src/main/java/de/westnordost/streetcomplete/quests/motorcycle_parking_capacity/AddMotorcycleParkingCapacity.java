package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacityForm;

public class AddMotorcycleParkingCapacity extends SimpleOverpassQuestType
{
	@Inject public AddMotorcycleParkingCapacity(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return "nodes, ways with amenity=motorcycle_parking and !capacity and access !~ private|no";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddMotorcycleParkingCapacityForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("capacity", ""+Integer.parseInt(answer.getString(AddMotorcycleParkingCapacityForm.INPUT)));
	}

	@Override public String getCommitMessage() { return "Add motorcycle parking capacities"; }
	@Override public int getIcon() { return R.drawable.ic_quest_motorcycle_parking_capacity; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_motorcycleParkingCapacity_title;
	}
}
