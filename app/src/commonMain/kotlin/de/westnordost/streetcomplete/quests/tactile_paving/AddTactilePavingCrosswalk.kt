package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer.INCORRECT
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer.NO
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer.YES
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

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
            or tactile_paving ~ no|partial|incorrect and tactile_paving older today -8 years
            or tactile_paving = yes and tactile_paving older today -12 years
          )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = cycleway and foot !~ yes|designated
          or highway = service and service = driveway
          or highway and access ~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether crosswalks have tactile paving"
    override val wikiLink = "Key:tactile_paving"
    override val icon = Res.drawable.quest_blind_pedestrian_crossing
    override val title = Res.string.quest_tactilePaving_title_crosswalk
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)
    override val hint = Res.string.quest_generic_looks_like_this
    override val hintImages = listOf(
        Res.drawable.tactile_paving1,
        Res.drawable.tactile_paving2,
        Res.drawable.tactile_paving3
    )

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isCrossing() }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    @Composable
    override fun Form(on: (QuestAction<TactilePavingCrosswalkAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES)) }
            ),
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_tactilePaving_incorrect)) {
                    on(Answer(INCORRECT))
                }
            ) }
        )
    }

    override fun applyAnswerTo(answer: TactilePavingCrosswalkAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.osmValue)
    }
}
