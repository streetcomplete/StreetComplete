package de.westnordost.streetcomplete.quests.tactile_paving;

import android.os.Bundle;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
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
		return new TactilePavingCrosswalkForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("tactile_paving", yesno);
	}

	@Override public String getCommitMessage() { return "Add tactile pavings on crosswalks"; }
	@Override public int getIcon() { return R.drawable.ic_quest_blind_pedestrian_crossing; }
	@Override public int getTitle(Map<String, String> tags)
	{
		return R.string.quest_tactilePaving_title_crosswalk;
	}
}
