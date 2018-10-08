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
		return "nodes with amenity=post_box and !collection_times and collection_times:signed != no and (access !~ private|no)";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddCollectionTimesForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		boolean noTimes = answer.getBoolean(AddCollectionTimesForm.NO_TIMES_SPECIFIED);
		if(noTimes)
		{
			changes.add("collection_times:signed","no");
		}
		else
		{
			String times = answer.getString(AddCollectionTimesForm.TIMES);
			if (times != null)
			{
				changes.add("collection_times", times);
			}
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
			// definitely, seen pictures:
			"AU","NZ","VU","MY","SG","TH","VN","LA","MM","IN","BD","NP","LK","BT","PK","TW","HK",
			"MO","CN","KR","JP","RU","BY","LT","LV","FI","SE","NO","DK","GB","IE","IS","NL","BE",
			"FR","AD","ES","PT","CH","LI","AT","DE","LU","MC","IT","SM","MT","PL","EE","CA","US",
			"UA","SK","CZ","HU","RO","MD","BG","SI","HR","IL","ZA","GR","UZ","ME","CY","TR","LB",
			// these only maybe/sometimes (Oceania, Cambodia, North Korea):
			"BN","KH","ID","TL","PG","KP","PH",
			// unknown but all countries around have it (former Yugoslawia):
			"RS","RS-KM","BA","MK","AL",
			// unknown but region around it has it (southern states of former soviet union):
			"TJ","KG","KZ","MN","GE",
		});

		// apparently mostly not in Latin America and in Arabic world and unknown in Africa
	}
}
