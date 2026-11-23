package de.westnordost.streetcomplete.quests.bicycle_repair_station

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_amenity
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_station_services_title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleRepairStationServices : OsmElementQuestType<Set<BicycleRepairStationService>> {

    private val filter by lazy { """
        nodes, ways with
          amenity = bicycle_repair_station
          and access !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify features of bicycle repair stations"
    override val wikiLink = "Tag:amenity=bicycle_repair_station"
    override val icon = Res.drawable.quest_bicycle_repair_amenity
    override val title = Res.string.quest_bicycle_repair_station_services_title
    override val achievements = listOf(BICYCLIST)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && (
            !element.hasAnySupportedBicycleServices()
            || bicycleServicesOlderThan2Years.matches(element)
    )

    @Composable
    override fun Form(on: (QuestAction<Set<BicycleRepairStationService>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val initialSelectedItems = remember(element) {
            parseBicycleRepairStationService(element.tags)
        }

        ItemsSelectQuestForm(
            on = on,
            items = BicycleRepairStationService.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            initialSelectedItems = initialSelectedItems,
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
        tags.updateCheckDateForKey("service:bicycle")
    }
}

private val bicycleServicesOlderThan2Years =
    TagOlderThan("service:bicycle", RelativeDate(-(365 * 2).toFloat()))

private fun Element.hasAnySupportedBicycleServices(): Boolean =
    BicycleRepairStationService.entries.any { tags.containsKey(it.value) }
