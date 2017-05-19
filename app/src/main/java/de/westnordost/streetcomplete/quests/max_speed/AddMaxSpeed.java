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
		       "!maxspeed and !source:maxspeed";
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
		int maxspeed = answer.getInt(AddMaxSpeedForm.MAX_SPEED,-1);
		if(maxspeed != -1) changes.add("maxspeed", "" + maxspeed);

		String maxspeedSource = answer.getString(AddMaxSpeedForm.MAX_SPEED_SOURCE);
		if(maxspeedSource != null) changes.add("source:maxspeed", maxspeedSource);
	}

	@Override public String getCommitMessage() { return "Add speed limits"; }

	@Override public String getIconName() { return "max_speed"; }


}
