package de.westnordost.osmagent.data.osm.download.reflectiontest;

import android.os.Bundle;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.quests.OsmElementQuestType;
import de.westnordost.osmapi.map.data.Element;

// for ReflectionQuestTypeListCreatorTest
public class OsmQuestType1 implements QuestType, OsmElementQuestType
{
	@Override public boolean appliesTo(Element element)	{ return false; }
	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }
	@Override public int getCommitMessageResourceId() { return 0; }
	@Override public int importance() { return 0; }
	@Override public AbstractQuestAnswerFragment getForm() { return null; }
	@Override public String getIconName() {	return null; }
}