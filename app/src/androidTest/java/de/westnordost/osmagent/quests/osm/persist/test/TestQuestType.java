package de.westnordost.osmagent.quests.osm.persist.test;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmagent.quests.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
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
	@Override public DialogFragment getDialog()
	{
		return null;
	}
}