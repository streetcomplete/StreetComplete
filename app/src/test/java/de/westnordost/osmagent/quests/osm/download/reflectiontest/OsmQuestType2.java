package de.westnordost.osmagent.quests.osm.download.reflectiontest;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmagent.quests.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmapi.map.data.Element;

// for ReflectionQuestTypeListCreatorTest
public class OsmQuestType2 extends OsmQuestType1
{
	@Override public boolean appliesTo(Element element)	{ return false; }
	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }
	@Override public int getCommitMessageResourceId() { return 0; }
	@Override public int importance() { return 1; }
	@Override public DialogFragment getDialog() { return null; }
}