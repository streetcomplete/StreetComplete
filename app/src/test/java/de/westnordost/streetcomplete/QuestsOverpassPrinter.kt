package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.elementfilter.toOverpassQLString
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.questTypeRegistry
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.mock

@Mock
private val dao: WayTrafficFlowDao = mock(classOf<WayTrafficFlowDao>())
@Mock
private val ar: ArSupportChecker = mock(classOf<ArSupportChecker>())

fun main() {
    val registry = questTypeRegistry(TrafficFlowSegmentsApi(HttpClient(MockEngine), ""), dao, ar, { _ -> CountryInfo(emptyList()) }, { _ -> null} )

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
