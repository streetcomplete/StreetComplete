package de.westnordost.osmagent.data.osm.persist.test;

import android.os.Bundle;

import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.data.osm.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;

public class TestQuestType implements OsmElementQuestType
{
	@Override public boolean appliesTo(Element element)
	{
		return false;
	}
	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }
	@Override public int getCommitMessageResourceId()
	{
		return 0;
	}
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