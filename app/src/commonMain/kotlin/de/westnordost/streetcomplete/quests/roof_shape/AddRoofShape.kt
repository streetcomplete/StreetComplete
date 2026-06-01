package de.westnordost.streetcomplete.quests.roof_shape

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRoofShape(
    private val getCountryInfoByLocation: (location: LatLon) -> CountryInfo,
) : OsmElementQuestType<RoofShape> {

    private val filter by lazy { """
        ways, relations with
          ((building:levels or roof:levels) or (building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}))
          and !roof:shape and !3dr:type and !3dr:roof
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify roof shapes"
    override val wikiLink = "Key:roof:shape"
    override val icon = Res.drawable.quest_roof_shape
    override val title = Res.string.quest_roofShape_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_roofShape

    override fun getApplicableElements(mapData: MapDataWithGeometry) =
        mapData.filter { element ->
            filter.matches(element) && (
                (element.tags["roof:levels"]?.toFloatOrNull() ?: 0f) > 0f
                    || roofsAreUsuallyFlatAt(element, mapData) == false
            )
        }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        /* if it has 0 roof levels, or the roof levels aren't specified,
           the quest should only be shown in certain countries. But whether
           the element is in a certain country cannot be ascertained without the element's geometry */
        if ((element.tags["roof:levels"]?.toFloatOrNull() ?: 0f) == 0f) return null
        return true
    }

    private fun roofsAreUsuallyFlatAt(element: Element, mapData: MapDataWithGeometry): Boolean? {
        val center = mapData.getGeometry(element.type, element.id)?.center ?: return null
        return getCountryInfoByLocation(center).roofsAreUsuallyFlat
    }

    @Composable
    override fun Form(on: (QuestAction<RoofShape>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = remember { RoofShape.entries - MANY },
            itemsPerRow = 4,
            itemContent = { Image(painterResource(it.icon), null) },
            on = on,
            favoriteKey = "AddRoofShapeForm",
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_roofShape_answer_many)) { on(Answer(MANY)) }
            )
        )
    }

    override fun applyAnswerTo(answer: RoofShape, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["roof:shape"] = answer.osmValue
    }
}
