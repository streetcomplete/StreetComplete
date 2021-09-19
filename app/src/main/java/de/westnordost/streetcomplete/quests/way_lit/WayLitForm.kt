package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.way_lit.WayLit.*
import de.westnordost.streetcomplete.quests.AnswerItem

class WayLitForm : AbstractQuestAnswerFragment<WayLit>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_way_lit_24_7) { applyAnswer(NIGHT_AND_DAY) },
        AnswerItem(R.string.quest_way_lit_automatic) { applyAnswer(AUTOMATIC) }
    )
}
