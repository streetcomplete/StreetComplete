package de.westnordost.streetcomplete.quests.religion;

import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;

public class AddReligionToPlaceOfWorship extends AbstractAddReligionToQuestType
{
	@Inject public AddReligionToPlaceOfWorship(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with amenity=place_of_worship and" +
                " !religion and" +
                " name";
	}

	@Override public String getCommitMessage() { return "Add religion for place of worship"; }
	@Override public int getTitle(@NonNull Map<String,String> tags)
	{
		return R.string.quest_religion_for_place_of_worship_title;
	}
}
