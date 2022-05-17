package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
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

    override val changesetComment = "Add type of parking access"
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
    override val wikiLink = "Tag:service:bicycle:second_hand"
    override val icon = R.drawable.ic_quest_parking_access // TODO ADD AN ICON
    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_shop_second_hand_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddSecondHandBicycleAvailabilityForm()

    override fun applyAnswerTo(answer: SecondHandBicycleAvailability, tags: Tags, timestampEdited: Long) {
        when (answer) {
            SecondHandBicycleAvailability.ONLY_NEW -> {
                tags.updateWithCheckDate("service:bicycle:retail", "yes")
                tags["service:bicycle:second_hand"] = "no"
            }
            SecondHandBicycleAvailability.NEW_AND_SECOND_HAND -> {
                tags.updateWithCheckDate("service:bicycle:retail", "yes")
                tags["service:bicycle:second_hand"] = "yes"
            }
            SecondHandBicycleAvailability.ONLY_SECOND_HAND -> {
                tags.updateWithCheckDate("service:bicycle:retail", "yes")
                tags["service:bicycle:second_hand"] = "only"
            }
            SecondHandBicycleAvailability.NO_BICYCLES_SOLD -> {
                tags.updateWithCheckDate("service:bicycle:retail", "no")
            }
        }
    }
}
