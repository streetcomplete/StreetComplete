package de.westnordost.osmagent.quests.osm.download.reflectiontest;

import android.app.DialogFragment;

import de.westnordost.osmagent.quests.QuestType;

// for ReflectionQuestTypeListCreatorTest
public class QuestType1 implements QuestType
{
	@Override public int importance() { return -1; }
	@Override public DialogFragment getDialog() { return null; }
}
