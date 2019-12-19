package de.westnordost.streetcomplete.data.osm

import org.junit.Test

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.quests.QuestModule

import org.junit.Assert.*

class SimpleOverpassQuestsValidityTest {

    @Test fun `query valid`() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val questTypes = QuestModule.questTypeRegistry(OsmNoteQuestType(), mock(), mock(), mock(), mock(), mock()).all

        for (questType in questTypes) {
            if (questType is SimpleOverpassQuestType<*>) {
                // if this fails and the returned exception is not informative, catch here and record
                // the name of the SimpleOverpassQuestType
                questType.getOverpassQuery(bbox)
            }
        }
        // parsing the query threw no errors -> valid
        assertTrue(true)
    }
}
