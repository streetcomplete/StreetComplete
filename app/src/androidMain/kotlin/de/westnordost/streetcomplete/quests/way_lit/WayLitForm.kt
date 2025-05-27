package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.couldBeSteps

class WayLitForm : AbstractOsmQuestForm<WayLitOrIsStepsAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(WayLit(NO)) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(WayLit(YES)) }
    )

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.lit_value_24_7) { applyAnswer(WayLit(NIGHT_AND_DAY)) },
        AnswerItem(R.string.lit_value_automatic) { applyAnswer(WayLit(AUTOMATIC)) },
        createConvertToStepsAnswer(),
    )

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
                applyAnswer(IsActuallyStepsAnswer)
            }
        } else {
            null
        }
}
