package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.elementfilter.toOverpassQLString
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.questTypeRegistry
import de.westnordost.streetcomplete.testutils.mock

fun main() {
    val registry = questTypeRegistry(mock(), mock(), mock(), mock(), mock())

    for (questType in registry) {
        if (questType is OsmElementQuestType<*>) {
            println("### " + questType.name)
            if (questType is OsmFilterQuestType<*>) {
                val query = "[bbox:{{bbox}}];\n" + questType.filter.toOverpassQLString() + "\n out meta geom;"
                println("```\n$query\n```")
            } else {
                println("Not available, see source code")
            }
            println()
        }
    }
}
