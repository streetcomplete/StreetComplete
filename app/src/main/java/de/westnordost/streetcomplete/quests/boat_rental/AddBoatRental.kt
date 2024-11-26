package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags

class AddBoatRental : OsmFilterQuestType<List<BoatRental>>() {

    override val elementFilter = """
        nodes, ways with
        amenity = boat_rental
        and (
          ${BoatRental.entries.joinToString(" and ") { "!${it.osmValue}" }}
          or ${DEPRECATED_RENTALS.joinToString(" or ")}
        )
    """
    override val changesetComment = "Specify boats for rental"
    override val wikiLink = "Tag:amenity=boat_rental"
    override val icon = R.drawable.ic_quest_boat
    override val achievements = listOf(OUTDOORS, RARE)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_boat_rental_title

    override fun createForm() = AddBoatRentalForm()

    override fun applyAnswerTo(answer: List<BoatRental>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.forEach { tags[it.osmValue] = "yes" }
        // remove ambiguous ones that should have been specified correctly by the user's answer
        DEPRECATED_RENTALS.forEach { tags.remove(it) }
    }
}

private val DEPRECATED_RENTALS = listOf(
    // ambiguous:
    // motor or rowing?        what kind of board?          rowing what?
    "dinghy_rental", "paddleboard_rental", "board_rental", "rowing_rental"
)
