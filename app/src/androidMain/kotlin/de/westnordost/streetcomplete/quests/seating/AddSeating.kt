package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddSeating : OsmFilterQuestType<Seating>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          (
            amenity ~ restaurant|cafe|fast_food|ice_cream|food_court|pub|bar
            or shop = bakery
          )
          and takeaway != only
          and (!outdoor_seating or !indoor_seating)
    """
    override val changesetComment = "Survey whether places have seating"
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.quest_seating
    override val title = Res.string.quest_seating_name_title
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_seasonal

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddSeatingForm()

    override fun applyAnswerTo(answer: Seating, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == Seating.TAKEAWAY_ONLY) tags["takeaway"] = "only"
        tags["outdoor_seating"] = answer.hasOutdoorSeating.toYesNo()
        tags["indoor_seating"] = answer.hasIndoorSeating.toYesNo()
    }
}
