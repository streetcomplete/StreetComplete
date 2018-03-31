package de.westnordost.streetcomplete.quests.postbox_collection_times;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddPostboxCollectionTimes extends SimpleOverpassQuestType
{
	public AddPostboxCollectionTimes(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with amenity=post_box and !collection_times and (access !~ private|no)";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddCollectionTimesForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String times = answer.getString(AddCollectionTimesForm.TIMES);
		if(times != null)
		{
			changes.add("collection_times", times);
		}
	}

	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_postboxCollectionTimes_title;
	}

	@Override public int getIcon() { return R.drawable.ic_quest_mail; }
	@Override public String getCommitMessage() { return "Add postbox collection times"; }

	@NonNull @Override public Countries getEnabledForCountries()
	{
		// See overview here: https://ent8r.github.io/blacklistr/?java=postbox_collection_times/AddPostboxCollectionTimes.java

		// sources:
		// https://www.itinerantspirit.com/home/2016/5/22/post-boxes-from-around-the-world
		// https://commons.wikimedia.org/wiki/Category:Post_boxes_by_country
		// http://wanderlustexplorers.com/youve-got-mail-23-international-postal-boxes/

		return Countries.noneExcept(new String[]{
			"AU","NZ","VU","MY","SG","TH","VN","LA","MM","IN","BD","NP","LK","BT","PK","TW","HK",
			"MO","CN","KR","JP","RU","BY","LT","LV","FI","SE","NO","DK","GB","IE","IS","NL","BE",
			"FR","AD","ES","PT","CH","LI","AT","DE","LU","MC","IT","SM","MT","PL","EE","CA","US",
			"UA","SK","CZ","HU","RO","MD","BG","SI","HR","IL","ZA","GR","UZ","ME","CY","TR","LB"
		});

		// apparently mostly not in Latin America and in Arabic world
		// maybe/sometimes in Indonesia, Philippines, Papua New Guinea, Timor, Cambodia
		// unknown in Kazakhstan, Mongolia, Turkmenistan, Kyrgyzstan and countries around that area
		// unknown in Africa
		// unknown in former Yugoslawia
	}
}
