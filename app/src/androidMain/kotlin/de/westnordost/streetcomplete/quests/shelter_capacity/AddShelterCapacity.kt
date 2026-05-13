package de.westnordost.streetcomplete.quests.shelter_capacity

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddShelterCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
          (
            (
              amenity = shelter
              and shelter_type = basic_hut
            )
            or tourism = wilderness_hut
          )
          and !capacity
          and !capacity:persons
          and access !~ private|no
    """
    override val changesetComment = "Specify shelter capacities"
    override val wikiLink = "Tag:amenity=shelter"
    override val icon = R.drawable.quest_shelter_capacity
    override val title = Res.string.quest_shelter_capacity_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = shelter")

    @Composable
    override fun Form(onAnswer: (Int) -> Unit) {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_sleeping_bag),
            onClickOk = onAnswer
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
