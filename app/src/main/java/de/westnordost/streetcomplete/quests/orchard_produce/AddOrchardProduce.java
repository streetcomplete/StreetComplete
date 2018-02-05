package de.westnordost.streetcomplete.quests.orchard_produce;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddOrchardProduce extends SimpleOverpassQuestType
{
	public AddOrchardProduce(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways, relations with landuse = orchard and !trees and !produce and !crop";
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddOrchardProduceForm(); }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddOrchardProduceForm.OSM_VALUES);
		if(values != null && !values.isEmpty())
		{
			String produce = values.get(0);

			changes.add("produce", produce);

			if(produce.equals("grape"))
			{
				changes.modify("landuse","vineyard");
			}
			else if(produce.equals("sisal"))
			{
				changes.modify("landuse","farmland");
			}
		}
	}

	@Override public String getCommitMessage() { return "Add orchard produces"; }
	@Override public int getIcon() { return R.drawable.ic_quest_apple; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_orchard_produce_title;
	}
}
