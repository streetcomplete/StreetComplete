package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

class AddRoofShape(private val countryInfos: CountryInfos) : OsmElementQuestType<String> {

    private val filter by lazy { """
        ways, relations with
          roof:levels and !roof:shape and !3dr:type and !3dr:roof
    """.toElementFilterExpression() }

    override val commitMessage = "Add roof shapes"
    override val wikiLink = "Key:roof:shape"
    override val icon = R.drawable.ic_quest_roof_shape

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofShape_title

    override fun createForm() = AddRoofShapeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("roof:shape", answer)
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry) =
        mapData.filter {
            filter.matches(it) && (
                it.tags?.get("roof:levels") != "0" ||
                roofsAreUsuallyFlatAt(it, mapData) == false
            )
        }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        // if it has 0 roof levels, the quest should only be shown in certain countries. But whether
        // the element is in a certain country cannot be ascertained at this point
        if (element.tags?.get("roof:levels") == "0") return null
        return true
    }

    private fun roofsAreUsuallyFlatAt(element: Element, mapData: MapDataWithGeometry): Boolean? {
        val center = mapData.getGeometry(element.type, element.id)?.center ?: return null
        return countryInfos.get(center.longitude, center.latitude).isRoofsAreUsuallyFlat
    }
}
