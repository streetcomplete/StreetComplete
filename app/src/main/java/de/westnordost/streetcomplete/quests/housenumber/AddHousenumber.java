package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddHousenumber implements OsmElementQuestType
{
	@Override public int importance() { return QuestImportance.MAJOR; }
	@Override public AbstractQuestAnswerFragment createForm() { return new AddHousenumberForm(); }
	@Override public String getCommitMessage() { return "Add housenumbers"; }
	@Override public String getIconName() { return "housenumber"; }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		// TODO apply
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		// TODO stub
		return false;
	}
}
