package de.westnordost.streetcomplete.quests.aerialway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.YES
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.SUMMER
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.NO

class AddAerialwayBicycleAccess : OsmElementQuestType<AerialwayBicycleAccessAnswer>, AndroidQuest {

    private val filter by lazy {
        "ways, relations with aerialway and !aerialway:bicycle and !bicycle".toElementFilterExpression()
    }
    override val changesetComment = "Specify aerialway access for bicycles"
    override val wikiLink = "Tag:aerialway"
    override val icon = R.drawable.ic_quest_aerialway_bicycle
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_aerialway_bicycle_title

    override fun createForm() = AddAerialwayBicycleAccessForm()

    override fun applyAnswerTo(answer: AerialwayBicycleAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["aerialway:bicycle"] = when (answer) {
            YES -> "yes"
            SUMMER -> "-1"
            NO -> "no"
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData
            .filter(filter)
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        if (element is Way) return null
        return true
    }
}
