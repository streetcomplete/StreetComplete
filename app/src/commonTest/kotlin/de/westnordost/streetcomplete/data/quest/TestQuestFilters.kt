package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.questTypeRegistry
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import kotlin.test.Test
import kotlin.test.assertTrue

class TestQuestFilters {

    @Test
    fun getHighlightedElements_filters_are_valid_and_parsed_correctly() {
        val registry = questTypeRegistry(
            arSupportChecker = object : ArSupportChecker { override fun invoke() = false },
            getCountryInfoByLocation = { CountryInfo("DE", emptyList()) },
            getCountryOrSubdivisionCode = { null },
            getFeature = { null }
        )

        val elementQuestTypes = registry.filterIsInstance<OsmElementQuestType<*>>()
        assertTrue(elementQuestTypes.isNotEmpty())

        val element = node(1)
        val mapData = TestMapDataWithGeometry(emptyList())

        for (questType in elementQuestTypes) {
            val highlightedElements = questType.getHighlightedElements(element, mapData)
            highlightedElements.toList()
        }
    }
}
