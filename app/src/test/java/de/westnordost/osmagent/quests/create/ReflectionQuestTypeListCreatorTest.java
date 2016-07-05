package de.westnordost.osmagent.quests.create;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.types.QuestType;

public class ReflectionQuestTypeListCreatorTest extends TestCase
{
	public void testOrder()
	{
		List<QuestType> questTypeList = new ReflectionQuestTypeListCreator().create();
		int currentImportance = 0;
		for(QuestType questType : questTypeList)
		{
			assertTrue(questType.importance() >= currentImportance);
		}
	}

	public void testAmount()
	{
		// not much we can test here without hardcoding every single quest type here. So, let's just
		// update this number from time to time...
		int minAmountOfQuestTypes = 2;
		assertTrue(new ReflectionQuestTypeListCreator().create().size() >= minAmountOfQuestTypes);
	}
}
