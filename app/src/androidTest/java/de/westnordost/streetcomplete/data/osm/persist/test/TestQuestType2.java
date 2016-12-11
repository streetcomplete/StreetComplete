package de.westnordost.streetcomplete.data.osm.persist.test;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;

public class TestQuestType2 implements OsmElementQuestType
{
	@Override public boolean appliesTo(Element element)
	{
		return false;
	}
	@Override public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { return 0; }
	@Override public int importance()
	{
		return 0;
	}
	@Override public AbstractQuestAnswerFragment getForm()
	{
		return null;
	}
	@Override public String getIconName() {	return null; }
}