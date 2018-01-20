package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;

public class AddWheelChairAccessToilets extends SimpleOverpassQuestType
{
	@Inject public AddWheelChairAccessToilets(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return " nodes, ways with " +
				" amenity=toilets" +
				" and access !~ private|customers" +
				" and !wheelchair";
	}

	@Override public WheelchairAccessAnswerFragment createForm()
	{
		return new AddWheelchairAccessToiletsForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String wheelchair = answer.getString(AddWheelchairAccessToiletsForm.ANSWER);
		if(wheelchair != null)
		{
			changes.add("wheelchair", wheelchair);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add wheelchair access to toilets";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_toilets_wheelchair; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		return hasName ? R.string.quest_wheelchairAccess_toilets_name_title : R.string.quest_wheelchairAccess_toilets_title;
	}
}
