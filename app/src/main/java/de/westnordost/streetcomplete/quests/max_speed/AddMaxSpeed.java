package de.westnordost.streetcomplete.quests.max_speed;

import android.os.Bundle;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddMaxSpeed extends SimpleOverpassQuestType
{
	@Inject public AddMaxSpeed(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways with highway ~ " +
		       "motorway|trunk|primary|secondary|tertiary|unclassified|residential" +
		       " and !maxspeed and !maxspeed:forward and !maxspeed:backward" +
		       " and !source:maxspeed and !zone:maxspeed and !maxspeed:type" + // implicit speed limits
		       " and (access !~ private|no or (foot and foot !~ private|no))"; // no private roads
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddMaxSpeedForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		boolean isLivingStreet = answer.getBoolean(AddMaxSpeedForm.LIVING_STREET);
		String maxspeed = answer.getString(AddMaxSpeedForm.MAX_SPEED);
		String advisory = answer.getString(AddMaxSpeedForm.ADVISORY_SPEED);
		if(isLivingStreet)
		{
			changes.modify("highway","living_street");
		}
		else if(advisory != null)
		{
			changes.add("maxspeed:advisory", advisory);
			changes.add("source:maxspeed:advisory", "sign");
		}
		else
		{
			if (maxspeed != null)
			{
				changes.add("maxspeed", maxspeed);
			}
			String country = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY);
			String roadtype = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE);
			if (roadtype != null && country != null)
			{
				changes.add("source:maxspeed", country + ":" + roadtype);
			}
			else if (maxspeed != null)
			{
				changes.add("source:maxspeed", "sign");
			}
		}
	}

	@Override public String getCommitMessage() { return "Add speed limits"; }
	@Override public int getIcon() { return R.drawable.ic_quest_max_speed; }
	@Override public int getTitle(Map<String, String> tags)
	{
		return R.string.quest_maxspeed_title_short;
	}
}
