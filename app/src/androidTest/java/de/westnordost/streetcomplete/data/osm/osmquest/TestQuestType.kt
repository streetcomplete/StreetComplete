package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

open class TestQuestType : OsmElementQuestType<String> {

    override fun getTitle(tags: Map<String, String>) = 0
    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit) = false
    override fun isApplicableTo(element: Element):Boolean? = null
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
    override val icon = 0
    override fun createForm(): AbstractQuestAnswerFragment<String> = object : AbstractQuestAnswerFragment<String>() {}
    override val commitMessage = ""
}
