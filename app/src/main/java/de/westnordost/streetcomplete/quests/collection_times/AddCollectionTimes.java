package de.westnordost.streetcomplete.quests.collection_times;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;

public class AddCollectionTimes extends SimpleOverpassQuestType
{
	@Inject public AddCollectionTimes(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		String[] amenities = { "post_box" };

		return " nodes, ways, relations with ( amenity ~ "
				+ TextUtils.join("|", amenities) + ")" +
				" and !collection_times" + " and (access !~ private|no)"; // exclude ones without access to general public
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddCollectionTimesForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String collectionTimes = answer.getString(AddCollectionTimesForm.COLLECTION_TIMES);
		if(collectionTimes != null)
		{
			changes.add("collection_times", collectionTimes);
		}
	}

	@NonNull @Override public Countries getEnabledForCountries()
	{
		return Countries.allExcept(new String[]{
			""
		});
	}

	@Override public String getCommitMessage() { return "Add collection times"; }
	@Override public int getIcon() { return R.drawable.ic_quest_collection_times; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return R.string.quest_collectionTimes_name_title; }
}
