package de.westnordost.streetcomplete.quests.traffic_signals_button;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddTrafficSignalsButton extends SimpleOverpassQuestType {
	@Inject public AddTrafficSignalsButton(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with highway=crossing and crossing=traffic_signals and !button_operated";
	}

	public AbstractQuestAnswerFragment createForm() { return new YesNoQuestAnswerFragment(); }

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER))
		{
			changes.add("button_operated", "yes");
		} else {
			changes.add("button_operated", "no");
		}
	}

	@Override public String getCommitMessage() { return "add whether traffic signals have a button for pedestrians"; }
	@Override public int getIcon() { return R.drawable.ic_quest_traffic_lights; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_traffic_signals_button_title;
	}
}
