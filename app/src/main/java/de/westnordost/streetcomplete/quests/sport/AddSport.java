package de.westnordost.streetcomplete.quests.sport;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddSport extends SimpleOverpassQuestType
{
	@Inject public AddSport(OverpassMapDataDao overpassServer) { super(overpassServer); }

	private static final String[] AMBIGUOUS_SPORT_VALUES = {
			"team_handball", // -> not really ambiguous but same as handball
			"hockey", // -> ice_hockey or field_hockey
			"skating", // -> ice_skating or roller_skating
			"football" // -> american_football, soccer or other *_football
	};

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with leisure=pitch and" +
				" (!sport or sport ~ " + TextUtils.join("|", AMBIGUOUS_SPORT_VALUES)+ ")" +
				" and (access !~ private|no)"; // exclude ones without access to general public
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddSportForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddSportForm.OSM_VALUES);
		if(values != null && !values.isEmpty())
		{
			String valuesStr = TextUtils.join(";", values);

			String prev = changes.getPreviousValue("sport");

			// only modify the previous values in case of these ~deprecated ones, otherwise assume
			// always that the tag has not been set yet (will drop the solution if it has been set
			// in the meantime by other people) (#291)
			if(Arrays.asList(AMBIGUOUS_SPORT_VALUES).contains(prev))
			{
				changes.modify("sport",valuesStr);
			}
			else
			{
				changes.add("sport", valuesStr);
			}
		}
	}

	@Override public String getCommitMessage() { return "Add pitches sport"; }
	@Override public int getIcon() { return R.drawable.ic_quest_sport; }
	@Override public int getTitle(@NonNull Map<String,String> tags)
	{
		return R.string.quest_sport_title;
	}
}
