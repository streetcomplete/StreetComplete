package de.westnordost.streetcomplete.quests.collection_times;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

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
		return Countries.noneExcept(new String[]{
			"DE", // Germany
			"GB", // UK
			"DK", // Denmark
			"AT", // Austria
			"CH", // Switzerland
			"FR", // France
			"SE", // Sweden
			"PL", // Poland
			"NL", // Netherlands
			"US"  // United States of America
		});
	}

	@Override public String getCommitMessage() { return "Add collection times"; }
	@Override public int getIcon() { return R.drawable.ic_quest_mail; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return R.string.quest_collectionTimes_name_title; }
}
