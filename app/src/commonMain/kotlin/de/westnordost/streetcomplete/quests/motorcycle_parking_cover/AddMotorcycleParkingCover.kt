package de.westnordost.streetcomplete.quests.motorcycle_parking_cover

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddMotorcycleParkingCover : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with amenity = motorcycle_parking
        and access !~ private|no
        and !covered
        and motorcycle_parking !~ shed|garage_boxes|building
    """
    override val changesetComment = "Specify motorcycle parkings covers"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = Res.drawable.quest_motorcycle_parking_cover
    override val title = Res.string.quest_motorcycleParkingCoveredStatus_title
    override val achievements = listOf(CAR)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = motorcycle_parking")

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["covered"] = answer.toYesNo()
    }
}
