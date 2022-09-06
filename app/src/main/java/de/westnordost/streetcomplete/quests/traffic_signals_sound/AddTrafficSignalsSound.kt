package de.westnordost.streetcomplete.quests.traffic_signals_sound

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isCrossingWithTrafficSignals
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddTrafficSignalsSound : OsmElementQuestType<Boolean> {

    private val crossingFilter by lazy { """
        nodes with crossing = traffic_signals
         and highway ~ crossing|traffic_signals
         and foot != no
         and (
          !$SOUND_SIGNALS
          or $SOUND_SIGNALS = no and $SOUND_SIGNALS older today -4 years
          or $SOUND_SIGNALS older today -8 years
         )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = cycleway
          and foot !~ yes|designated
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether traffic signals have sound signals"
    override val wikiLink = "Key:$SOUND_SIGNALS"
    override val icon = R.drawable.ic_quest_blind_traffic_lights_sound
    override val achievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_sound_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossingWithTrafficSignals() }.asSequence()

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

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate(SOUND_SIGNALS, answer.toYesNo())
    }
}

private const val SOUND_SIGNALS = "traffic_signals:sound"
