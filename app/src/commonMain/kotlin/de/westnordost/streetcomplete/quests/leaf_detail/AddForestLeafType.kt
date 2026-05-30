package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.math.measuredMultiPolygonArea
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddForestLeafType : OsmElementQuestType<ForestLeafType> {
    private val areaFilter by lazy { """
        ways, relations with (landuse = forest or natural = wood) and !leaf_type
    """.toElementFilterExpression() }

    private val wayFilter by lazy { """
        ways with natural = tree_row and !leaf_type
    """.toElementFilterExpression() }

    override val changesetComment = "Specify leaf types"
    override val wikiLink = "Key:leaf_type"
    override val icon = Res.drawable.quest_leaf
    override val title = Res.string.quest_leafType_title
    override val achievements = listOf(OUTDOORS)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val forests = mapData
            .filter { areaFilter.matches(it) }
            .filter {
                val geometry = mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry
                val area = geometry?.polygons?.measuredMultiPolygonArea() ?: 0.0
                area > 0.0 && area < 10000
            }
        val treeRows = mapData.filter { wayFilter.matches(it) }
        return forests + treeRows
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (wayFilter.matches(element)) return true // tree rows
        // for areas, we don't want to show things larger than x m², we need the geometry for that
        if (!areaFilter.matches(element)) return false
        return null
    }

    @Composable
    override fun Form(onAnswer: (QuestAnswer<ForestLeafType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = ForestLeafType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onAnswer = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: ForestLeafType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["leaf_type"] = answer.osmValue
    }
}
