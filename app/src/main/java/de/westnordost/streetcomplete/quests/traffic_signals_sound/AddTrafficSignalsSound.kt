package de.westnordost.streetcomplete.quests.traffic_signals_sound

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsSound : OsmElementQuestType<Boolean> {

    private val crossingFilter by lazy { """
        nodes with crossing = traffic_signals and highway ~ crossing|traffic_signals and foot != no
        and (
          !$SOUND_SIGNALS
          or $SOUND_SIGNALS = no and $SOUND_SIGNALS older today -4 years
          or $SOUND_SIGNALS older today -8 years
        )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = cycleway and foot !~ yes|designated
    """.toElementFilterExpression() }

    override val commitMessage = "Add whether traffic signals have sound signals"
    override val wikiLink = "Key:$SOUND_SIGNALS"
    override val icon = R.drawable.ic_quest_blind_traffic_lights_sound

    override val questTypeAchievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_sound_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element))
            false
        else
            null

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate(SOUND_SIGNALS, answer.toYesNo())
    }
}

private const val SOUND_SIGNALS = "traffic_signals:sound"
