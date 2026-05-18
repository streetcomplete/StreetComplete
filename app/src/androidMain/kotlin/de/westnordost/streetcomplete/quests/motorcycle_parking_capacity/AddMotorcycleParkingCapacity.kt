package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddMotorcycleParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with amenity = motorcycle_parking
         and access !~ private|no
         and (!capacity or capacity older today -4 years)
         and markings != no
    """
    override val changesetComment = "Specify motorcycle parking capacities"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = R.drawable.quest_motorcycle_parking_capacity
    override val title = Res.string.quest_motorcycleParkingCapacity_title
    override val achievements = listOf(CAR)

    @Composable
    override fun Form(onAnswer: (Int) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_motorcycle),
            onClickOk = onAnswer
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = motorcycle_parking")

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
