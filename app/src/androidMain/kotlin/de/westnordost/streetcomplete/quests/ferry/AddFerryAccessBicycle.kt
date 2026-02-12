package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddFerryAccessBicycle :
    OsmElementQuestType<FerryBicycleAccess>,
    AndroidQuest {

    private val filter by lazy {
        "ways, relations with route = ferry and !bicycle and !bicycle:signed"
            .toElementFilterExpression()
    }

    override val changesetComment = "Specify ferry access for bicycles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry_bicycle
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, EditTypeAchievement.BICYCLIST)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_ferry_bicycle_title

    override fun createForm() = AddFerryAccessBicycleForm()

    override fun applyAnswerTo(
        answer: FerryBicycleAccess,
        tags: StringMapChangesBuilder,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        when (answer) {
            FerryBicycleAccess.YES ->
                tags["bicycle"] = "yes"

            FerryBicycleAccess.NO ->
                tags["bicycle"] = "no"

            FerryBicycleAccess.NOT_SIGNED ->
                tags["bicycle:signed"] = "no"
        }
    }

    override fun getApplicableElements(
        mapData: MapDataWithGeometry
    ): Iterable<Element> {
        val wayIdsInFerryRoutes = wayIdsInFerryRoutes(mapData.relations)
        return mapData
            .filter(filter)
            .filter { it !is Way || it.id !in wayIdsInFerryRoutes }
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        if (element is Way) return null
        return true
    }
}
