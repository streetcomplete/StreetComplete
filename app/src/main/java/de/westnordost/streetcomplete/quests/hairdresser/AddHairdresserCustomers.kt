package de.westnordost.streetcomplete.quests.hairdresser

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace

class AddHairdresserCustomers : OsmFilterQuestType<HairdresserCustomers>() {

    override val elementFilter = """
        nodes, ways with
          (
              shop = hairdresser
              and !female and !male
              and !male:signed and !female:signed
          )
    """
    override val changesetComment = "Survey hairdresser's customers"
    override val wikiLink = "Tag:shop=hairdresser"
    override val icon = R.drawable.ic_quest_hairdresser
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_hairdresser_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddHairdresserCustomersForm()

    override fun applyAnswerTo(answer: HairdresserCustomers, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == HairdresserCustomers.NOT_SIGNED) {
            tags["male:signed"] = "no"
            tags["female:signed"] = "no"
        } else {
            if (answer.isMale) tags["male"] = "yes"
            if (answer.isFemale) tags["female"] = "yes"
        }
        tags.remove("unisex")
    }
}
