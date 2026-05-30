package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.PAVED_SURFACES
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.BOTTOM
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.NO
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.TOP
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.YES
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddTactilePavingSteps : OsmFilterQuestType<TactilePavingStepsAnswer>() {

    override val elementFilter = """
        ways with highway = steps
         and surface ~ ${PAVED_SURFACES.joinToString("|")}
         and !sac_scale
         and (!conveying or conveying = no)
         and access !~ private|no
        and (
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving ~ no|partial|incorrect and tactile_paving older today -8 years
          or tactile_paving = yes and tactile_paving older today -12 years
        )
    """

    override val changesetComment = "Survey tactile paving on steps"
    override val wikiLink = "Key:tactile_paving"
    override val icon = Res.drawable.quest_steps_tactile_paving
    override val title = Res.string.quest_tactilePaving_title_steps
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)
    override val hint = Res.string.quest_generic_looks_like_this
    override val hintImages = listOf(
        Res.drawable.tactile_paving1,
        Res.drawable.tactile_paving2,
        Res.drawable.tactile_paving3
    )

    @Composable
    override fun Form(onAnswer: (QuestAnswer<TactilePavingStepsAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(Answer(YES)) }
            ),
            onAnswer = onAnswer,
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_tactilePaving_steps_bottom)) { onAnswer(Answer(BOTTOM)) },
                AnswerItem(stringResource(Res.string.quest_tactilePaving_steps_top)) { onAnswer(Answer(TOP)) }
            )
        )
    }

    override fun applyAnswerTo(answer: TactilePavingStepsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.osmValue)
    }
}
