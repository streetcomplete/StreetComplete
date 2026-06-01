package de.westnordost.streetcomplete.quests.bike_rental_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddBikeRentalCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = bicycle_rental
         and access !~ private|no
         and bicycle_rental = docking_station
         and (!capacity or capacity older today -6 years)
    """

    override val changesetComment = "Specify bicycle rental capacities"
    override val wikiLink = "Tag:amenity=bicycle_rental"
    override val icon = Res.drawable.quest_bicycle_rental_capacity
    override val title = Res.string.quest_bicycle_rental_capacity_title
    override val achievements = listOf(BICYCLIST)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bicycle_rental")

    @Composable
    override fun Form(on: (QuestAction<Int>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_bicycle),
            on = on
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
