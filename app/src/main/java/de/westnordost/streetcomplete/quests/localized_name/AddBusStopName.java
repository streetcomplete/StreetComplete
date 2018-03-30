package de.westnordost.streetcomplete.quests.localized_name;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
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
		return "nodes with" +
			" ((public_transport=platform and (bus=yes or trolleybus=yes or tram=yes))" +
			" or" +
			" (highway=bus_stop and public_transport!=stop_position))" +
			" and !name and noname != yes";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddBusStopNameForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddLocalizedNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
		}

		HashMap<String,String> stopNameByLanguage = AddLocalizedNameForm.toNameByLanguage(answer);
		for (Map.Entry<String, String> e : stopNameByLanguage.entrySet())
		{
			if(e.getKey().isEmpty())
			{
				changes.add("name", e.getValue());
			}
			else
			{
				changes.add("name:" + e.getKey(), e.getValue());
			}
		}
	}

	@Override public String getCommitMessage() { return "Determine bus/tram stop names"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bus_stop_name; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		String tram = tags.get("tram");
		return (tram != null && tram.equals("yes")) ? R.string.quest_tramStopName_title : R.string.quest_busStopName_title;
	}
}
