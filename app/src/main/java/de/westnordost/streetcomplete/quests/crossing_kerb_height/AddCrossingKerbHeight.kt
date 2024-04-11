package de.westnordost.streetcomplete.quests.crossing_kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeightForm
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight

class AddCrossingKerbHeight : OsmElementQuestType<KerbHeight> {

    private val crossingFilter by lazy { """
        nodes with
          highway = crossing
          and foot != no
          and (!kerb:left or !kerb:right)
          and (
            !kerb
            or kerb ~ yes|unknown
            or kerb !~ no|rolled and kerb older today -8 years
          )
    """.toElementFilterExpression() }

    /* The quest should not be asked when the kerb situation can theoretically be tagged with
       greater detail, i.e. where the sidewalks are mapped as separate ways and hence there is a
       footway that crosses the road at the highway=crossing node: In that case, it would be
       possible to put the kerbs at their actual physical locations. */
    private val excludedWaysFilter by lazy { """
        ways with
          highway and access ~ private|no
          or highway ~ footway|path|cycleway
          or highway = service and service = driveway
    """.toElementFilterExpression() }

    override val changesetComment = "Determine the heights of kerbs at crossings"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_wheelchair_crossing
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_kerb_height_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    override fun createForm() = AddKerbHeightForm()

    override fun applyAnswerTo(answer: KerbHeight, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("kerb", answer.osmValue)
    }
}
