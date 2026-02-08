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
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags

class AddFerryAccessBicycle : OsmElementQuestType<FerryBicycleAccess>, AndroidQuest {

    private val filter by lazy {
        "ways, relations with route = ferry and !bicycle".toElementFilterExpression()
    }
    override val changesetComment = "Specify ferry access for bicycles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry_bicycle
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_ferry_bicycle_title

    override fun createForm() = AddFerryAccessBicycleForm()

    override fun applyAnswerTo(answer: FerryBicycleAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long)
    {
        when (answer) {
            FerryBicycleAccess.ALLOWED -> {
                tags["bicycle"] = "yes"
            }
            FerryBicycleAccess.NOT_ALLOWED -> {
                tags["bicycle"] = "no"
            }
            FerryBicycleAccess.NOT_SIGNED -> {
                tags["bicycle:signed"] = "no"
            }
        }
    }

    override fun getApplicableElements(
        mapData: MapDataWithGeometry
    ): Iterable<Element> {
        // adapted from AddFerryAccessPedestrian / AddMaxWeight
        val wayIdsInFerryRoutes = wayIdsInFerryRoutes(mapData.relations)
        return mapData
            .filter(filter)
            .filter { it !is Way || it.id !in wayIdsInFerryRoutes }
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        // defer ways that may be part of a ferry relation
        if (element is Way) return null
        return true
    }
}
