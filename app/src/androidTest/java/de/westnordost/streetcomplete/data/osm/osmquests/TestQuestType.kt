package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

open class TestQuestType : OsmElementQuestType<String> {

    override fun isApplicableTo(element: Element): Boolean? = null
    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}
    override val icon = 0
    override fun createForm(): AbstractOsmQuestForm<String> = object : AbstractOsmQuestForm<String>() {}
    override val changesetComment = ""
    override fun getTitle(tags: Map<String, String>) = 0
    override fun getApplicableElements(mapData: MapDataWithGeometry) = emptyList<Element>()
    override val wikiLink: String? = null
    override val achievements = emptyList<EditTypeAchievement>()
}
