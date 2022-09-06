package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddSecondHandBicycleAvailability : OsmFilterQuestType<SecondHandBicycleAvailability>() {
    override val elementFilter = """
        nodes, ways, relations with shop = bicycle
        and (
            !service:bicycle:second_hand
            or service:bicycle:second_hand older today -6 years
        )
        and (
            service:bicycle:retail != no
            or service:bicycle:retail older today -6 years
        )
        and !second_hand
        and access !~ private|no
        """

    override val changesetComment = "Survey whether bicycle shop sells second-hand bicycles"
    override val wikiLink = "Tag:service:bicycle:second_hand"
    override val icon = R.drawable.ic_quest_bicycle_second_hand
    override val isReplaceShopEnabled = true
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_shop_second_hand_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddSecondHandBicycleAvailabilityForm()

    override fun applyAnswerTo(answer: SecondHandBicycleAvailability, tags: Tags, timestampEdited: Long) {
        if (answer.osmValue == null) {
            tags.updateWithCheckDate("service:bicycle:retail", "no")
        } else {
            tags.updateWithCheckDate("service:bicycle:retail", "yes")
            tags["service:bicycle:second_hand"] = answer.osmValue
        }
    }
}
