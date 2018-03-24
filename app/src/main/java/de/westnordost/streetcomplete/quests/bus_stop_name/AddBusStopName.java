package de.westnordost.streetcomplete.quests.bus_stop_name;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBusStopName extends SimpleOverpassQuestType
{

	@Inject public AddBusStopName(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return " nodes, ways with !name and noname != yes" +
			   " and ( highway = bus_stop or public_transport = platform )";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddBusStopNameForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddBusStopNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
		}

		String name = answer.getString(AddBusStopNameForm.NAME);
		if(name != null) changes.add("name", name);
	}

	@Override public String getCommitMessage() { return "Determine bus stop names"; }
	@Override public int getIcon() { return R.drawable.ic_quest_label; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_busStopName_title;
	}
}
