package de.westnordost.streetcomplete

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.QuestModule

fun main() {

    val overpassMock: OverpassMapDataAndGeometryDao = mock()
    on(overpassMock.query(any(), any())).then { invocation ->
        var query = invocation.getArgument(0) as String
        // make query overpass-turbo friendly
        query = query
            .replace("0,0,1,1", "{{bbox}}")
            .replace("out meta geom 2000;", "out meta geom;")
        print("```\n$query\n```\n")
        true
    }

    val registry = QuestModule.questTypeRegistry(mock(), overpassMock, mock(), mock(), mock(), mock())

    val bbox = BoundingBox(0.0,0.0,1.0,1.0)

    for (questType in registry.all) {
        if (questType is OsmElementQuestType) {
            println("### " + questType.javaClass.simpleName)
            if (questType is SimpleOverpassQuestType) {
                val filters = questType.tagFilters.trimIndent()
                println("<details>\n<summary>Tag Filters</summary>\n\n```\n$filters\n```\n</details>\n")
            }
            questType.download(bbox, mock())
            println()
        }
    }
}
