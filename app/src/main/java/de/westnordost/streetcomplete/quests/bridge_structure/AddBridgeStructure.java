package de.westnordost.streetcomplete.quests.bridge_structure;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBridgeStructure extends SimpleOverpassQuestType
{
	@Inject public AddBridgeStructure(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways with man_made=bridge and !bridge:structure";
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddBridgeStructureForm(); }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		List<String> values = answer.getStringArrayList(AddBridgeStructureForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("bridge:structure", values.get(0));
		}
	}

	@Override public int getTitle(@NonNull Map<String, String> tags) { return R.string.quest_bridge_structure_title; }
	@Override public int getIcon() { return R.drawable.ic_quest_bridge; }
	@Override public String getCommitMessage() { return "Add bridge structures"; }
}
