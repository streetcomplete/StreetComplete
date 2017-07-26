package de.westnordost.streetcomplete.quests.orchard_produce;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddOrchardProduce extends SimpleOverpassQuestType
{
	public AddOrchardProduce(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return "ways, relations with landuse = orchard and !trees and !produce and !crop";
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddOrchardProduceForm(); }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		// TODO
	}

	@Override public String getCommitMessage() { return "Add orchard produces"; }
	@Override public String getIconName() { return "apple"; }
	@Override public int importance() {	return QuestImportance.EXTRA; }
}
