package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags

class AddFireHydrantType : OsmFilterQuestType<FireHydrantType>() {

    override val elementFilter = "nodes with emergency = fire_hydrant and !fire_hydrant:type"
    override val changesetComment = "Specify fire hydrant types"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = R.drawable.ic_quest_fire_hydrant
    override val isDeleteElementEnabled = true
    override val achievements = listOf(LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fireHydrant_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with emergency = fire_hydrant")

    override fun createForm() = AddFireHydrantTypeForm()

    override fun applyAnswerTo(answer: FireHydrantType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fire_hydrant:type"] = answer.osmValue
    }
}
