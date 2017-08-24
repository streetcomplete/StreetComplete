package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddVegan extends SimpleOverpassQuestType
{
	@Inject public AddVegan(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with amenity ~ restaurant|cafe|fast_food and name and diet:vegetarian ~ yes|only and !diet:vegan";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan);
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String osmValue = bundle.getString(AddDietTypeForm.OSM_VALUE);
		if (osmValue != null)
		{
			changes.add("diet:vegan", osmValue);
		}
	}

	@Override public String getCommitMessage() { return "Add vegan diet type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_restaurant_veg; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_dietType_vegan_name_title;
	}

	// TODO: show only for a certain set of countries: See https://github.com/westnordost/StreetComplete/pull/506#issuecomment-323579888
}
