package de.westnordost.streetcomplete.quests.religion;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddReligionToPlaceOfWorship extends SimpleOverpassQuestType
{
	@Inject public AddReligionToPlaceOfWorship(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways, relations with amenity=place_of_worship and" +
                " !religion and" +
                " name";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddReligionToPlaceOfWorshipForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddReligionToPlaceOfWorshipForm.OSM_VALUES);
		if(values != null && !values.isEmpty())
		{
			String religionValueStr = values.get(0);
            changes.add("religion", religionValueStr);
		}
	}

	@Override public String getCommitMessage() { return "Add religion for amenity=place_of_worship"; }
	@Override public int getIcon() { return R.drawable.ic_quest_religion; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_religion_for_place_of_worship_title;
	}
}
