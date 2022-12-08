package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.measuredLength

class AddBicycleIncline : OsmElementQuestType<BicycleInclineAnswer> {

    private val tagFilter by lazy { """
        ways with mtb:scale:uphill
         and highway ~ footway|cycleway|path|bridleway|track
         and (!indoor or indoor = no)
         and area != yes
         and access !~ private|no
         and !incline
    """.toElementFilterExpression() }

    override val changesetComment = "Specify which way leads up (where mtb:scale:uphill is present)"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_bicycle_incline
    override val achievements = listOf(BICYCLIST)
    override val hasMarkersAtEnds = false

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // sadly for very long ways shape may be complex and it may be confusing which answer should be given
        // once multiple quest markers are appearing it becomes completely unclear
        // see for example https://www.openstreetmap.org/way/437205914
        return mapData
            .filter { element -> tagFilter.matches(element) }
            .filter { element ->
                val geometry = mapData.getGeometry(element.type, element.id) as? ElementPolylinesGeometry
                geometry?.polylines?.all { it.measuredLength() < 200 } == true
            }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        // we don't want to show overly long things
        if (!tagFilter.matches(element)) return false
        return null
    }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_incline_title

    override fun createForm() = AddBicycleInclineForm()

    override fun applyAnswerTo(answer: BicycleInclineAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        answer.applyTo(tags)
}
