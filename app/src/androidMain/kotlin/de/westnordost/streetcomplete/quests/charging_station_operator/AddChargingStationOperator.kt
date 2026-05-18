package de.westnordost.streetcomplete.quests.charging_station_operator

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
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddChargingStationOperator : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !operator and !name and !brand
          and operator:signed != no
          and brand:signed != no
          and access !~ private|no
    """
    override val changesetComment = "Specify charging station operators"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.quest_charger_operator
    override val title = Res.string.quest_charging_station_operator_title
    override val achievements = listOf(CAR)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station")

    @Composable
    override fun Form(onAnswer: (String) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.chargingStationOperators,
            onClickOk = onAnswer
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
