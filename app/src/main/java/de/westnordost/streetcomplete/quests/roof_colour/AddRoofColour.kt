package de.westnordost.streetcomplete.quests.roof_colour

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import java.util.concurrent.FutureTask

class AddRoofColour(
    private val countryInfos: CountryInfos,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
) : OsmElementQuestType<RoofColour> {

    private val filter by lazy { """
        ways, relations with
          ((building:levels or roof:levels) or (building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}))
          and !roof:colour
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify roof colour"
    override val wikiLink = "Key:roof:colour"
    override val icon = R.drawable.ic_quest_roof_colour
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roofColour

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofColour_title

    override fun createForm() = AddRoofColourForm()

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

    override fun applyAnswerTo(answer: RoofColour, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["roof:colour"] = answer.osmValue
    }
}
