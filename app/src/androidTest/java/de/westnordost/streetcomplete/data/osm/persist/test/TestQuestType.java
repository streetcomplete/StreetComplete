package de.westnordost.streetcomplete.data.osm.persist.test;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;

public class TestQuestType implements OsmElementQuestType
{
	@Override public boolean appliesTo(Element element)
	{
		return false;
	}
	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }
	@Override public String getCommitMessage() { return null; }
	@Override public int importance()
	{
		return 0;
	}
	@Override public AbstractQuestAnswerFragment createForm()
	{
		return null;
	}
	@Override public String getIconName() {	return null; }
}