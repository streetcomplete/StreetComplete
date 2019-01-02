package de.westnordost.streetcomplete.data.osm.persist.test

import android.os.Bundle
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

open class TestQuestType : OsmElementQuestType {

    override fun getTitle(tags: Map<String, String>) = 0
    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler) = false
    override fun isApplicableTo(element: Element):Boolean? = null
    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {}
    override val icon = 0
    override fun createForm(): AbstractQuestAnswerFragment = object : AbstractQuestAnswerFragment() {}
    override val commitMessage = ""
}
