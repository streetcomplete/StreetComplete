package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.ktx.toYesNo

class AddTactilePavingCrosswalk : OsmElementQuestType<Boolean> {

    private val crossingFilter by lazy { """
        nodes with
          (
            highway = traffic_signals and crossing = traffic_signals and foot != no
            or highway = crossing and foot != no
          )
          and (
            !tactile_paving
            or tactile_paving = unknown
            or tactile_paving = no and tactile_paving older today -4 years
            or tactile_paving = yes and tactile_paving older today -8 years
          )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = cycleway and foot !~ yes|designated
          or highway = service and service = driveway
          or highway and access ~ private|no
    """.toElementFilterExpression() }

    override val commitMessage = "Add tactile pavings on crosswalks"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_blind_pedestrian_crossing
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON

    override val questTypeAchievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_crosswalk

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

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("tactile_paving", answer.toYesNo())
    }
}
