package de.westnordost.streetcomplete.quests.charging_station_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.painterResource

// Can a charging station, with another purpose also be used by cars?
// Per the wiki, this is asumed to be yes, but it's good to confirm.
class AddChargingStationMotorcar : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !motorcar
          and motor_vehicle != no
          and
          (
             bicycle ~ yes|designated
             or hgv ~ yes|designated
             or motorcycle ~ yes|designated
             or scooter ~ yes|designated
             or bus ~ yes|designated
             or boat ~ yes|designated
          )
          and access !~ private|no
    """
    override val changesetComment = "Specify if cars can charge at charging stations"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = Res.drawable.quest_car_charger
    override val title = Res.string.quest_charging_station_car_access
    override val achievements = listOf(CAR)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["motorcar"] = answer.toYesNo()
    }
}
