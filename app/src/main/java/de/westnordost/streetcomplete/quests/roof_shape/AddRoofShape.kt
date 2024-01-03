package de.westnordost.streetcomplete.quests.roof_shape

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.numberSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

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
    override val icon = R.drawable.ic_quest_roof_shape
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roofShape

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofShape_title

    override fun createForm() = AddRoofShapeForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry) =
        mapData.filter { element ->
            filter.matches(element) && (
                (element.tags["roof:levels"]?.toFloatOrNull() ?: 0f) > 0f
                    || roofsAreUsuallyFlatAt(element, mapData) == false
            ) && levelsOk(element)
        }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        /* if it has 0 roof levels, or the roof levels aren't specified,
           the quest should only be shown in certain countries. But whether
           the element is in a certain country cannot be ascertained without the element's geometry */
        if ((element.tags["roof:levels"]?.toFloatOrNull() ?: 0f) == 0f) return null
        return levelsOk(element)
    }

    private fun levelsOk(element: Element): Boolean =
        ((element.tags["building:levels"]?.toIntOrNull() ?: 0) -
            (element.tags["roof:levels"]?.toIntOrNull() ?: 0)) <= prefs.getInt(questPrefix(prefs) + PREF_ROOF_SHAPE_MAX_LEVELS, 99)

    private fun roofsAreUsuallyFlatAt(element: Element, mapData: MapDataWithGeometry): Boolean? {
        val center = mapData.getGeometry(element.type, element.id)?.center ?: return null
        return getCountryInfoByLocation(center).roofsAreUsuallyFlat
    }

    override fun applyAnswerTo(answer: RoofShape, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["roof:shape"] = answer.osmValue
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) = numberSelectionDialog(
        context, prefs, questPrefix(prefs) + PREF_ROOF_SHAPE_MAX_LEVELS, 99, R.string.quest_settings_max_roof_levels
    )

}

private const val PREF_ROOF_SHAPE_MAX_LEVELS = "qs_AddRoofShape_max_levels"
