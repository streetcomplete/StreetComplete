package de.westnordost.streetcomplete.quests.charging_station_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddChargingStationOperator : OsmFilterQuestType<String>(), AndroidQuest {

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
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CAR)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station")

    override fun createForm() = AddChargingStationOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
