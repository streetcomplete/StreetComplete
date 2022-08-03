package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import java.util.concurrent.FutureTask

class AddRoofShape(
    private val countryInfos: CountryInfos,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
) : OsmElementQuestType<RoofShape> {

    private val filter by lazy { """
        ways, relations with (building:levels or roof:levels)
          and !roof:shape and !3dr:type and !3dr:roof
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify roof shapes"
    override val wikiLink = "Key:roof:shape"
    override val icon = R.drawable.ic_quest_roof_shape
    override val defaultDisabledMessage = R.string.default_disabled_msg_roofShape
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofShape_title

    override fun createForm() = AddRoofShapeForm()

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
        return countryInfos.getByLocation(
            countryBoundariesFuture.get(),
            center.longitude,
            center.latitude,
        ).roofsAreUsuallyFlat
    }

    override fun applyAnswerTo(answer: RoofShape, tags: Tags, timestampEdited: Long) {
        tags["roof:shape"] = answer.osmValue
    }
}
