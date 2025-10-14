package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags

class AddStepsIncline : OsmElementQuestType<Incline>, AndroidQuest {

    private val filter by lazy { """
         ways with highway = steps
         and (!indoor or indoor = no)
         and area != yes
         and access !~ private|no
         and !incline
    """.toElementFilterExpression() }
    override val changesetComment = "Specify which way leads up for steps"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_steps
    override val achievements = listOf(PEDESTRIAN)

    override val hint = R.string.quest_arrow_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_incline_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> = mapData
        .filter(filter)
        .filter { it is Way && !it.isClosed }
        .asIterable()

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        return null
    }

    override fun createForm() = AddInclineForm()

    override fun applyAnswerTo(answer: Incline, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        answer.applyTo(tags)
}
