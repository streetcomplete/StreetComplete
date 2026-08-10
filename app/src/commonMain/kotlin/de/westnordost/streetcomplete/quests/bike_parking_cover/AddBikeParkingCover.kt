package de.westnordost.streetcomplete.quests.bike_parking_cover

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
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBikeParkingCover : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
            amenity = bicycle_parking
            or amenity = charging_station and bicycle ~ yes|designated and !lockable and motorcar=no
        )
        and access !~ private|no
        and !covered
        and bicycle_parking !~ shed|lockers|building
    """
    override val changesetComment = "Specify bicycle parkings covers"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = Res.drawable.quest_bicycle_parking_cover
    override val title = Res.string.quest_bicycleParkingCoveredStatus_title
    override val achievements = listOf(BICYCLIST)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bicycle_parking")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["covered"] = answer.toYesNo()
    }
}
