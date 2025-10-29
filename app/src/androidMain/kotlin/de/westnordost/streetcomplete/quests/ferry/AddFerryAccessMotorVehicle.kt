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
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFerryAccessMotorVehicle : OsmElementQuestType<Boolean>, AndroidQuest {

    private val filter by lazy {
        "ways, relations with route = ferry and !motor_vehicle".toElementFilterExpression()
    }
    override val changesetComment = "Specify ferry access for motor vehicles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.quest_ferry
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_ferry_motor_vehicle_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["motor_vehicle"] = answer.toYesNo()
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // the quest shall not be asked for ways tagged with route=ferry that are part of a relation
        // also tagged with route=ferry because that makes the former not actually a "real" ferry
        // route (╯°□°）╯︵ ┻━┻. Tagging mistake or not, it is very common tagging (#6373)
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
