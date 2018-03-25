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
		if(answer.getBoolean(AddBusStopNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
		}

		String[] names = answer.getStringArray(AddRoadNameForm.NAMES);
		String[] languages = answer.getStringArray(AddRoadNameForm.LANGUAGE_CODES);

		HashMap<String,String> stopNameByLanguage = toStopNameByLanguage(names, languages);
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

	private static HashMap<String,String> toStopNameByLanguage(String[] names, String[] languages)
	{
		HashMap<String,String> result = new HashMap<>();
		result.put("", names[0]);
		// add languages only if there is more than one name specified. If there is more than one
		// name, the "main" name (name specified in top row) is also added with the language.
		if(names.length > 1)
		{
			for (int i = 0; i < names.length; i++)
			{
				// (the first) element may have no specific language
				if(!languages[i].isEmpty())
				{
					result.put(languages[i], names[i]);
				}
			}
		}
		return result;
	}

	@Override public String getCommitMessage() { return "Determine bus/tram stop names"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bus_stop_name; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		String tram = tags.get("tram");
		return (tram != null && tram.equals("yes")) ? R.string.quest_tramStopName_title : R.string.quest_busStopName_title;
	}
}
