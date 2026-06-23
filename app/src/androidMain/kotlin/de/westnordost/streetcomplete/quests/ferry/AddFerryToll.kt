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
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFerryToll : OsmElementQuestType<Boolean>, AndroidQuest {

    private val filter by lazy {
        """
        ways, relations with
          route = ferry
          and !toll
          and !fee
        """.toElementFilterExpression()
    }

    override val changesetComment = "Specify whether a ferry requires payment"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry_fee
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_ferry_toll_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(
        answer: Boolean,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags["toll"] = answer.toYesNo()
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // same logic as other ferry access quests
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
