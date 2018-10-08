package de.westnordost.streetcomplete.quests.parking_access;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddParkingAccess extends SimpleOverpassQuestType
{
	@Inject public AddParkingAccess(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with amenity=parking and (!access or access=unknown)";
	}

	public AbstractQuestAnswerFragment createForm() { return new AddParkingAccessForm(); }

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String osmValue = bundle.getString(AddParkingAccessForm.ACCESS);
		if (osmValue != null)
		{
			changes.addOrModify("access", osmValue);
		}
	}
	@Override public String getCommitMessage() { return "Add type of parking access"; }
	@Override public int getIcon() { return R.drawable.ic_quest_parking_access; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_parking_access_title;
	}
}
