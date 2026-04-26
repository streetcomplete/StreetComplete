package de.westnordost.streetcomplete.quests.way_lit

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.stringResource

class WayLitForm : AbstractOsmQuestForm<WayLitOrIsStepsAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                    applyAnswer(WayLit(NO))
                },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                    applyAnswer(WayLit(YES))
                }
            ),
            otherAnswers = listOfNotNull(
                Answer(stringResource(Res.string.lit_value_24_7)) {
                    applyAnswer(WayLit(NIGHT_AND_DAY))
                },
                Answer(stringResource(Res.string.lit_value_automatic)) {
                    applyAnswer(WayLit(AUTOMATIC))
                },
                if (element.couldBeSteps()) {
                    Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        applyAnswer(IsActuallyStepsAnswer)
                    }
                } else {
                    null
                },
            )
        )
    }
}
