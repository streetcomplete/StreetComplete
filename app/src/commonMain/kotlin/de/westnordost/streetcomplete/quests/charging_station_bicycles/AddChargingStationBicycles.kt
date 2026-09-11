package de.westnordost.streetcomplete.quests.charging_station_bicycles

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

class AddChargingStationBicycles : OsmFilterQuestType<Boolean>() {

    // Charging station with a socket that commonly supports bicycles.
    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !bicycle
          and access !~ private|no
          and (
             socket:ropd > 0
             or socket:bosch_3pin > 0
             or socket:shimano_steps_5pin > 0
             or socket:xlr_3pin_cable > 0
             or socket:domestic > 0
             or socket:typec > 0
             or socket:typee > 0
             or socket:nema5_15 > 0
             or socket:nema_5_20 > 0
             or socket:nema_TT_30 > 0
             or socket:schuko > 0
             or socket:as3112 > 0
             or socket:sev1011_t23 > 0
          )
    """
    override val changesetComment = "Specify whether bicycles can be charged at charging stations"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = Res.drawable.quest_bicycle_charger
    override val title = Res.string.quest_charging_station_bicycles_title
    override val achievements = listOf(BICYCLIST)
    override val hint = Res.string.quest_charging_station_bicycles_hint

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["crossing:island"] = answer.toYesNo()
    }
}
