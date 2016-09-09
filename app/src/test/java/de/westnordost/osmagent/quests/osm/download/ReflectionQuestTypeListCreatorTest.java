package de.westnordost.osmagent.quests.osm.download;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.osm.types.OsmElementQuestType;
import de.westnordost.osmagent.quests.QuestType;

public class ReflectionQuestTypeListCreatorTest extends TestCase
{
	private static final String OVER_THERE = "de.westnordost.osmagent.quests.osm.download.reflectiontest";

	public void testOrder()
	{
		List<OsmElementQuestType> types = ReflectionQuestTypeListCreator.create(
				OsmElementQuestType.class, OVER_THERE);

		int currentImportance = 0;
		for(QuestType questType : types)
		{
			assertTrue(questType.importance() >= currentImportance);
		}
	}

	public void testAmount()
	{
		List<OsmElementQuestType> types = ReflectionQuestTypeListCreator.create(
				OsmElementQuestType.class, OVER_THERE);

		assertEquals(2,types.size());

		List<QuestType> types2 = ReflectionQuestTypeListCreator.create(
				QuestType.class, OVER_THERE);

		assertEquals(3,types2.size());
	}
}
