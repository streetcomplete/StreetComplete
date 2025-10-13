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
    override val icon = R.drawable.ic_quest_ferry
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_ferry_motor_vehicle_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["motor_vehicle"] = answer.toYesNo()
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> = mapData
            .filter(filter)
            .asIterable()

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        if (element is Way) return null
        return true
    }
}
