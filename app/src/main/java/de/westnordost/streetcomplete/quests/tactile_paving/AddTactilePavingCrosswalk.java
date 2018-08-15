package de.westnordost.streetcomplete.quests.tactile_paving;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddTactilePavingCrosswalk extends SimpleOverpassQuestType
{
	@Inject public AddTactilePavingCrosswalk(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return "nodes with highway=crossing and !tactile_paving";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new TactilePavingForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("tactile_paving", yesno);
	}

	@Override public String getCommitMessage() { return "Add tactile pavings on crosswalks"; }
	@Override public int getIcon() { return R.drawable.ic_quest_blind_pedestrian_crossing; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_tactilePaving_title_crosswalk;
	}

	static final Countries ENBABLED_FOR_COUNTRIES = Countries.noneExcept(new String[]
	{
		// Europe
		"NO","SE",
		"GB","IE","NL","BE","FR","ES",
		"DE","PL","CZ","SK","HU","AT","CH",
		"LV","LT","EE","RU",
		// America
		"US","CA","AR",
		// Asia
		"HK","SG","KR","JP",
		// Oceania
		"AU","NZ",
	});

    @NonNull @Override public Countries getEnabledForCountries()
    {
		// See overview here: https://ent8r.github.io/blacklistr/?java=tactile_paving/AddTactilePavingCrosswalk.java
		// #750
		return ENBABLED_FOR_COUNTRIES;
    }
}
