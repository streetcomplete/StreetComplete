package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

open class TestQuestType : OsmElementQuestType<String> {

    override fun getTitle(tags: Map<String, String>) = 0
    override fun isApplicableTo(element: Element):Boolean? = null
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
    override val icon = 0
    override fun createForm(): AbstractQuestAnswerFragment<String> = object : AbstractQuestAnswerFragment<String>() {}
    override val commitMessage = ""
    override fun getApplicableElements(mapData: MapDataWithGeometry) = emptyList<Element>()
    override val wikiLink: String? = null
}
