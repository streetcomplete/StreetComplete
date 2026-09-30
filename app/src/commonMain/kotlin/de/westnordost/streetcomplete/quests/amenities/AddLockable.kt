package de.westnordost.streetcomplete.quests.amenities

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
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddLockable : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
          (
            amenity = device_charging_station
            or
            (
                amenity = charging_station
                and motorcar=no
                and bicycle|scooter ~ yes|designated
            )
          )
          and !lockable
          and access !~ private|no
    """

    override val changesetComment = "Specify whether an object can be locked"
    override val wikiLink = "Key:lockable"
    override val icon = Res.drawable.quest_access
    override val title = Res.string.quest_lockable_title
    override val achievements = listOf(BICYCLIST)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity ~ device_charging_station|charging_station")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["lockable"] = answer.toYesNo()
    }
}
