package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION

class AddSeating : OsmFilterQuestType<Seating>() {

    override val elementFilter = """
        nodes, ways with
          amenity ~ restaurant|cafe|fast_food|ice_cream|food_court|pub|bar
          and takeaway != only
          and (!outdoor_seating or !indoor_seating)
    """
    override val changesetComment = "Add seating info"
    override val defaultDisabledMessage = R.string.default_disabled_msg_summer_outdoor_seating
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.ic_quest_seating
    override val isReplaceShopEnabled = true
    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_seating_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddSeatingForm()

    override fun applyAnswerTo(answer: Seating, tags: Tags, timestampEdited: Long) {
        if (answer == Seating.NO) tags["takeaway"] = "only"
        tags["outdoor_seating"] = answer.hasOutdoorSeating.toYesNo()
        tags["indoor_seating"] = answer.hasIndoorSeating.toYesNo()
    }
}
