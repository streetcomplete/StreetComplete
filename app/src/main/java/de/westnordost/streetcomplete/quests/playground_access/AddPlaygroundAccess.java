package de.westnordost.streetcomplete.quests.playground_access;

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

public class AddPlaygroundAccess extends SimpleOverpassQuestType
{
	@Inject public AddPlaygroundAccess(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with leisure=playground and (!access or access=unknown)";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
			changes.add("access", "yes");
		} else {
			changes.add("access", "private");
		}
	}
	@Override public String getCommitMessage() { return "Add playground access"; }
	@Override public int getIcon() { return R.drawable.ic_quest_playground; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_playground_access_title;
	}
}
