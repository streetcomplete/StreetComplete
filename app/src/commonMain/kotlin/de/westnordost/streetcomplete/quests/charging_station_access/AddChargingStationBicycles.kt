package de.westnordost.streetcomplete.quests.charging_station_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bicycle_charger
import de.westnordost.streetcomplete.resources.quest_charging_station_bicycles_hint2
import de.westnordost.streetcomplete.resources.quest_charging_station_bicycles_title
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
            ${socketsEligibleForBicycleCharging.joinToString(" or ") {
                "socket:$it = yes or socket:$it > 0"
            }}
          )
    """
    override val changesetComment = "Specify whether bicycles can be charged at charging stations"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = Res.drawable.quest_bicycle_charger
    override val title = Res.string.quest_charging_station_bicycles_title
    override val achievements = listOf(EditTypeAchievement.BICYCLIST)
    override val hint = Res.string.quest_charging_station_bicycles_hint2

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station")

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bicycle"] = answer.toYesNo()
    }
}

private val socketsEligibleForBicycleCharging = listOf(
    // specifically for bicycles
    "bosch_3pin",
    "bosch_5pin",
    "bosch_smart",
    "ropd",
    "shimano_steps_5pin",
    "xlr_3pin_cable",

    // USB-C can deliver quite some watts
    "socket:device:USB-C",

    // domestic. Barrel without bottom. Just the used ones according to taginfo
    "domestic",
    "typea", "nema_1_15",
    "typeb", "nema_5_15", "nema_5_20",
    "typec",
    "typed",
    "typee",
    "typef", "schuko",
    "typeg", "bs1363",
    "typeh",
    "typei", "as3112",
    "typej", "sev1011_t13", "sev1011_t23",
    "typek",
    "typeL", "cei23_50_s_11",
    "typem",
    "typen",
    "typeo",
)
