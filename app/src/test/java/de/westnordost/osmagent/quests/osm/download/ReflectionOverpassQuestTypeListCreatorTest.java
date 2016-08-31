package de.westnordost.osmagent.quests.osm.download;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;
import de.westnordost.osmagent.quests.QuestType;

public class ReflectionOverpassQuestTypeListCreatorTest extends TestCase
{
	public void testOrder()
	{
		List<OverpassQuestType> questTypeList = new ReflectionOverpassQuestTypeListCreator().create();
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
		assertTrue(new ReflectionOverpassQuestTypeListCreator().create().size() >= minAmountOfQuestTypes);
	}
}
