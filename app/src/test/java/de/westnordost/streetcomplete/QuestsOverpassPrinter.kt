package de.westnordost.streetcomplete

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.*
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.quests.QuestModule
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import org.mockito.Mockito.mock
import java.util.concurrent.FutureTask

fun main() {

    val overpassMock = mock(OverpassMapDataDao::class.java)
    on(overpassMock.getAndHandleQuota(any(), any())).then { invocation ->
        var query = invocation.getArgument(0) as String
        query = query.replace("0,0,1,1", "{{bbox}}").replace(";", ";\n").replace("\n(", "\n(\n")
        print("```\n$query```\n")
        true
    }

    val registry = QuestModule.questTypeRegistry(
        mock(OsmNoteQuestType::class.java),
        overpassMock,
        mock(RoadNameSuggestionsDao::class.java),
        mock(PutRoadNameSuggestionsHandler::class.java),
        mock(TrafficFlowSegmentsDao::class.java),
        mock(WayTrafficFlowDao::class.java),
        mock(FutureTask::class.java) as FutureTask<FeatureDictionary>
    )

    val bbox = BoundingBox(0.0,0.0,1.0,1.0)

    for (questType in registry.all) {
        if (questType is OsmElementQuestType) {
            println("### " + questType.javaClass.simpleName)
            questType.download(bbox, mock(MapDataWithGeometryHandler::class.java))
            println()
        }
    }
}
