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
        (
          amenity = boat_rental
        )
        and ${ALL_RENTALS.joinToString(" and ") { "!$it" }}
    """
    override val changesetComment = "Specify boat types for rental"
    override val wikiLink = "Tag:amenity=boat_rental"
    override val icon = R.drawable.ic_quest_boat_rental
    override val achievements = listOf(OUTDOORS, RARE)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_boat_rental_title

    override fun createForm() = AddBoatRentalForm()

    override fun applyAnswerTo(answer: List<BoatRental>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.forEach{ tags[it.osmValue] = "yes" }
    }
}

private val ALL_RENTALS =
    setOf("canoe_rental", "kayak_rental", "pedalboard_rental",
        "motorboat_rental", "standup_paddleboard_rental", "sailboat_rental",
        "jetski_rental", "houseboat_rental", "dinghy_rental")
