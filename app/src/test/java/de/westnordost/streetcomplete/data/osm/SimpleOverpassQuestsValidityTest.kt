package de.westnordost.streetcomplete.data.osm

import org.junit.Test
import java.util.concurrent.FutureTask

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.quests.QuestModule
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao

import org.junit.Assert.*
import org.mockito.Mockito.*

class SimpleOverpassQuestsValidityTest {

    @Test fun `query valid`() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val questTypes = QuestModule.questTypeRegistry(
            OsmNoteQuestType(),
	        mock(OverpassMapDataDao::class.java),
	        mock(RoadNameSuggestionsDao::class.java),
	        mock(PutRoadNameSuggestionsHandler::class.java),
	        mock(TrafficFlowSegmentsDao::class.java),
	        mock(WayTrafficFlowDao::class.java),
	        mock(FutureTask::class.java) as FutureTask<FeatureDictionary>
        ).all

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
