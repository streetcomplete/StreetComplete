package de.westnordost.streetcomplete.quests.bike_parking_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddBikeParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = bicycle_parking
         and access !~ private|no
         and bicycle_parking !~ floor|informal
         and (
           !capacity
           or (
             bicycle_parking ~ stands|wall_loops|safe_loops|handlebar_holder
             and capacity older today -8 years
           )
         )
    """
    /* Bike capacity may change more often for stands and wheelbenders as adding or
       removing a few of them is minor work
     */

    override val changesetComment = "Specify bicycle parking capacities"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.quest_bicycle_parking_capacity
    override val title = Res.string.quest_bikeParkingCapacity_title
    override val achievements = listOf(BICYCLIST)
    override val hint = Res.string.quest_bikeParkingCapacity_hint

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bicycle_parking")

    @Composable
    override fun Form(onAnswer: (Int) -> Unit) {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_bicycle),
            onClickOk = onAnswer
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
