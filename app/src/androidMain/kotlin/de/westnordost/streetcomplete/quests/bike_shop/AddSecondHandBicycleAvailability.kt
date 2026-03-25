package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*

class AddSecondHandBicycleAvailability : OsmFilterQuestType<SecondHandBicycleAvailability>(), AndroidQuest {
    override val elementFilter = """
        nodes, ways with shop = bicycle
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
    override val icon = R.drawable.quest_bicycle_second_hand
    override val title = Res.string.quest_bicycle_shop_second_hand_title
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddSecondHandBicycleAvailabilityForm()

    override fun applyAnswerTo(answer: SecondHandBicycleAvailability, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.osmValue == null) {
            tags.updateWithCheckDate("service:bicycle:retail", "no")
        } else {
            tags.updateWithCheckDate("service:bicycle:retail", "yes")
            tags["service:bicycle:second_hand"] = answer.osmValue
        }
    }
}
