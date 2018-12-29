package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;

public class AddWheelchairAccessOutside extends SimpleOverpassQuestType
{
	@Inject public AddWheelchairAccessOutside(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with leisure=dog_park and !wheelchair";
	}

	@Override public WheelchairAccessAnswerFragment createForm()
	{
		return new AddWheelchairAccessOutsideForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String wheelchair = answer.getString(AddWheelchairAccessOutsideForm.ANSWER);
		if(wheelchair != null)
		{
			changes.add("wheelchair", wheelchair);
		}
	}

	@Override public String getCommitMessage() { return "Add wheelchair access to outside places"; }
	@Override public int getIcon() { return R.drawable.ic_quest_wheelchair_outside; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_wheelchairAccess_outside_title;
	}
}
