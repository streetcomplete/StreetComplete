package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.ktx.isArea
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.way_lit.WayLit.*
import de.westnordost.streetcomplete.quests.AnswerItem

class WayLitForm : AbstractQuestAnswerFragment<WayLitOrIsStepsAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) }
    )

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_way_lit_24_7) { applyAnswer(NIGHT_AND_DAY) },
        AnswerItem(R.string.quest_way_lit_automatic) { applyAnswer(AUTOMATIC) },
        createConvertToStepsAnswer(),
    )

    private fun createConvertToStepsAnswer(): AnswerItem? {
        val way = osmElement as? Way ?: return null
        if (way.isArea() || way.tags["highway"] == "steps") return null

        return AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
            applyAnswer(IsActuallyStepsAnswer)
        }
    }
}
