package de.westnordost.streetcomplete

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
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
        // make query overpass-turbo friendly
        query = query
            .replace("0,0,1,1", "{{bbox}}")
            .replace("out meta geom 2000;", "out meta geom;")
        print("```\n$query\n```\n")
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
            if (questType is SimpleOverpassQuestType) {
                val filters = questType.tagFilters.trimIndent()
                println("<details>\n<summary>Tag Filters</summary>\n\n```\n$filters\n```\n</details>\n")
            }
            questType.download(bbox, mock(MapDataWithGeometryHandler::class.java))
            println()
        }
    }
}
