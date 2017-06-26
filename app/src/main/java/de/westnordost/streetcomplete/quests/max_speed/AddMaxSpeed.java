package de.westnordost.streetcomplete.quests.max_speed;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
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
		       "motorway|trunk|primary|secondary|tertiary|unclassified|residential and " +
		       "!maxspeed and !source:maxspeed" +
		       // other tags that are used for basically the same thing as source:maxspeed
		       " and !zone:maxspeed and !maxspeed:type";
	}

	@Override public int importance()
	{
		return QuestImportance.MAJOR;
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddMaxSpeedForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String maxspeed = answer.getString(AddMaxSpeedForm.MAX_SPEED);
		if(maxspeed != null)
		{
			changes.add("maxspeed", maxspeed);

		}
		String country = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY);
		String roadtype = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE);
		if(roadtype != null && country != null)
		{
			changes.add("source:maxspeed", country + ":" + roadtype);
		}
		else if(maxspeed != null)
		{
			changes.add("source:maxspeed", "sign");
		}
	}

	@Override public String getCommitMessage() { return "Add speed limits"; }

	@Override public String getIconName() { return "max_speed"; }


}
