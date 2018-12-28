package de.westnordost.streetcomplete.quests.traffic_signals_sound;

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

public class AddTrafficSignalsSound extends SimpleOverpassQuestType {
	@Inject public AddTrafficSignalsSound(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with highway=crossing and crossing=traffic_signals and !traffic_signals:sound";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
			changes.add("traffic_signals:sound", "yes");
		} else {
			changes.add("traffic_signals:sound", "no");
		}
	}

	@Override public String getCommitMessage() { return "add traffic_signals:sound tag"; }
	@Override public int getIcon() { return R.drawable.ic_quest_blind_traffic_lights; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_traffic_signals_sound_title;
	}
}
