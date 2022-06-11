package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddTactilePavingCrosswalk : OsmElementQuestType<TactilePavingCrosswalkAnswer> {

    private val crossingFilter by lazy { """
        nodes with
          (
            highway = traffic_signals and crossing = traffic_signals and foot != no
            or highway = crossing and foot != no
          )
          and (
            !tactile_paving
            or tactile_paving = unknown
            or tactile_paving ~ no|incorrect and tactile_paving older today -4 years
            or tactile_paving = yes and tactile_paving older today -8 years
          )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = cycleway and foot !~ yes|designated
          or highway = service and service = driveway
          or highway and access ~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Add tactile pavings on crosswalks"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_blind_pedestrian_crossing
    override val questTypeAchievements = listOf(BLIND)

    override fun isEnabled(countryInfo: CountryInfo) = countryInfo.hasTactilePaving

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_crosswalk

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossing() }.asSequence()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    override fun createForm() = TactilePavingCrosswalkForm()

    override fun applyAnswerTo(answer: TactilePavingCrosswalkAnswer, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.osmValue)
    }
}
