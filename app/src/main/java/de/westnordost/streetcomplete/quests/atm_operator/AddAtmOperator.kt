package de.westnordost.streetcomplete.quests.atm_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddAtmOperator : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = atm and !operator and !name and !brand"
    override val changesetComment = "Specify ATM operator"
    override val wikiLink = "Tag:amenity=atm"
    override val icon = R.drawable.ic_quest_money
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_atm_operator_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = atm")

    override fun createForm() = AddAtmOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
