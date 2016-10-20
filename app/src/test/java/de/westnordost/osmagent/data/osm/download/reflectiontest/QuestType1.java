package de.westnordost.osmagent.data.osm.download.reflectiontest;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;

// for ReflectionQuestTypeListCreatorTest
public class QuestType1 implements QuestType
{
	@Override public int importance() { return -1; }
	@Override public AbstractQuestAnswerFragment getForm() { return null; }
	@Override public String getIconName() {	return null; }
}
