package de.westnordost.streetcomplete.data.osm.persist.test;

import android.os.Bundle;

import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;

public class TestQuestType implements OsmElementQuestType
{
	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }

	@Override public String getCommitMessage() { return null; }
	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return false;
	}
	@Override public AbstractQuestAnswerFragment createForm()
	{
		return null;
	}
	@Override public int getIcon() { return 0; }
	@Override public int getTitle() { return 0; }
	@Override public int getTitle(Map<String,String> tags) { return 0; }
	@Override public boolean appliesTo(Element element) { return false; }
}