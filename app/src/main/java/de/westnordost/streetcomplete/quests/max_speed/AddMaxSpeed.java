package de.westnordost.streetcomplete.quests.max_speed;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddMaxSpeed extends SimpleOverpassQuestType
{
	private static final String MAXSPEED_TYPE = "maxspeed:type";

	@Inject public AddMaxSpeed(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return
			"ways with highway ~ motorway|trunk|primary|secondary|tertiary|unclassified|residential" +
			" and !maxspeed and !maxspeed:forward and !maxspeed:backward" +
			" and !source:maxspeed and !zone:maxspeed and !maxspeed:type" + // implicit speed limits
			// not any unpaved as they are unlikely developed enough to have speed limits signposted
			" and surface !~" + TextUtils.join("|", OsmTaggings.ANYTHING_UNPAVED) +
			// neither private roads nor roads that are not for cars
			" and motor_vehicle !~ private|no" +
			" and vehicle !~ private|no" +
			" and (access !~ private|no or (foot and foot !~ private|no))";
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
			changes.add(MAXSPEED_TYPE+":advisory", "sign");
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
				changes.add(MAXSPEED_TYPE, country + ":" + roadtype);
			}
			else if (maxspeed != null)
			{
				changes.add(MAXSPEED_TYPE, "sign");
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
