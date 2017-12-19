package de.westnordost.streetcomplete.quests.internet_access;


import android.os.Bundle;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddInternetAccess extends SimpleOverpassQuestType
{
	@Inject public AddInternetAccess(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override
	protected String getTagFilters() {
		return "nodes, ways, relations with " +
				" amenity ~ restaurant|cafe|ice_cream|fast_food|bar|pub|biergarten|food_court|cinema|library" +
				"  or" +
				" tourism ~ hotel|guest_house|hostel|motel" +
				" and !internet_access and !name";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddInternetAccessForm();
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String osmValue = bundle.getString(AddInternetAccessForm.OSM_VALUE);
		if (osmValue != null)
		{
			changes.add("internet_access", osmValue);
		}
	}
	@Override public String getCommitMessage() { return "Add internet access"; }
	@Override public int getIcon() { return R.drawable.ic_quest_wifi; }
	@Override public int getTitle(Map<String, String> tags)
	{
		return R.string.quest_internet_access_name_title;
	}
	@Override public int getDefaultDisabledMessage()
	{
		return R.string.default_disabled_msg_go_inside;
	}
}
