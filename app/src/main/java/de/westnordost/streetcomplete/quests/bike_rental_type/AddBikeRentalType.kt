package de.westnordost.streetcomplete.quests.bike_rental_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags

class AddBikeRentalType : OsmFilterQuestType<BikeRentalTypeAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bicycle_rental
          and access !~ private|no
          and (!bicycle_rental or bicycle_rental = yes)
          and !shop
    """
    override val changesetComment = "Specify bicycle rental types"
    override val wikiLink = "Key:bicycle_rental"
    override val icon = R.drawable.ic_quest_bicycle_rental
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_rental_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bicycle_rental")

    override fun createForm() = AddBikeRentalTypeForm()

    override fun applyAnswerTo(answer: BikeRentalTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BikeRentalType -> {
                tags["bicycle_rental"] = answer.osmValue
                if (answer == BikeRentalType.HUMAN) {
                    tags["shop"] = "rental"
                }
            }
            is BikeShopWithRental -> {
                tags.remove("amenity")
                tags["shop"] = "bicycle"
                tags["service:bicycle:rental"] = "yes"
            }
        }
    }
}
