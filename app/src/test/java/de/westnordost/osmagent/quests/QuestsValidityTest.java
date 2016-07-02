package de.westnordost.osmagent.quests;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.types.QuestType;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<QuestType> questTypes = new ReflectionQuestTypeListBuilder().build();
		for(QuestType questType : questTypes)
		{
			// if this fails and the returned exception is not informative, catch here and record
			// the name of the QuestType
			questType.getOverpassQuery(null);
		}
		// parsing the query threw no errors -> valid
		assertTrue(true);
	}
}
