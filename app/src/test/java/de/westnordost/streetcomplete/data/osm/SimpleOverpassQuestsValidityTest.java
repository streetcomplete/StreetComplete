package de.westnordost.streetcomplete.data.osm;

import org.junit.Test;

import java.util.List;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.quests.QuestModule;

import static org.junit.Assert.*;

public class SimpleOverpassQuestsValidityTest
{
	@Test public void queryValid()
	{
		List<QuestType> questTypes = QuestModule.questTypeRegistry(new OsmNoteQuestType(),
				null,null,null,null,null).getAll();
		for(QuestType questType : questTypes)
		{
			if(questType instanceof SimpleOverpassQuestType)
			{
				// if this fails and the returned exception is not informative, catch here and record
				// the name of the SimpleOverpassQuestType
				((SimpleOverpassQuestType) questType).getOverpassQuery(null);
			}
		}
		// parsing the query threw no errors -> valid
		assertTrue(true);
	}
}
