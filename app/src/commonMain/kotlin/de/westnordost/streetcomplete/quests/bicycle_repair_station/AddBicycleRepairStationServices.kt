package de.westnordost.streetcomplete.quests.bicycle_repair_station

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleRepairStationServices : OsmFilterQuestType<Set<BicycleRepairStationService>>() {

    override val elementFilter = """
        nodes, ways with
        amenity = bicycle_repair_station
        and
        (
          !service:bicycle:pump
          or !service:bicycle:stand
          or !service:bicycle:tools
          or !service:bicycle:chain_tool
          or older today -2 years
        )
        and access !~ private|no
    """

    override val changesetComment = "Specify features of bicycle repair stations"
    override val wikiLink = "Tag:amenity=bicycle_repair_station"
    override val icon = Res.drawable.quest_bicycle_repair_amenity
    override val title = Res.string.quest_bicycle_repair_station_services_title
    override val achievements = listOf(BICYCLIST)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<Set<BicycleRepairStationService>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemsSelectQuestForm(
            items = BicycleRepairStationService.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onAnswer = onAnswer,
        )
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with
            amenity ~ bicycle_repair_station|compressed_air
        """)

    override fun applyAnswerTo(answer: Set<BicycleRepairStationService>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        for (entry in BicycleRepairStationService.entries) {
            tags["service:bicycle:${entry.value}"] = (entry in answer).toYesNo()
        }
        tags.updateCheckDate()
    }
}
